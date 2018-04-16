package clc3.webapi.mc;

import clc3.common.servlets.requests.IdRequest;
import clc3.montecarlo.database.daos.MCTaskDao;
import clc3.montecarlo.database.daos.MCTaskResultDao;
import clc3.montecarlo.database.entities.MCTask;
import clc3.montecarlo.database.entities.MCTaskResult;
import clc3.montecarlo.database.enums.TaskStatus;
import clc3.montecarlo.util.MCTaskResultAggregator;
import clc3.webapi.mc.responses.MCTaskProgressResponse;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;

@WebServlet(urlPatterns = "/mc/task/progress")
public class MCTaskProgressServlet extends BaseServlet {

    private MCTaskDao taskDao = serviceLocator.getMcTaskDao();
    private MCTaskResultDao taskResultDao = serviceLocator.getMcTaskResultDao();

    @Override
    protected Object createEmptyParams() {
        return new IdRequest();
    }

    @Override
    protected void process(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        IdRequest req = (IdRequest) getParameters(request);

        Map<Long, MCTaskProgressResponse> resp = new HashMap<>();

        MCTask task = taskDao.getById(req.getId());
        if (task != null) {
            MCTaskResult aggregatedTaskResult = null;

            List<MCTaskResult> taskResults = taskResultDao.getByMcTaskKey(task.getKey());
            if (taskResults.size() > 1) {
                taskResultDao.deleteBulk(taskResults);

                aggregatedTaskResult = MCTaskResultAggregator.aggregateTaskResults(taskResults);
                aggregatedTaskResult.setMcTask(task.getKey());
                taskResultDao.save(aggregatedTaskResult);
            } else if (taskResults.size() == 1) {
                aggregatedTaskResult = taskResults.get(0);
            }

            if (aggregatedTaskResult != null) {
                long duration = aggregatedTaskResult.getComputeTimes().stream().mapToInt(Long::intValue).sum();
                if (task.getStatus() != TaskStatus.DONE
                        && aggregatedTaskResult.getCalculatedIterations() == task.getIterations()) {
                    task.setStatus(TaskStatus.DONE);
                    task.setCompleted(new Date());
                    task.setDuration(duration);
                    taskDao.save(task);
                }

                double progress = (double) aggregatedTaskResult.getCalculatedIterations()
                        / (double) task.getIterations();
                
                resp.put(task.getId(), new MCTaskProgressResponse(progress * 100, duration, task.getStatus()));
            } else {
                resp.put(task.getId(), new MCTaskProgressResponse(0.0, 0, task.getStatus()));
            }
        }
        jsonResponse(response, resp);
    }
}