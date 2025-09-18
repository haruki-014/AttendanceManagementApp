package com.example.attendance.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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

    private static final List<Attendance> attendanceRecords = new CopyOnWriteArrayList<>();

    public Boolean checkIn(Integer userId) {
    	if (hasActiveAttendance(userId)) {
            return false;
        }
    	
        String insertSql = "Insert INTO attendance (user_id, check_in_time) values (?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
        		PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
        	
        	insertStmt.setInt(1, userId);
        	insertStmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
        	insertStmt.executeUpdate();
        	return true;
            
        } catch (SQLException e) {
           e.printStackTrace();
        }
        return false;
    }
    
    public boolean hasActiveAttendance(Integer userId) {
        String sql = "SELECT COUNT(*) FROM attendance WHERE user_id = ? AND check_out_time IS NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public void checkOut(Integer userId) {
    	String sql = "WITH latest AS (" +
	    	        "    SELECT id FROM attendance " +
	    	        "    WHERE user_id = ? AND check_out_time IS NULL " +
	    	        "    ORDER BY check_in_time DESC " +
	    	        "    LIMIT 1" +
	    	        ") " +
	    	        "UPDATE attendance " +
	    	        "SET check_out_time = ? " +
	    	        "WHERE id IN (SELECT id FROM latest)";
//    			"UPDATE attendance " +
//	            "SET check_out_time = ? " +
//	            "WHERE id = (" +
//	            "    SELECT id FROM attendance " +
//	            "    WHERE user_id = ? AND check_out_time IS NULL " +
//	            "    ORDER BY check_in_time DESC " +
//	            "    LIMIT 1" +
//	            ")";


	   try (Connection conn = DBConnection.getConnection();
	        PreparedStatement stmt = conn.prepareStatement(sql)) {
	
	       stmt.setInt(1, userId);
	       stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
	
	       stmt.executeUpdate();
	   } catch (SQLException e) {
	       e.printStackTrace();
	   }
   }

    public List<Attendance> findByUserId(Integer userId) {
        List<Attendance> records = new ArrayList<>();
        String sql = "SELECT id, user_id, check_in_time, check_out_time " +
                     "FROM attendance WHERE user_id = ? " +
                     "ORDER BY check_in_time DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapToAttendance(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("findByUserId->records: " +records);
        return records;
    }

    private Attendance mapToAttendance(ResultSet rs) throws SQLException {
        Attendance att = new Attendance();
        att.setId(rs.getInt("id"));
        att.setUserId(rs.getInt("user_id"));
        Timestamp checkIn = rs.getTimestamp("check_in_time");
        Timestamp checkOut = rs.getTimestamp("check_out_time");
        att.setCheckInTime(checkIn != null ? checkIn.toLocalDateTime() : null);
        att.setCheckOutTime(checkOut != null ? checkOut.toLocalDateTime() : null);
        return att;
    }


    public List<Attendance> findAll() {
        List<Attendance> records = new ArrayList<>();
        
        String sql = "SELECT * FROM attendance ORDER BY check_in_time DESC";
        
        try (Connection conn = DBConnection.getConnection();
        		PreparedStatement stmt = conn.prepareStatement(sql);
        		ResultSet rs = stmt.executeQuery()) {
        	
        	while (rs.next()) {
        		Attendance att = mapToAttendance(rs);
        		records.add(att);
        	}
        	
        } catch (SQLException e) {
        	e.printStackTrace();
        }
        
        return records;
    }

    public List<Attendance> findFilteredRecords(Integer userId, LocalDate startDate, LocalDate endDate) {
        List<Attendance> records = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder("SELECT * FROM attendance WHERE 1=1");
        
        if (userId != null) {
            sql.append(" AND user_id = ?");
        }
        if (startDate != null) {
            sql.append(" AND check_in_time >= ?");
        }
        if (endDate != null) {
            sql.append(" AND check_out_time <= ?");
        }
        
        sql.append(" ORDER BY check_in_time DESC");
        
        try (Connection conn = DBConnection.getConnection();
        		PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
        	
        	int paramIndex = 1;
        	if (userId != null) {
                stmt.setInt(paramIndex++, userId);
            }
            if (startDate != null) {
                stmt.setTimestamp(paramIndex++, Timestamp.valueOf(startDate.atStartOfDay()));
            }
            if (endDate != null) {
                stmt.setTimestamp(paramIndex++, Timestamp.valueOf(endDate.atTime(23, 59, 59)));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapToAttendance(rs));
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return records;
    }

    public Map<YearMonth, Long> getMonthlyWorkingHours(String userId) {
        return attendanceRecords.stream()
                .filter(att -> userId == null || userId.isEmpty() || att.getUserId().equals(userId))
                .filter(att -> att.getCheckInTime() != null && att.getCheckOutTime() != null)
                .collect(Collectors.groupingBy(
                        att -> YearMonth.from(att.getCheckInTime()),
                        Collectors.summingLong(att ->
                                ChronoUnit.HOURS.between(att.getCheckInTime(), att.getCheckOutTime())
                        )
                ));
    }

    public Map<YearMonth, Long> getMonthlyCheckInCounts(String userId) {
        return attendanceRecords.stream()
                .filter(att -> userId == null || userId.isEmpty() || att.getUserId().equals(userId))
                .filter(att -> att.getCheckInTime() != null)
                .collect(Collectors.groupingBy(
                        att -> YearMonth.from(att.getCheckInTime()),
                        Collectors.counting()
                ));
    }

    public void addManualAttendance(Integer userId, LocalDateTime checkIn, LocalDateTime checkOut) {
        Attendance newRecord = new Attendance(userId);
        newRecord.setCheckInTime(checkIn);
        newRecord.setCheckOutTime(checkOut);
        attendanceRecords.add(newRecord);
    }

    public boolean updateManualAttendance(String userId, LocalDateTime oldCheckIn, LocalDateTime oldCheckOut,
                                          LocalDateTime newCheckIn, LocalDateTime newCheckOut) {
        for (int i = 0; i < attendanceRecords.size(); i++) {
            Attendance att = attendanceRecords.get(i);

            if (att.getUserId().equals(userId) &&
                    att.getCheckInTime().equals(oldCheckIn) &&
                    (att.getCheckOutTime() == null ? oldCheckOut == null : att.getCheckOutTime().equals(oldCheckOut))) {
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
