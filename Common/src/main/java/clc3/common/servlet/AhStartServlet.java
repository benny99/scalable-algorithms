package clc3.common.servlet;

import clc3.common.AbstractServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/_ah/start")
public class AhStartServlet extends AbstractServlet {

    @Override
    protected void process(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // manual/basic scaling startup
        response.setStatus(HttpServletResponse.SC_OK);
    }
}