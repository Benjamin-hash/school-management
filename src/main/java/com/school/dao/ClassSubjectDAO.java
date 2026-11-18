package com.school.dao;

import com.school.database.Database;
import com.school.models.ClassSubject;
import com.school.models.Subject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClassSubjectDAO {

    // Uses JOINs to get names instead of just IDs for display
    public static List<ClassSubject> getAll() {
        List<ClassSubject> list = new ArrayList<>();
        String sql = "SELECT cs.id, c.name AS c_name, s.name AS s_name, t.first_name || ' ' || t.last_name AS t_name " +
                "FROM class_subjects cs " +
                "JOIN classes c ON c.id = cs.class_id " +
                "JOIN subjects s ON s.id = cs.subject_id " +
                "JOIN teachers t ON t.id = cs.teacher_id";
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new ClassSubject(rs.getInt("id"), rs.getString("c_name"), rs.getString("s_name"), rs.getString("t_name")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static void assign(int classId, int subjectId, int teacherId) {
        String sql = "INSERT INTO class_subjects (class_id, subject_id, teacher_id) VALUES (?, ?, ?) " +
                "ON CONFLICT(class_id, subject_id) DO UPDATE SET teacher_id = excluded.teacher_id";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId); ps.setInt(2, subjectId); ps.setInt(3, teacherId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // THIS IS THE METHOD CAUSING THE RED ERROR - NOW ADDED HERE
    public static void assignTeacherToAllSubjects(int classId, int teacherId) {
        String selectSql = "SELECT id FROM subjects";
        String insertSql = "INSERT INTO class_subjects (class_id, subject_id, teacher_id) VALUES (?, ?, ?) " +
                "ON CONFLICT(class_id, subject_id) DO UPDATE SET teacher_id = excluded.teacher_id";

        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(selectSql);
             PreparedStatement ps = conn.prepareStatement(insertSql)) {

            while (rs.next()) {
                ps.setInt(1, classId);
                ps.setInt(2, rs.getInt("id"));
                ps.setInt(3, teacherId);
                ps.addBatch();
            }
            ps.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Subject> getSubjectsForTeacherInClass(int teacherId, int classId) {
        List<Subject> list = new ArrayList<>();

        // First, check if the teacher is the homeroom teacher for this class
        boolean isHomeroom = false;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT homeroom_teacher_id FROM classes WHERE id = ?")) {
            ps.setInt(1, classId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) == teacherId) isHomeroom = true;
        } catch (SQLException e) { }

        // If Homeroom, give them ALL subjects. If not, give them only assigned subjects.
        String sql = isHomeroom
                ? "SELECT id, name FROM subjects"
                : "SELECT s.id, s.name FROM subjects s JOIN class_subjects cs ON cs.subject_id = s.id WHERE cs.teacher_id = ? AND cs.class_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (!isHomeroom) {
                ps.setInt(1, teacherId);
                ps.setInt(2, classId);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Subject(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}