package com.example.attendance.dao;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.example.attendance.dto.User;
import com.example.attendance.util.DBConnection;

public class UserDAO {

    // ====== 【変更前】メモリ内保持 -> util.DBConnectionへ ======
    /*
    private static final Map<String, User> users = new HashMap<>();
    static {
        users.put("employee1", new User("employee1", hashPassword("password"), "employee", true));
        users.put("admin1", new User("admin1", hashPassword("adminpass"), "admin", true));
        users.put("employee2", new User("employee2", hashPassword("password"), "employee", true));
    }
    */

    // ====== 【変更前】DriverManagerで接続 ======
    /*
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    */

    // ====== 【変更後】DBConnectionユーティリティを利用 ======
    private Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    // ====== findByUserName ======
    // 【変更前】メモリ内検索
    /*
    public User findByUserName(String userName) {
        return users.get(userName);
    }
    */
    // 【変更後】DB検索
    public User findById(Integer id) {
        String sql = "SELECT id, name, password, role, is_enabled FROM users WHERE user_name = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                    	rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getBoolean("is_enabled")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean verifyPassword(Integer id, String password) {
        User user = findById(id);
        return user != null && user.isEnabled() && user.getPassword().equals(hashPassword(password));
    }

    // ====== getAllUsers ======
    // 【変更前】メモリ内取得
    /*
    public Collection<User> getAllUsers() {
        return users.values();
    }
    */
    // 【変更後】DB取得
    public Collection<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT name, password, role, is_enabled FROM users";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(new User(
	                	rs.getInt("id"),
	                    rs.getString("name"),
	                    rs.getString("password"),
	                    rs.getString("role"),
	                    rs.getBoolean("is_enabled")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // ====== addUser ======
    // 【変更前】メモリ内追加
    /*
    public void addUser(User user) {
        users.put(user.getUserName(), user);
    }
    */
    // 【変更後】DB追加
    public void addUser(User user) {
        String sql = "INSERT INTO users (name, password, role, is_enabled) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
        	ps.setInt(1, user.getId());
            ps.setString(2, user.getName());
            ps.setString(3, hashPassword(user.getPassword()));
            ps.setString(4, user.getRole());
            ps.setBoolean(5, user.isEnabled());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ====== updateUser ======
    /*
    public void updateUser(User user) {
        users.put(user.getUserName(), user);
    }
    */
    public void updateUser(User user) {
        String sql = "UPDATE users SET password = ?, role = ?, is_enabled = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashPassword(user.getPassword()));
            ps.setString(2, user.getRole());
            ps.setBoolean(3, user.isEnabled());
            ps.setInt(4, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ====== deleteUser ======
    /*
    public void deleteUser(String userName) {
        users.remove(userName);
    }
    */
    public void deleteUser(Integer id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ====== resetPassword ======
    /*
    public void resetPassword(String userName, String newPassword) {
        User user = users.get(userName);
        if (user != null) {
            users.put(userName, new User(user.getUserName(), hashPassword(newPassword), user.getRole(), user.isEnabled()));
        }
    }
    */
    public void resetPassword(Integer id, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashPassword(newPassword));
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ====== toggleUserEnabled ======
    /*
    public void toggleUserEnabled(String userName, boolean isEnabled) {
        User user = users.get(userName);
        if (user != null) {
            users.put(userName, new User(user.getUserName(), user.getPassword(), user.getRole(), isEnabled));
        }
    }
    */
    public void toggleUserEnabled(Integer id, boolean isEnabled) {
        String sql = "UPDATE users SET is_enabled = ? WHERE user_name = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isEnabled);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ====== hashPassword（共通） ======
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
