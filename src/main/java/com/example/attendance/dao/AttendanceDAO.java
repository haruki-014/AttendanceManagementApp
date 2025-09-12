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
	
	/* ローカル管理時 */
	private static final List<Attendance> attendanceRecords = new CopyOnWriteArrayList<>();
	
	public void checkIn(String userId) {
		
		/* ローカル管理時 */
//		Attendance attendance = new Attendance(userId);
//		attendance.setCheckInTime(LocalDateTime.now());
//		attendanceRecords.add(attendance);
		
		String sql = "INSERT INTO attendance (user_name, check_in_time) VALUES (?, ?)";
		
		try (Connection conn = DBConnection.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, userId);
			ps.setObject(2, LocalDateTime.now());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public void checkOut(String userId) {
		
		/* ローカル管理時 */
//		attendanceRecords.stream()
//				.filter(att -> userId.equals(att.getUserId()) && att.getCheckOutTime() == null) 
//				.findFirst()
//				.ifPresent(att -> att.setCheckOutTime(LocalDateTime.now()));
		
        String selectSql = """
                SELECT id FROM attendance
                WHERE user_name = ? AND check_out_time IS NULL
                ORDER BY check_in_time DESC
                LIMIT 1
                """;
        String updateSql = "UPDATE attendance SET check_out_time = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement selectPs = conn.prepareStatement(selectSql)) {

            selectPs.setString(1, userId);
            ResultSet rs = selectPs.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                    updatePs.setObject(1, LocalDateTime.now());
                    updatePs.setInt(2, id);
                    updatePs.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
		
	}
	
	public List<Attendance> findByUserId(String userId) {
		return attendanceRecords.stream()
				.filter(att -> userId.equals(att.getUserId()))
				.collect(Collectors.toList());
	}
	
	public List<Attendance> findAll() {
		return new ArrayList<>(attendanceRecords);
	}
	
	public List<Attendance> findFilteredRecords(String userId, LocalDate startDate, LocalDate endDate) {
		return attendanceRecords.stream()
				.filter(att -> userId == null || userId.isEmpty() || att.getUserId().equals(userId))
				.filter(att ->
						startDate == null || (
						att.getCheckInTime() != null && att.getCheckInTime().toLocalDate().isBefore(startDate))
						)
				.filter(att ->
						endDate == null || (
						att.getCheckOutTime() != null && att.getCheckOutTime().toLocalDate().isAfter(endDate))
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
		
		newRecord.setCheckInTime(checkIn);
		newRecord.setCheckOutTime(checkOut);
		
		attendanceRecords.add(newRecord);
	}
	
	public boolean updateManualAttendance(String userId, LocalDateTime oldCheckIn, LocalDateTime oldCheckOut,
			LocalDateTime newCheckIn, LocalDateTime newCheckOut) {
		for (int i = 0; i < attendanceRecords.size(); i++) {
			Attendance att = attendanceRecords.get(i);
			
			if (
				att.getUserId().equals(userId) &&
				att.getCheckInTime().equals(oldCheckIn) &&
				(att.getCheckOutTime() == null ? oldCheckOut == null : att.getCheckOutTime().equals(oldCheckOut))
				) {
				att.setCheckInTime(newCheckIn);
				att.setCheckOutTime(newCheckOut);
				return true;
			}
		}
		return false;
	}
	
	public boolean deleteManualAttendance(String userId, LocalDateTime checkIn, LocalDateTime checkOut) {
		return attendanceRecords.removeIf(att ->
				att.getUserId().equals(userId) &&
				att.getCheckInTime().equals(checkIn) &&
				(att.getCheckOutTime() == null ? checkOut == null : att.getCheckOutTime().equals(checkOut))
				);

	}
	
}
