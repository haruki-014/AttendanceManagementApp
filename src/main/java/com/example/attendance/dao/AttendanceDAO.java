package com.example.attendance.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.example.attendance.dto.Attendance;
import com.example.attendance.util.DBConnection;

public class AttendanceDAO {
	
	/* ConcurrentLinkedQueue, Collections.synchronizedList(new ArrayList<> */
	private static final List<Attendance> attendanceRecords = new CopyOnWriteArrayList<>();
	
	public void checkIn(Integer userId) {
		Attendance attendance = new Attendance(userId);
		attendance.setCheck_in_time(LocalDateTime.now());
		attendanceRecords.add(attendance);
	}
	
	public void checkOut(String userId) {
		attendanceRecords.stream()
				.filter(att -> userId.equals(att.getUserId()) && att.getCheck_out_time() == null) 
				.findFirst()
				.ifPresent(att -> att.setCheck_out_time(LocalDateTime.now()));
	}
	
	public List<Attendance> findByUserId(Integer userId) {
	    List<Attendance> records = new ArrayList<>();
	    String sql = "SELECT * FROM attendance WHERE user_id = ? ORDER BY check_in_time DESC";

	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        
	        stmt.setInt(1, userId);

	        try (ResultSet rs = stmt.executeQuery()) {
	            while (rs.next()) {
	                Attendance att = new Attendance();
	                att.setId(rs.getInt("id"));
	                att.setUserId(rs.getInt("user_id"));
	                att.setCheck_in_time(rs.getTimestamp("check_in_time") != null ? rs.getTimestamp("check_in_time").toLocalDateTime() : null);
	                att.setCheck_out_time(rs.getTimestamp("check_out_time") != null ? rs.getTimestamp("check_out_time").toLocalDateTime() : null);
	                records.add(att);
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return records;
	}

	
//	public List<Attendance> findByUserId(String userId) {
//		return attendanceRecords.stream()
//				.filter(att -> userId.equals(att.getUserId()))
//				.collect(Collectors.toList());
//	}
	
	public List<Attendance> findAll() {
		return new ArrayList<>(attendanceRecords);
	}
	
	public List<Attendance> findFilteredRecords(String userId, LocalDate startDate, LocalDate endDate) {
		return attendanceRecords.stream()
				.filter(att -> userId == null || userId.isEmpty() || att.getUserId().equals(userId))
				.filter(att ->
						startDate == null || (
						att.getCheck_in_time() != null && att.getCheck_in_time().toLocalDate().isBefore(startDate))
						)
				.filter(att ->
						endDate == null || (
						att.getCheck_out_time() != null && att.getCheck_out_time().toLocalDate().isAfter(endDate))
						)
				.collect(Collectors.toList());
	}
	
	public Map<YearMonth, Long> getMonthlyWorkingHours(String userId) {
		return attendanceRecords.stream()
//				.filter(att -> userId == null || userId.isEmpty() || att.getUserId().equals(userId))
//				.filter(att -> att.getCheckInTime() != null)
				.filter(att -> userId == null || userId.isEmpty() || att.getUserId().equals(userId))
	            .filter(att -> att.getCheckInTime() != null && att.getCheckOutTime() != null)
				.collect(Collectors.groupingBy(
						att -> YearMonth.from(att.getCheckInTime()),
						Collectors.summingLong(att ->
										ChronoUnit.HOURS.between(
										att.getCheckInTime(),
										att.getCheckOutTime()
									)
								)
						)
				);
	}
	
	public Map<YearMonth, Long> getMonthlyCheckInCounts(String userId){
		return attendanceRecords.stream()
				.filter(att -> userId == null || userId.isEmpty() || att.getUserId().equals(userId))
				.filter(att -> att.getCheckInTime() != null)
				.collect(Collectors.groupingBy(att ->
						YearMonth.from(att.getCheckInTime()),
						Collectors.counting()
						)
				);
	}
	
	
	
	public void addManualAttendance(String userId, LocalDateTime checkIn, LocalDateTime checkOut) {
		Attendance newRecord = new Attendance(userId);
		
		newRecord.setCheck_in_time(checkIn);
		newRecord.setCheck_out_time(checkOut);
		
		attendanceRecords.add(newRecord);
	}
	
	public boolean updateManualAttendance(String userId, LocalDateTime oldCheckIn, LocalDateTime oldCheckOut,
			LocalDateTime newCheckIn, LocalDateTime newCheckOut) {
		for (int i = 0; i < attendanceRecords.size(); i++) {
			Attendance att = attendanceRecords.get(i);
			
			if (
				att.getUserId().equals(userId) &&
				att.getCheck_in_time().equals(oldCheckIn) &&
				(att.getCheck_out_time() == null ? oldCheckOut == null : att.getCheck_out_time().equals(oldCheckOut))
				) {
				att.setCheck_in_time(newCheckIn);
				att.setCheck_out_time(newCheckOut);
				return true;
			}
		}
		return false;
	}
	
	public boolean deleteManualAttendance(String userId, LocalDateTime checkIn, LocalDateTime checkOut) {
		return attendanceRecords.removeIf(att ->
				att.getUserId().equals(userId) &&
				att.getCheck_in_time().equals(checkIn) &&
				(att.getCheck_out_time() == null ? checkOut == null : att.getCheck_out_time().equals(checkOut))
				);

	}
	
}
