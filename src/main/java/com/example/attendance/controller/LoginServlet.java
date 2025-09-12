package com.example.attendance.controller;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.example.attendance.dao.AttendanceDAO;
import com.example.attendance.dao.UserDAO;
import com.example.attendance.dto.User;

/**
 * P63
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
	
	private final UserDAO userDAO = new UserDAO();
	private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String name = request.getParameter("name");

		String password = request.getParameter("password");

		User user = userDAO.findByName(name);

		
		/* ユーザーが登録されている、ユーザーの有効化が正、パスワードのハッシュ化がなされている、全てを満たす時
			セッションに成功メッセージをセット
		*/
		if (user != null && user.isEnabled() && userDAO.verifyPassword(name, password)) {
			HttpSession session = request.getSession();
			session.setAttribute("user", user);
			session.setAttribute("successMessage", "ログインしました");
			
			/*　ロールが管理者の時、全員の合計労働時間を取得し管理者画面に遷移 */
			if ("admin".equals(user.getRole())) {
				request.setAttribute("allAttendanceRecords", attendanceDAO.findAll());
				
				Map<String, Long> totalHoursByUser = attendanceDAO.findAll().stream()
						.collect(Collectors.groupingBy(
								com.example.attendance.dto.Attendance::getUserId,
								Collectors.summingLong(att -> {
												if (att.getCheckInTime() != null && att.getCheckOutTime() != null) {
													return java.time.temporal.ChronoUnit.HOURS.between(
															att.getCheckInTime(),
															att.getCheckOutTime()
														);
												}
												return 0L;
								})));
				
				request.setAttribute("totalHoursByUser", totalHoursByUser);
				RequestDispatcher rd = request.getRequestDispatcher("jsp/admin_menu.jsp");
				rd.forward(request, response);
				
			/* ロールが従業員の時、自身の従業員画面に遷移 */
			} else {
				request.setAttribute(
						"attendanceRecords",
						attendanceDAO.findByUserId(user.getName())
						);
				RequestDispatcher rd = request.getRequestDispatcher("jsp/employee_menu.jsp");
				rd.forward(request, response);
			}
			
		/*  ユーザーが登録されていない、またはユーザーが無効、またはパスワードがハッシュ化されていない */
		} else {
			request.setAttribute("errorMessage", "ユーザーIDまたはパスワードが不正、あるいはアカウントが無効です。");
			RequestDispatcher rd = request.getRequestDispatcher("/login.jsp");
			rd.forward(request, response);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
