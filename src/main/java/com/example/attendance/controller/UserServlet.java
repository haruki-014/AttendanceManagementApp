package com.example.attendance.controller;

import java.io.IOException;
import java.util.Collection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.example.attendance.dao.UserDAO;
import com.example.attendance.dto.User;

@WebServlet("/users")
public class UserServlet extends HttpServlet {
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO(); // DB利用のDAO
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) {
            action = "list";
        }

        switch (action) {
            case "list":
                listUsers(req, resp);
                break;
            case "edit":
                editUser(req, resp);
                break;
            case "delete":
                deleteUser(req, resp);
                break;
            default:
                listUsers(req, resp);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        HttpSession session = req.getSession(false);
		User currentUser = (User) session.getAttribute("user"); 
		
        if (action == null) {
            action = "add";
        }

        switch (action) {
            case "add":
                addUser(req, resp);
                break;
            case "update":
                updateUser(req, resp);
                break;
            case "resetPassword":
                resetPassword(req, resp);
                break;
            case "toggle_enabled":
            	toggleEnabled(req, resp);
            default:
                listUsers(req, resp);
                break;
        }
        
        resp.sendRedirect("users?action=list");
    }

    private void listUsers(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Collection<User> users = userDAO.getAllUsers();
        req.setAttribute("users", users);
        req.getRequestDispatcher("/WEB-INF/views/userList.jsp").forward(req, resp);
    }

    private void editUser(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer id = Integer.parseInt(req.getParameter("id"));
        User user = userDAO.findById(id);
        req.setAttribute("user", user);
        req.getRequestDispatcher("/WEB-INF/views/userForm.jsp").forward(req, resp);
    }

    private void addUser(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String name = req.getParameter("name");
        String password = req.getParameter("password");
        String role = req.getParameter("role");
        boolean isEnabled = Boolean.parseBoolean(req.getParameter("isEnabled"));

        User user = new User(null, name, password, role, isEnabled);
        userDAO.addUser(user);

        resp.sendRedirect("users?action=list");
    }

    private void updateUser(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Integer id = Integer.parseInt(req.getParameter("id"));
        String password = req.getParameter("password");
        String role = req.getParameter("role");
        boolean isEnabled = Boolean.parseBoolean(req.getParameter("isEnabled"));

        User user = new User(id, null, password, role, isEnabled);
        userDAO.updateUser(user);

        resp.sendRedirect("users?action=list");
    }

    private void deleteUser(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Integer id = Integer.parseInt(req.getParameter("id"));
        userDAO.deleteUser(id);
        resp.sendRedirect("users?action=list");
    }

    private void resetPassword(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Integer id = Integer.parseInt(req.getParameter("id"));
        String newPassword = req.getParameter("newPassword");

        userDAO.resetPassword(id, newPassword);

        resp.sendRedirect("users?action=list");
    }
    
    private void toggleEnabled(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    	Integer id = Integer.parseInt(req.getParameter("id"));
		boolean isEnabled = Boolean.parseBoolean(req.getParameter("isEnabled"));
		userDAO.toggleUserEnabled(id, isEnabled);
    }
	
}
