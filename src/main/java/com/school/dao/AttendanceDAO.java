package com.school.dao;

import com.school.database.Database;
import com.school.models.AttendanceRecord;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    /**
     * Fetches the class roster and their attendance for a specific date.
     * If no attendance is found for a student, it defaults to 'PRESENT'.
     */
    public static List<AttendanceRecord> getRoster(int classId, String date) {
        List<AttendanceRecord> roster = new ArrayList<>();
        // LEFT JOIN ensures we get all students, even if they don't have an attendance record yet
        String sql = "SELECT s.id, s.first_name || ' ' || s.last_name AS full_name, a.status " +
                "FROM students s " +
                "LEFT JOIN attendance a ON a.student_id = s.id AND a.date = ? " +
                "WHERE s.class_id = ? AND s.status = 'active' " +
                "ORDER BY s.last_name, s.first_name";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date);
            ps.setInt(2, classId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String status = rs.getString("status");
                if (status == null) status = "PRESENT"; // Default to PRESENT
                roster.add(new AttendanceRecord(rs.getInt("id"), rs.getString("full_name"), status));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return roster;
    }

    /**
     * Saves or Updates attendance.
     * Uses SQLite's ON CONFLICT to update if the student+date already exists.
     */
    public static void saveAttendance(int studentId, String date, String status, int markedBy) {
        String sql = "INSERT INTO attendance (student_id, date, status, marked_by) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT(student_id, date) " +
                "DO UPDATE SET status = excluded.status, marked_by = excluded.marked_by";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setString(2, date);
            ps.setString(3, status);
            ps.setInt(4, markedBy);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    public static String getStudentAttendanceSummary(int studentId) {
        int present = 0, absent = 0, late = 0;
        String sql = "SELECT status, COUNT(*) AS count FROM attendance WHERE student_id = ? GROUP BY status";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                if ("PRESENT".equals(status)) present = count;
                else if ("ABSENT".equals(status)) absent = count;
                else if ("LATE".equals(status)) late = count;
            }
        } catch (SQLException e) { e.printStackTrace(); }

        return "Present: " + present + " | Absent: " + absent + " | Late: " + late;
    }

    public static int[] getSchoolAttendanceStats() {
        int[] stats = new int[3]; // [Present, Absent, Late]
        String sql = "SELECT status, COUNT(*) as count FROM attendance GROUP BY status";
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                if ("PRESENT".equals(status)) stats[0] = count;
                else if ("ABSENT".equals(status)) stats[1] = count;
                else if ("LATE".equals(status)) stats[2] = count;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return stats;
    }
}