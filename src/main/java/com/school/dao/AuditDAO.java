package com.school.dao;

import com.school.database.Database;
import java.sql.*;

public class AuditDAO {
    public static void log(int userId, String action, String details) {
        String sql = "INSERT INTO audit_logs (user_id, action, details) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, action);
            ps.setString(3, details);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}