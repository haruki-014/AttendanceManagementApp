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

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        RequestDispatcher rd = request.getRequestDispatcher("/login.jsp");
        rd.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String userIdStr = request.getParameter("userId");
        String password = request.getParameter("password");

        // 入力チェック
        if (userIdStr == null || password == null) {
            request.setAttribute("errorMessage", "ユーザーIDとパスワードを入力してください。");
            RequestDispatcher rd = request.getRequestDispatcher("/login.jsp");
            rd.forward(request, response);
            return;
        }

        Integer userId = null;
        try {
            userId = Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "ユーザーIDは数字で入力してください。");
            RequestDispatcher rd = request.getRequestDispatcher("/login.jsp");
            rd.forward(request, response);
            return;
        }

        User user = userDAO.findById(userId);

        if (user != null && user.isEnabled() && userDAO.verifyPassword(user.getId(), password)) {
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            request.setAttribute("successMessage", "ログインしました");

            // 管理者の場合
            if ("admin".equals(user.getRole())) {
                request.setAttribute("allAttendanceRecords", attendanceDAO.findAll());

                Map<Integer, Long> totalHoursByUser = attendanceDAO.findAll().stream()
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

            // 従業員の場合
            } else {
                request.setAttribute(
                        "attendanceRecords",
                        attendanceDAO.findByUserId(user.getId())
                );
                RequestDispatcher rd = request.getRequestDispatcher("jsp/employee_menu.jsp");
                rd.forward(request, response);
            }

        } else {
            request.setAttribute("errorMessage", "ユーザーIDまたはパスワードが不正、あるいはアカウントが無効です。");
            RequestDispatcher rd = request.getRequestDispatcher("/login.jsp");
            rd.forward(request, response);
        }
    }
}
