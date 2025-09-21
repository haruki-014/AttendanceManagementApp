package com.example.attendance.controller;

import java.io.IOException;
import java.util.List;
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

@WebServlet("/employeeMenu")
public class EmployeeMenuServlet extends HttpServlet {

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
		User currentUser = (User) session.getAttribute("user"); 

        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        
        String period = request.getParameter("period");
		System.out.println(period);
	    request.setAttribute("selectedPeriod", period);
		
		if (period != null) {
	        AttendanceDAO attendanceDAO = new AttendanceDAO();
	        double totalHours = 0.0;

	        switch (period) {
	            case "today":
	                totalHours = attendanceDAO.getTotalHoursToday(currentUser.getId());
	                break;
	            case "week":
	                totalHours = attendanceDAO.getTotalHoursThisWeek(currentUser.getId());
	                break;
	            case "month":
	                totalHours = attendanceDAO.getTotalHoursThisMonth(currentUser.getId());
	                break;
	            default:
	                totalHours = 0.0;
	                break;
	        }

	        request.setAttribute("totalHours", totalHours);
	    }

        List<Attendance> allRecords = attendanceDAO.findByUserId(user.getId());

        int page = 1;
        int recordsPerPage = 5;
        try {
            page = Integer.parseInt(request.getParameter("page"));
        } catch (NumberFormatException e) {
            // 無指定なら1ページ目
        }

        int start = (page - 1) * recordsPerPage;
        int end = Math.min(start + recordsPerPage, allRecords.size());

        List<Attendance> pageRecords = allRecords.stream()
                .sorted((a, b) -> b.getCheckInTime().compareTo(a.getCheckInTime()))
                .collect(Collectors.toList())
                .subList(start, end);

        int totalPages = (int) Math.ceil((double) allRecords.size() / recordsPerPage);

        request.setAttribute("attendanceRecords", pageRecords);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        RequestDispatcher rd = request.getRequestDispatcher("jsp/employee_menu.jsp");
        rd.forward(request, response);
    }
}
