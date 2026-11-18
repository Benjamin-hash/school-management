package com.school.dao;

import com.school.database.Database;
import com.school.models.SchoolClass;
import com.school.utils.PasswordUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherDAO {

    /**
     * The query that makes each teacher's dashboard personal:
     * only classes where THIS teacher is assigned in class_subjects.
     */
    public static java.util.List<SchoolClass> getClassesForTeacher(int teacherId) {
        java.util.List<SchoolClass> classes = new java.util.ArrayList<>();

        // This query checks if they are assigned to a subject OR if they are the homeroom teacher
        String sql = "SELECT DISTINCT c.id, c.name, c.level, c.section, c.level_order, c.homeroom_teacher_id, "
                + "(SELECT COUNT(*) FROM students WHERE students.class_id = c.id AND students.status = 'active') AS student_count "
                + "FROM classes c "
                + "LEFT JOIN class_subjects cs ON c.id = cs.class_id "
                + "WHERE cs.teacher_id = ? OR c.homeroom_teacher_id = ? "
                + "ORDER BY c.level_order, c.section";

        try (java.sql.Connection conn = com.school.database.Database.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ps.setInt(2, teacherId); // Pass teacherId twice for the OR condition
            java.sql.ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int hrId = rs.getInt("homeroom_teacher_id");
                Integer homeroomId = rs.wasNull() ? null : hrId;

                classes.add(new SchoolClass(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("level"),
                        rs.getString("section"),
                        rs.getInt("level_order"),
                        rs.getInt("student_count"),
                        homeroomId // Pass the new homeroom ID
                ));
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return classes;
    }
    /*public static int count() {
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM teachers")) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }*/

    public static java.util.List<com.school.models.Teacher> getAll() {
        java.util.List<com.school.models.Teacher> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM teachers";
        try (java.sql.Connection conn = com.school.database.Database.getConnection();
             java.sql.Statement st = conn.createStatement();
             java.sql.ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new com.school.models.Teacher(rs.getInt("id"), rs.getString("first_name"),
                        rs.getString("last_name"), rs.getString("email"), rs.getString("phone")));
            }
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
        return list;
    }

    // Transaction: Insert Teacher AND create their User Login
    public static boolean insertTeacherWithUser(String fn, String ln, String email, String phone, String username, String password) {
        String sqlTeacher = "INSERT INTO teachers (first_name, last_name, email, phone) VALUES (?, ?, ?, ?)";
        String sqlUser = "INSERT INTO users (username, password, role, teacher_id) VALUES (?, ?, 'TEACHER', ?)";

        try (java.sql.Connection conn = com.school.database.Database.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            try (java.sql.PreparedStatement psT = conn.prepareStatement(sqlTeacher, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                psT.setString(1, fn); psT.setString(2, ln); psT.setString(3, email); psT.setString(4, phone);
                psT.executeUpdate();

                java.sql.ResultSet keys = psT.getGeneratedKeys();
                int teacherId = 0;
                if (keys.next()) teacherId = keys.getInt(1);

                try (java.sql.PreparedStatement psU = conn.prepareStatement(sqlUser)) {
                    psU.setString(1, username); psU.setString(2, com.school.utils.PasswordUtils.hashPassword(password)); psU.setInt(3, teacherId);
                    psU.executeUpdate();
                }
                conn.commit(); // Save both
                return true;
            } catch (java.sql.SQLException ex) {
                conn.rollback(); // Cancel both if username is taken, etc.
                return false;
            }
        } catch (java.sql.SQLException e) { return false; }
    }
    public static int count() {
        try (java.sql.Connection conn = com.school.database.Database.getConnection();
             java.sql.Statement st = conn.createStatement();
             java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM teachers")) {
            rs.next();
            return rs.getInt(1);
        } catch (java.sql.SQLException e) { e.printStackTrace(); return 0; }
    }
    public static int getStudentCountForTeacher(int teacherId) {
        String sql = "SELECT COUNT(DISTINCT s.id) FROM students s " +
                "JOIN classes c ON c.id = s.class_id " +
                "LEFT JOIN class_subjects cs ON cs.class_id = c.id " +
                "WHERE s.status = 'active' AND (cs.teacher_id = ? OR c.homeroom_teacher_id = ?)";
        try (java.sql.Connection conn = com.school.database.Database.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ps.setInt(2, teacherId);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
        return 0;
    }
}