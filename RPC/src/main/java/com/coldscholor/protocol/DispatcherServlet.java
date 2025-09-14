package com.coldscholor.protocol;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author 寒士obj
 * @date 2025/09/13 13:48
 **/
public class DispatcherServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 有不同的请求，可以创建不同的处理类
        new HttpServerHandler().handle(req, resp);
        // ......
    }
}
