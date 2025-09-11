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

/**
 * P73
 */
@WebServlet("/users")
public class UserServlet extends HttpServlet {
	private final UserDAO userDAO = new UserDAO();
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");
		HttpSession session = request.getSession(false);
		User currentUser = (User) session.getAttribute("user"); 
		
		// ユーザーが登録されていない or 役割がadminでなければ、ログイン画面に遷移	
		if (currentUser == null || !"admin".equals(currentUser.getRole())) {
			response.sendRedirect("login.jsp");
			return;
		}
		
		String message = (String) session.getAttribute("successMessage");
		if (message != null) {
			request.setAttribute("successMessage", message);
			session.removeAttribute("successMessage");
		}
		
		if ("list".equals(action) || action == null) {
			Collection<User> users = userDAO.getAllUsers();
			request.setAttribute("users", users);
			RequestDispatcher rd = request.getRequestDispatcher("/jsp/user_management.jsp");
			rd.forward(request, response);
			
		} else if ("edit".equals(action)) {
			String name = request.getParameter("name");
			User user = userDAO.findByName(name);
			request.setAttribute("userToEdit", user);
			Collection<User> users = userDAO.getAllUsers();
			request.setAttribute("users", users);
			RequestDispatcher rd = request.getRequestDispatcher("/jsp/user_management.jsp");
			rd.forward(request, response);
			
		} else {
			response.sendRedirect("users?action=list");
		}	
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String action = request.getParameter("action");
		System.out.println(">>> doPost called, action=" + action);
		HttpSession session = request.getSession(false);
		
		if (session == null) {
		    System.out.println(">>> session is null");
		} else {
			System.out.println(">>> session is not null");
		}
		
		User currentUser = (User) session.getAttribute("user");
		
		if (currentUser == null || !"admin".equals(currentUser.getRole())) {
			response.sendRedirect(request.getContextPath() + "login.jsp");
			return;
		}
		
		System.out.println("user=true");
		
		if ("add".equals(action)) {
			String name = request.getParameter("name");
			String password = request.getParameter("password");
			String role = request.getParameter("role");
			
			System.out.println(name);
			System.out.println(password);
			System.out.println(role);
			
			if (userDAO.findByName(name) == null) {
				userDAO.addUser(
						new User(
								name, password, role
							)
						);
				session.setAttribute("successMessage", "ユーザーを追加しました");
				
			} else {
				request.setAttribute("errorMessage", "このユーザーIDは既に存在します");
			}
			
		} else if ("update".equals(action)) {
			int id = Integer.parseInt(request.getParameter("id"));
			System.out.println(id);
			String name = request.getParameter("name");
			String role = request.getParameter("role");
			boolean isEnabled = request.getParameter("enabled") != null;
			
			User existingUser = userDAO.findById(id);
			
			if (existingUser != null) {
				userDAO.updateUser(
						new User(id, name, existingUser.getPassword(), role, isEnabled)
						);
				session.setAttribute("successMessage", "ユーザー情報を更新しました");
			}
			
		} else if ("delete".equals(action)) {
			String name = request.getParameter("name");
			userDAO.deleteUser(name);
			session.setAttribute("successMessage", "ユーザーを削除しました");
			
		} else if ("reset_password".equals(action)) {
			String name = request.getParameter("name");
			String newPassword = request.getParameter("newPassword");
			userDAO.resetPassword(name, newPassword);
			session.setAttribute(
					"successMessage", name + "のパスワードをリセットしました　新しいパスワードは" + newPassword + "です");
			
		} else if ("toggle_enabled".equals(action)) {
			String name = request.getParameter("name");
			boolean isEnabled = Boolean.parseBoolean(request.getParameter("isEnabled"));
			userDAO.toggleUserEnabled(name, isEnabled);
			session.setAttribute("successMessage", name + "のアカウントを" + (isEnabled ? "有効" : "無効") + "にしました");
		}	
		response.sendRedirect(request.getContextPath() + "/users?action=list");
		
	}
}
