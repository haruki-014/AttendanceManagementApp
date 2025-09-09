package com.example.attendance.controller;

import java.io.IOException;
import java.sql.Connection;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.example.attendance.util.DBConnection;

@WebServlet("/dbtest")
public class test extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain; charset=UTF-8");
        try (Connection conn = DBConnection.getConnection()) {
            resp.getWriter().println("✅ DB接続成功: " + (conn != null));
        } catch (Exception e) {
            resp.getWriter().println("❌ DB接続失敗: " + e.getMessage());
        }
    }
}
