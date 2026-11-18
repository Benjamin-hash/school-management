package com.school.dao;

import com.school.database.Database;
import com.school.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public static User findByCredentials(String username, String password) {
        // 1. Only search by username
        String sql = "SELECT id, username, password, role, teacher_id FROM users WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");

                // 2. Use BCrypt to check if the typed password matches the hash
                if (com.school.utils.PasswordUtils.checkPassword(password, storedHash)) {
                    int tid = rs.getInt("teacher_id");
                    return new User(rs.getInt("id"), rs.getString("username"), rs.getString("role"),
                            rs.wasNull() ? null : tid);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Login failed
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
    public static boolean createAdmin(String username, String plainPassword) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'ADMIN')";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            // Hash the password before saving
            ps.setString(2, com.school.utils.PasswordUtils.hashPassword(plainPassword));
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    // Reset a user's password
    public static void resetPassword(int userId, String newPlainPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // Hash the new password before saving
            ps.setString(1, com.school.utils.PasswordUtils.hashPassword(newPlainPassword));
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}