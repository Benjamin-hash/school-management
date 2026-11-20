package com.school.dao;

import com.school.database.Database;
import com.school.models.ClassSubject;
import com.school.models.Subject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClassSubjectDAO {

    // 1. Get ALL assignments (This is the base method we know works)
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

    // 2. Filter for JHS in Java
    public static List<ClassSubject> getSecondaryAssignments() {
        List<ClassSubject> result = new ArrayList<>();
        for (ClassSubject cs : getAll()) {
            String className = cs.getClassName();
            if (className != null && (className.toUpperCase().contains("JHS") || className.toUpperCase().contains("SS"))) {
                result.add(cs);
            }
        }
        return result;
    }

    // 3. Filter for Primary in Java
    public static List<ClassSubject> getPrimaryAssignments() {
        List<ClassSubject> result = new ArrayList<>();
        for (ClassSubject cs : getAll()) {
            String className = cs.getClassName();
            if (className != null && !className.toUpperCase().contains("JHS") && !className.toUpperCase().contains("SS")) {
                result.add(cs);
            }
        }
        return result;
    }

    // 4. Assign a new subject
    public static void assign(int classId, int subjectId, int teacherId) {
        String sql = "INSERT INTO class_subjects (class_id, subject_id, teacher_id) VALUES (?, ?, ?) " +
                "ON CONFLICT(class_id, subject_id) DO UPDATE SET teacher_id = excluded.teacher_id";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId); ps.setInt(2, subjectId); ps.setInt(3, teacherId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // 5. Remove an assignment
    public static void removeAssignment(int id) {
        String sql = "DELETE FROM class_subjects WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // 6. Used by the Double-Click popup on the Teachers screen
    public static List<String> getAssignmentsForTeacher(int teacherId) {
        List<String> assignments = new ArrayList<>();
        String sql = "SELECT c.name AS class_name, s.name AS subject_name " +
                "FROM class_subjects cs " +
                "JOIN classes c ON c.id = cs.class_id " +
                "JOIN subjects s ON s.id = cs.subject_id " +
                "WHERE cs.teacher_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                assignments.add(rs.getString("class_name") + "  →  " + rs.getString("subject_name"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return assignments;
    }

    // 7. Used by GradeController to populate the dropdown
    public static List<Subject> getSubjectsForTeacherInClass(int teacherId, int classId) {
        List<Subject> list = new ArrayList<>();
        boolean isHomeroom = false;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT homeroom_teacher_id FROM classes WHERE id = ?")) {
            ps.setInt(1, classId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) == teacherId) isHomeroom = true;
        } catch (SQLException e) { }

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