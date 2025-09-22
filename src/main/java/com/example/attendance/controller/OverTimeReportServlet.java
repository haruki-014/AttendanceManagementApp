package com.example.attendance.controller;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.example.attendance.dao.AttendanceDAO;
import com.example.attendance.dto.OverTimeReport;

@WebServlet("/overtimeReport")
public class OverTimeReportServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        AttendanceDAO dao = new AttendanceDAO();
        List<OverTimeReport> reports = dao.getOverTimeReports();
        System.out.println("取得したレポート件数: " + reports.size());
        
        req.setAttribute("reports", reports);
        req.getRequestDispatcher("jsp/admin_menu.jsp").forward(req, resp);
    }
}
