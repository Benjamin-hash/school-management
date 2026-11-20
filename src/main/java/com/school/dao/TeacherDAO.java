package com.school.dao;

import com.school.database.Database;
import com.school.models.Teacher; // <--- ADDED IMPORT
import com.school.models.SchoolClass;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherDAO {

    public static List<Teacher> getAll() {
        List<Teacher> list = new ArrayList<>();
        String sql = "SELECT * FROM teachers";
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Teacher(rs.getInt("id"), rs.getString("first_name"),
                        rs.getString("last_name"), rs.getString("email"), rs.getString("phone")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static boolean insertTeacherWithUser(String fn, String ln, String email, String phone, String username, String password) {
        String sqlTeacher = "INSERT INTO teachers (first_name, last_name, email, phone) VALUES (?, ?, ?, ?)";
        String sqlUser = "INSERT INTO users (username, password, role, teacher_id) VALUES (?, ?, 'TEACHER', ?)";

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psT = conn.prepareStatement(sqlTeacher, Statement.RETURN_GENERATED_KEYS)) {
                psT.setString(1, fn); psT.setString(2, ln); psT.setString(3, email); psT.setString(4, phone);
                psT.executeUpdate();

                ResultSet keys = psT.getGeneratedKeys();
                int teacherId = 0;
                if (keys.next()) teacherId = keys.getInt(1);

                try (PreparedStatement psU = conn.prepareStatement(sqlUser)) {
                    psU.setString(1, username);
                    psU.setString(2, com.school.utils.PasswordUtils.hashPassword(password));
                    psU.setInt(3, teacherId);
                    psU.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) { return false; }
    }

    public static void update(Teacher t) {
        String sql = "UPDATE teachers SET first_name = ?, last_name = ?, email = ?, phone = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getFirstName());
            ps.setString(2, t.getLastName());
            ps.setString(3, t.getEmail());
            ps.setString(4, t.getPhone());
            ps.setInt(5, t.getId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static boolean delete(int teacherId) {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE teacher_id = ?")) {
                    ps.setInt(1, teacherId); ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM class_subjects WHERE teacher_id = ?")) {
                    ps.setInt(1, teacherId); ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("UPDATE classes SET homeroom_teacher_id = NULL WHERE homeroom_teacher_id = ?")) {
                    ps.setInt(1, teacherId); ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM teachers WHERE id = ?")) {
                    ps.setInt(1, teacherId); ps.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) { return false; }
    }

    public static int count() {
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM teachers")) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) { return 0; }
    }

    public static List<SchoolClass> getClassesForTeacher(int teacherId) {
        List<SchoolClass> classes = new ArrayList<>();
        String sql = "SELECT DISTINCT c.id, c.name, c.level, c.section, c.level_order, c.homeroom_teacher_id, "
                + "(SELECT COUNT(*) FROM students WHERE students.class_id = c.id AND students.status = 'active') AS student_count "
                + "FROM classes c "
                + "LEFT JOIN class_subjects cs ON c.id = cs.class_id "
                + "WHERE cs.teacher_id = ? OR c.homeroom_teacher_id = ? "
                + "ORDER BY c.level_order, c.section";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ps.setInt(2, teacherId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int hrId = rs.getInt("homeroom_teacher_id");
                Integer homeroomId = rs.wasNull() ? null : hrId;
                classes.add(new SchoolClass(rs.getInt("id"), rs.getString("name"), rs.getString("level"),
                        rs.getString("section"), rs.getInt("level_order"), rs.getInt("student_count"), homeroomId));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return classes;
    }

    public static int getStudentCountForTeacher(int teacherId) {
        String sql = "SELECT COUNT(DISTINCT s.id) FROM students s " +
                "JOIN classes c ON c.id = s.class_id " +
                "LEFT JOIN class_subjects cs ON cs.class_id = c.id " +
                "WHERE s.status = 'active' AND (cs.teacher_id = ? OR c.homeroom_teacher_id = ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ps.setInt(2, teacherId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}