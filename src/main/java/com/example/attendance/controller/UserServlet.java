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
			String userName = request.getParameter("userName");
			User user = userDAO.findByUserName(userName);
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
		User currentUser = (User) session.getAttribute("user");
		
		if (currentUser == null || !"admin".equals(currentUser.getRole())) {
			response.sendRedirect("login.jsp");
			return;
		}
		
		if ("add".equals(action)) {
			String userName = request.getParameter("userName");
			String password = request.getParameter("password");
			String role = request.getParameter("role");
			
			System.out.println(userName);
			System.out.println(password);
			System.out.println(role);
			
			if (userDAO.findByUserName(userName) == null) {
				userDAO.addUser(
						new User(
								userName, password, role
							)
						);
				session.setAttribute("successMessage", "ユーザーを追加しました");
				
			} else {
				request.setAttribute("errorMessage", "このユーザーIDは既に存在します");
			}
			
		} else if ("update".equals(action)) {
			String userName = request.getParameter("userName");
			String role = request.getParameter("role");
			boolean isEnabled = request.getParameter("isEnabled") != null;
			
			User existingUser = userDAO.findByUserName(userName);
			
			if (existingUser != null) {
				userDAO.updateUser(
						new User(userName, existingUser.getPassword(), role, isEnabled)
						);
				session.setAttribute("successMessage", "ユーザー情報を更新しました");
			}
			
		} else if ("delete".equals(action)) {
			String userName = request.getParameter("userName");
			userDAO.deleteUser(userName);
			session.setAttribute("successMessage", "ユーザーを削除しました");
			
		} else if ("reset_password".equals(action)) {
			String userName = request.getParameter("userName");
			String newPassword = request.getParameter("newPassword");
			userDAO.resetPassword(userName, newPassword);
			session.setAttribute(
					"successMessage", userName + "のパスワードをリセットしました　新しいパスワードは" + newPassword + "です");
			
		} else if ("toggle_enabled".equals(action)) {
			String userName = request.getParameter("userName");
			boolean isEnabled = Boolean.parseBoolean(request.getParameter("isEnabled"));
			userDAO.toggleUserEnabled(userName, isEnabled);
			session.setAttribute("successMessage", userName + "のアカウントを" + (isEnabled ? "有効" : "無効") + "にしました");
		}	
		response.sendRedirect("users?action=list");
		
	}
}
