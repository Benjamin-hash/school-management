package com.school.dao;

import com.school.database.Database;
import java.sql.*;

public class SettingsDAO {

    public static String getActiveYear() {
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT current_academic_year FROM school_settings WHERE id = 1")) {
            if (rs.next() && rs.getString(1) != null) return rs.getString(1);
        } catch (SQLException e) { }
        // Fail-safe
        int year = java.time.LocalDate.now().getYear();
        return year + "/" + (year + 1);
    }

    public static int getActiveTerm() {
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT current_term FROM school_settings WHERE id = 1")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { }
        return 1; // Fail-safe
    }

    public static void updateSettings(String year, int term) {
        String sql = "UPDATE school_settings SET current_academic_year = ?, current_term = ? WHERE id = 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, year);
            ps.setInt(2, term);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}