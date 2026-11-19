package com.school.dao;

import com.school.database.Database;
import com.school.models.ReportCardRow;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportCardDAO {
    public static List<ReportCardRow> getReportCard(int studentId, int term, String year) {
        List<ReportCardRow> list = new ArrayList<>();

        // DEBUG PRINT: This will show in your IDE console!
        System.out.println("LOOKING FOR GRADES -> Student ID: " + studentId + " | Term: " + term + " | Year: " + year);

        String sql = "SELECT s.name AS subject_name, g.score " +
                "FROM grades g " +
                "JOIN subjects s ON s.id = g.subject_id " +
                "WHERE g.student_id = ? AND g.term = ? AND g.academic_year = ? " +
                "ORDER BY s.name";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, term);
            ps.setString(3, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ReportCardRow(rs.getString("subject_name"), rs.getDouble("score")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}