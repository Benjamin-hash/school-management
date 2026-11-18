package com.school.dao;

import com.school.database.Database;
import com.school.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public static User findByCredentials(String username, String password) {
        String sql = "SELECT id, username, role, teacher_id FROM users WHERE username = ? AND password = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int tid = rs.getInt("teacher_id");
                return new User(rs.getInt("id"), rs.getString("username"), rs.getString("role"),
                        rs.wasNull() ? null : tid);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    // Fetch all users for the Admin table
    public static List<User> getAll() {
        List<User> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, username, role, teacher_id FROM users")) {
            while (rs.next()) {
                int tid = rs.getInt("teacher_id");
                list.add(new User(rs.getInt("id"), rs.getString("username"), rs.getString("role"), rs.wasNull() ? null : tid));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // Create a new Admin account
    public static boolean createAdmin(String username, String password) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'ADMIN')";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false; // Returns false if username is already taken
        }
    }

    // Reset a user's password
    public static void resetPassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}