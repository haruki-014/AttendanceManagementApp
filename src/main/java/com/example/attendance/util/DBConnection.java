package com.example.attendance.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;



public class DBConnection {
	private static final String URL = "jdbc:postgresql://localhost:5432/attendance_db";
	private static final String USER = "postgres";
	private static final String PASSWORD = "postgres";
	
	static {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("PostgreSQL JDBC Driver not found", e);
		}
	}
	
	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(URL, USER, PASSWORD);
	}

}
