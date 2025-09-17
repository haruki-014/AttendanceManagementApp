package com.example.attendance.controller;

import java.io.IOException;
import java.util.Collection;

import jakarta.servlet.RequestDispatcher;
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
        userDAO = new UserDAO(); 
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
    	req.setCharacterEncoding("UTF-8");
    	
        String action = req.getParameter("action");
        HttpSession session = req.getSession(false);
		User currentUser = (User) session.getAttribute("user"); 
		
		if (currentUser == null) {
			resp.sendRedirect("/login");
			return;
		}
		
        if (action == null) {
            action = "list";
        }

        switch (action) {
            case "list":
                listUsers(req, resp);
                break;
            case "edit":
            	if ("admin".equals(currentUser.getRole())) {
            		editUser(req, resp);
            	} else {
            		resp.sendRedirect("/login");
            	}
                break;
            case "delete":
            	if ("admin".equals(currentUser.getRole())) {
            		deleteUser(req, resp);
            	} else {
            		resp.sendRedirect("/login");
            	}
                break;
            default:
                listUsers(req, resp);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
    	req.setCharacterEncoding("UTF-8");
    	
        String action = req.getParameter("action");
        
        System.out.println(action);
        HttpSession session = req.getSession(false);
		User currentUser = (User) session.getAttribute("user");

		if (currentUser == null || !"admin".equals(currentUser.getRole())) {
			resp.sendRedirect("/login");
			return;
		}
		
        if (action == null) {
            action = "add";
        }

        if ("delete".equals(action)) {
            deleteUser(req, resp);
            
        } else if ("add".equals(action)) {
            addUser(req, resp);
            
        } else if ("update".equals(action)) {
            updateUser(req, resp);
            
        } else if ("toggle_enabled".equals(action)) {
            toggleEnabled(req, resp);
            
        } else if ("reset_password".equals(action)) {
            resetPassword(req, resp);
            
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
        }
        
    }

    private void listUsers(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Collection<User> users = userDAO.getAllUsers();
        req.setAttribute("users", users);
        RequestDispatcher rd = req.getRequestDispatcher("/jsp/user_management.jsp");
        rd.forward(req, resp);
    }

    private void editUser(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer id = Integer.parseInt(req.getParameter("id"));
        User user = userDAO.findById(id);
        req.setAttribute("user", user);
        RequestDispatcher rd = req.getRequestDispatcher("/jsp/user_management.jsp");
        rd.forward(req, resp);
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
    	String idStr = req.getParameter("id");
        String newPassword = req.getParameter("newPassword");

        if (idStr == null || idStr.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ユーザーID または パスワード がありません");
            return;
        }
        
        Integer id = Integer.parseInt(idStr);

        userDAO.resetPassword(id, newPassword);

        resp.sendRedirect("users?action=list");
    }
    
    private void toggleEnabled(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    	Integer id = Integer.parseInt(req.getParameter("id"));
		boolean isEnabled = Boolean.parseBoolean(req.getParameter("isEnabled"));
		userDAO.toggleUserEnabled(id, !isEnabled);
		
		resp.sendRedirect("users?action=list");
    }
	
}
