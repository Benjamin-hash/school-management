package com.school.dao;

import com.school.database.Database;
import com.school.models.GradeRecord;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GradeDAO {

    public static List<GradeRecord> getGradesForClass(int classId, int subjectId, int term, String year) {
        List<GradeRecord> list = new ArrayList<>();
        // LEFT JOIN to get all students, even if they don't have a grade yet
        String sql = "SELECT s.id, s.first_name || ' ' || s.last_name AS full_name, g.score " +
                "FROM students s " +
                "LEFT JOIN grades g ON g.student_id = s.id AND g.subject_id = ? AND g.term = ? AND g.academic_year = ? " +
                "WHERE s.class_id = ? AND s.status = 'active' " +
                "ORDER BY s.last_name, s.first_name";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            ps.setInt(2, term);
            ps.setString(3, year);
            ps.setInt(4, classId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                double score = rs.getDouble("score");
                if (rs.wasNull()) score = 0.0; // Default to 0 if no grade exists
                list.add(new GradeRecord(rs.getInt("id"), rs.getString("full_name"), score));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static void saveGrade(int studentId, int subjectId, int term, String year, double score, int enteredBy) {
        String sql = "INSERT INTO grades (student_id, subject_id, term, academic_year, score, entered_by) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT(student_id, subject_id, term, academic_year) " +
                "DO UPDATE SET score = excluded.score, entered_by = excluded.entered_by";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            ps.setInt(3, term);
            ps.setString(4, year);
            ps.setDouble(5, score);
            ps.setInt(6, enteredBy);
            // Add this inside saveGrade (assuming you pass user ID):
            AuditDAO.log(enteredBy, "GRADE_UPDATE", "Updated grade for Student ID " + studentId + " to " + score);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}