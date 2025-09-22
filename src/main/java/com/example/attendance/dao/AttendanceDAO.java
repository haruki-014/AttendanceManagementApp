package com.example.attendance.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.attendance.dto.Attendance;
import com.example.attendance.dto.OverTimeReport;
import com.example.attendance.dto.Position;
import com.example.attendance.util.DBConnection;

public class AttendanceDAO {

//    private static final List<Attendance> attendanceRecords = new CopyOnWriteArrayList<>();

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

    public Map<YearMonth, Long> getMonthlyWorkingHours(Integer userId) {
        Map<YearMonth, Long> result = new HashMap<>();

        String sql = "SELECT DATE_TRUNC('month', check_in_time) AS month, " +
                     "SUM(EXTRACT(EPOCH FROM (check_out_time - check_in_time)) / 3600) AS hours " +
                     "FROM attendance " +
                     "WHERE check_in_time IS NOT NULL " +
                     "AND check_out_time IS NOT NULL " +
                     (userId != null ? "AND user_id = ? " : "") +
                     "GROUP BY DATE_TRUNC('month', check_in_time) " +
                     "ORDER BY month";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (userId != null) {
                stmt.setInt(1, userId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp monthTs = rs.getTimestamp("month");
                    YearMonth ym = YearMonth.from(monthTs.toLocalDateTime());
                    long hours = rs.getLong("hours");
                    result.put(ym, hours);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }
    
    public Map<YearMonth, Long> getMonthlyCheckInCounts(Integer userId) {
        Map<YearMonth, Long> result = new HashMap<>();

        String sql = "SELECT DATE_TRUNC('month', check_in_time) AS month, " +
                     "       COUNT(DISTINCT DATE(check_in_time)) AS count " +
                     "FROM attendance " +
                     "WHERE check_in_time IS NOT NULL " +
                     (userId != null ? "AND user_id = ? " : "") +
                     "GROUP BY DATE_TRUNC('month', check_in_time) " +
                     "ORDER BY month";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (userId != null) {
                stmt.setInt(1, userId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp monthTs = rs.getTimestamp("month");
                    YearMonth ym = YearMonth.from(monthTs.toLocalDateTime());
                    long count = rs.getLong("count");
                    result.put(ym, count);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }



//    public Map<YearMonth, Long> getMonthlyCheckInCounts(Integer userId) {
//        Map<YearMonth, Long> result = new HashMap<>();
//
//        String sql = "SELECT DATE_TRUNC('month', check_in_time) AS month, COUNT(*) AS count " +
//                     "FROM attendance " +
//                     "WHERE check_in_time IS NOT NULL " +
//                     (userId != null ? "AND user_id = ? " : "") +
//                     "GROUP BY DATE_TRUNC('month', check_in_time) " +
//                     "ORDER BY month";
//
//        try (Connection conn = DBConnection.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//            if (userId != null) {
//                stmt.setInt(1, userId);
//            }
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                while (rs.next()) {
//                    Timestamp monthTs = rs.getTimestamp("month");
//                    YearMonth ym = YearMonth.from(monthTs.toLocalDateTime());
//                    long count = rs.getLong("count");
//                    result.put(ym, count);
//                }
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        return result;
//    }
    
    public double getTotalHoursToday(int userId) {
    	String sql = 
    			"SELECT SUM(EXTRACT(EPOCH FROM (check_out_time - check_in_time))/3600) AS hours " +
    			"FROM attendance " +
    			"WHERE user_id = ? AND DATE(check_in_time) = CURRENT_DATE";
    	return getTotalHours(userId, sql);
    }

    public double getTotalHoursThisWeek(int userId) {
        String sql =
        		"SELECT SUM(EXTRACT(EPOCH FROM (check_out_time - check_in_time))/3600) AS hours " +
                "FROM attendance " +
                "WHERE user_id = ? AND DATE(check_in_time) >= date_trunc('week', CURRENT_DATE) " +
                "AND DATE(check_in_time) < date_trunc('week', CURRENT_DATE) + INTERVAL '7 day'";
        return getTotalHours(userId, sql);
    }

    public double getTotalHoursThisMonth(int userId) {
        String sql =
        		"SELECT SUM(EXTRACT(EPOCH FROM (check_out_time - check_in_time))/3600) AS hours " +
                "FROM attendance " +
                "WHERE user_id = ? AND DATE_TRUNC('month', check_in_time) = DATE_TRUNC('month', CURRENT_DATE)";
        return getTotalHours(userId, sql);
    }

    private double getTotalHours(int userId, String sql) {
        try (Connection conn = DBConnection.getConnection();
        		PreparedStatement ps = conn.prepareStatement(sql)) {
        	ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                	return rs.getDouble("hours");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
    
 // 月間残業時間を計算（1日8時間を超えた分を残業として加算）
    public double getMonthlyOvertimeHours(int userId) {
        int standardHoursPerDay = 8;

        String sql =
            "SELECT SUM(GREATEST(daily.hours - ?, 0)) AS overtime " +
            "FROM (" +
            "    SELECT DATE(check_in_time) AS work_date, " +
            "           SUM(EXTRACT(EPOCH FROM (check_out_time - check_in_time)) / 3600) AS hours " +
            "    FROM attendance " +
            "    WHERE user_id = ? " +
            "    AND DATE_TRUNC('month', check_in_time) = DATE_TRUNC('month', CURRENT_DATE) " +
            "    AND check_out_time IS NOT NULL " +
            "    GROUP BY DATE(check_in_time)" +
            ") AS daily";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, standardHoursPerDay);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("overtime");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }




 // 勤怠レコードを手動追加
    public void addManualAttendance(Integer userId, LocalDateTime checkIn, LocalDateTime checkOut) {
        String sql = "INSERT INTO attendance (user_id, check_in_time, check_out_time) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setTimestamp(2, Timestamp.valueOf(checkIn));
            if (checkOut != null) {
                stmt.setTimestamp(3, Timestamp.valueOf(checkOut));
            } else {
                stmt.setNull(3, Types.TIMESTAMP);
            }

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<OverTimeReport> getOverTimeReports() {
        List<OverTimeReport> reports = new ArrayList<>();
        String sql = "SELECT id, name, position FROM users WHERE position IN (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);) {
        	
        	ps.setString(1, Position.COMPANY_EMPLOYEE.name());
        	ps.setString(2, Position.PART_TIME.name());
        	
        	try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	                OverTimeReport report = new OverTimeReport();
	                int userId = rs.getInt("id");
	                report.setUserId(userId);
	                report.setUserName(rs.getString("name"));
	                report.setPosition(rs.getString("position"));
	                report.setOvertimeHours(getMonthlyOvertimeHours(userId)); // 既存メソッド呼び出し
	                reports.add(report);
	            }
        	}

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reports;
    }


    // 勤怠レコードを手動更新
    public boolean updateManualAttendance(Integer userId, LocalDateTime oldCheckIn, LocalDateTime oldCheckOut,
                                          LocalDateTime newCheckIn, LocalDateTime newCheckOut) {
        String sql = "UPDATE attendance " +
                     "SET check_in_time = ?, check_out_time = ? " +
                     "WHERE user_id = ? AND check_in_time = ? " +
                     (oldCheckOut == null ? "AND check_out_time IS NULL" : "AND check_out_time = ?");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // 新しい値
            stmt.setTimestamp(1, Timestamp.valueOf(newCheckIn));
            if (newCheckOut != null) {
                stmt.setTimestamp(2, Timestamp.valueOf(newCheckOut));
            } else {
                stmt.setNull(2, Types.TIMESTAMP);
            }

            // 検索条件
            stmt.setInt(3, userId);
            stmt.setTimestamp(4, Timestamp.valueOf(oldCheckIn));
            if (oldCheckOut != null) {
                stmt.setTimestamp(5, Timestamp.valueOf(oldCheckOut));
            }

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 勤怠レコードを手動削除
    public boolean deleteManualAttendance(Integer userId, LocalDateTime checkIn, LocalDateTime checkOut) {
        String sql = "DELETE FROM attendance WHERE user_id = ? AND check_in_time = ? " +
                     (checkOut == null ? "AND check_out_time IS NULL" : "AND check_out_time = ?");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setTimestamp(2, Timestamp.valueOf(checkIn));
            if (checkOut != null) {
                stmt.setTimestamp(3, Timestamp.valueOf(checkOut));
            }

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
