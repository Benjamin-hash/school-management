package com.school.dao;

import com.school.database.Database;
import com.school.models.FeeStructure;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeeStructureDAO {

    // Fetch distinct levels from classes (e.g., JHS 1, JHS 2, JHS 3)
    public static List<String> getAllLevels() {
        List<String> levels = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT DISTINCT level FROM classes ORDER BY level_order")) {
            while (rs.next()) levels.add(rs.getString("level"));
        } catch (SQLException e) { e.printStackTrace(); }
        return levels;
    }

    public static List<FeeStructure> getAll() {
        List<FeeStructure> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT level, term, amount FROM fee_structures ORDER BY level, term")) {
            while (rs.next()) {
                list.add(new FeeStructure(rs.getString("level"), rs.getInt("term"), rs.getDouble("amount")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static void saveOrUpdate(String level, int term, double amount) {
        String sql = "INSERT INTO fee_structures (level, term, amount) VALUES (?, ?, ?) " +
                "ON CONFLICT(level, term) DO UPDATE SET amount = excluded.amount";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, level);
            ps.setInt(2, term);
            ps.setDouble(3, amount);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}