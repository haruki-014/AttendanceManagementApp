package com.example.attendance.controller;

import java.io.IOException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.example.attendance.dao.UserDAO;

@WebServlet("/getUserName")
public class GetUserNameServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String userIdParam = request.getParameter("userId");
        String name = null;

        if (userIdParam != null && !userIdParam.isEmpty()) {
            try {
                Integer userId = Integer.parseInt(userIdParam);
                name = userDAO.findUserNameById(userId);
            } catch (NumberFormatException e) {
            }
        }

        response.setContentType("text/plain; charset=UTF-8");
        response.getWriter().write(name != null ? name : "");
    }
}
