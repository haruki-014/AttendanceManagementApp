package com.example.attendance.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
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
import com.example.attendance.dto.Attendance;
import com.example.attendance.dto.User;

/*
 * P41
 */

@WebServlet("/attendance")
public class AttendanceServlet extends HttpServlet {
	private final AttendanceDAO attendanceDAO = new AttendanceDAO();
       
    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");
		HttpSession session = request.getSession(false);
		User user = (User) session.getAttribute("user");
		
		if (user == null) {
			response.sendRedirect("login.jsp");
			return;
		}
		
		String message = (String) session.getAttribute("successMessage");
		if (message != null) {
			request.setAttribute("successMessage", message);
			session.removeAttribute("successMessage");
		}
		
		if ("export_csv".equals(action) && "admin".equals(user.getRole())) {
			exportCsv(request, response);
			
		} else if ("filter".equals(action) && "admin".equals(user.getRole())) {
			String filterUserId = request.getParameter("filterUserId");
			String startDateStr = request.getParameter("startDate");
			String endDateStr = request.getParameter("endDate");
			
			LocalDate startDate = null;
			LocalDate endDate = null;
			
			try {
				if (startDateStr != null && !startDateStr.isEmpty()) {
					startDate = LocalDate.parse(startDateStr);
				}
				if (endDateStr != null && !endDateStr.isEmpty()) {
					endDate = LocalDate.parse(endDateStr);
				}
				
			} catch (DateTimeParseException e) {
				request.setAttribute("errorMessage", "日付の形式が不正です");
			}
			
			List<Attendance> filteredRecords = attendanceDAO.findFilteredRecords(filterUserId, startDate, endDate);
			request.setAttribute("allAttendanceRecords", filteredRecords);
			
			Map<Integer, Long> totalHoursByUser = 
					filteredRecords.stream()
					.collect(Collectors.groupingBy(
							Attendance::getUserId,
							Collectors.summingLong(att -> {
										if (att.getCheck_in_time() != null && att.getCheck_out_time() != null) {
											return java.time.temporal.ChronoUnit.HOURS.between(att.getCheck_in_time(), att.getCheck_out_time());
										}
										return 0L;
									}
								)
							)
					);
			request.setAttribute("totalHoursByUser", totalHoursByUser);
			
			request.setAttribute(
					"monthlyWorkingHours",
					attendanceDAO.getMonthlyWorkingHours(null)
					);
			request.setAttribute(
					"monthlyCheckInCounts",
					attendanceDAO.getMonthlyCheckInCounts(null)
					);
			
			RequestDispatcher rd = request.getRequestDispatcher("/jsp/admin_menu.jsp");
			rd.forward(request, response);
			
		} else {
			request.setAttribute(
					"attendanceRecords",
					attendanceDAO.findByUserId(user.getId())
					);
			RequestDispatcher rd = request.getRequestDispatcher("/jsp/employee_menu.jsp");
			rd.forward(request, response);
		}
		
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		User user = (User) session.getAttribute("user");
		
		if (user == null) {
			response.sendRedirect("login.jsp");
			return;
		}
		
		String action = request.getParameter("action");
		
		if ("checkIn".equals(action)) {
			attendanceDAO.checkIn(user.getId());
			session.setAttribute("successMessage", "出勤を確認しました");
		} else if ("checkOut".equals(action)) {
			attendanceDAO.checkOut(user.getId());
			session.setAttribute("successMessage", "退勤を確認しました");
		} else if ("add_manual".equals(action) && "admin".equals(user.getRole())) {
			Integer userId = Integer.parseInt(request.getParameter("userId"));
			String checkInStr = request.getParameter("checkInTime");
			String checkOutStr = request.getParameter("checkOutTime");
			
			try {
				LocalDateTime checkIn = LocalDateTime.parse(checkInStr);
				LocalDateTime checkOut = 
						checkOutStr != null && !checkOutStr.isEmpty() ?
								LocalDateTime.parse(checkOutStr) : null;
				attendanceDAO.addManualAttendance(userId, checkIn, checkOut);
				session.setAttribute("successMessage", "手動で勤怠記録を追加しました");
			} catch (DateTimeParseException e) {
				session.setAttribute("errorMessage", "日付の形式が不正です");
			}
		} else if ("update_manual".equals(action) && "admin".equals(user.getRole())) {
			String userId = request.getParameter("userId");
			LocalDateTime oldCheckIn = LocalDateTime.parse(request.getParameter("oldCheckInTime"));
			LocalDateTime oldCheckOut =
					request.getParameter("oldCheckOut") != null &&
					!request.getParameter("oldCheckOut").isEmpty() ?
							LocalDateTime.parse(request.getParameter("oldCheckOut")) : null;
			LocalDateTime newCheckIn = LocalDateTime.parse(request.getParameter("newCheckInTime"));
			LocalDateTime newCheckOut =
					request.getParameter("newCheckOut") != null &&
					!request.getParameter("newCheckOut").isEmpty() ?
							LocalDateTime.parse(request.getParameter("newCheckOut")) : null;
			
			if (attendanceDAO.updateManualAttendance(userId, oldCheckIn, oldCheckOut, newCheckIn, newCheckOut)) {
				session.setAttribute("successMessage", "勤怠記録の手動更新に成功しました");
				
			} else {
				session.setAttribute("errorMessage", "勤怠記録の手動更新に失敗しました");
				
			}
			
		} else if ("delete_manual".equals(action) && "admin".equals(user.getRole())) {
			String userId = request.getParameter("userId");
			LocalDateTime checkIn = LocalDateTime.parse(request.getParameter("checkInTime"));
			LocalDateTime checkOut =
					request.getParameter("checkOutTime") != null &&
					!request.getParameter("checkOutTime").isEmpty() ?
							LocalDateTime.parse(request.getParameter("checkOutTime")) : null;
			
			if (attendanceDAO.deleteManualAttendance(userId, checkIn, checkOut)) {
				session.setAttribute("successMessage", "勤怠記録の削除に成功しました");
			} else {
				session.setAttribute("errorMessage", "勤怠記録の削除に失敗しました");
			}
			
		} 
		
		if ("admin".equals(user.getRole())) {
			response.sendRedirect(
					"attendance?action=filter&filterUserId" + 
					(request.getParameter("filterUserId") != null ?
							request.getParameter("filterUserId") : ""
					) +
					"&startDate=" +
					(request.getParameter("startDate") != null ?
							request.getParameter("startDate") : ""
					) +
					"&endDate=" +
					(request.getParameter("endDate") != null ?
							request.getParameter("endDate") : ""
					)
				);
		} else {
			response.sendRedirect("attendance");
		}
		
	}
	
	private void exportCsv(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/csv; charset=UTF-8");
		response.setHeader("Content-Disposition", "attachment; filename=\"attendance_records.csv\"");
		
		PrintWriter writer = response.getWriter();
		writer.append("User ID, Check-in Time, Check-out Time\n");
		
		String filterUserId = request.getParameter("filterUserId");
		String startDateStr = request.getParameter("startDate");
		String endDateStr = request.getParameter("endDate");
		LocalDate startDate = null;
		LocalDate endDate = null;
		
		try {
			if (startDateStr != null && !startDateStr.isEmpty()) {
				startDate = LocalDate.parse(startDateStr);
			}
			if (endDateStr != null && !endDateStr.isEmpty()) {
				startDate = LocalDate.parse(endDateStr);
			}
		} catch (DateTimeParseException e) {
			System.err.println("Invalid date format CSV export: " + e.getMessage());
		}
		
		List<Attendance> records = attendanceDAO.findFilteredRecords(filterUserId, startDate, endDate);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		
		for (Attendance record: records) {
			writer.append(String.format(
					"%s, %s, %s\n",
					record.getUserId(),
					record.getCheck_in_time() != null ? record.getCheck_in_time().format(formatter) : "",
					record.getCheck_out_time() != null ? record.getCheck_out_time().format(formatter) : ""
					)
			);
		}
		writer.flush();
		
	}

}
