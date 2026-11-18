/*
package com.school.dao;

import com.school.database.Database;
import com.school.models.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    private static final String SELECT =
            "SELECT s.*, c.name AS class_name FROM students s "
                    + "LEFT JOIN classes c ON c.id = s.class_id ";
    private static final String ORDER =
            " ORDER BY c.level_order, c.section, s.last_name, s.first_name";

    public static List<Student> getAll() {
        List<Student> students = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SELECT + ORDER)) {
            while (rs.next()) students.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return students;
    }

    */
/** The teacher's student list: only classes assigned to this teacher. *//*

    public static List<Student> getForTeacher(int teacherId) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT DISTINCT s.*, c.name AS class_name FROM students s "
                + "LEFT JOIN classes c ON c.id = s.class_id "
                + "JOIN class_subjects cs ON cs.class_id = s.class_id "
                + "WHERE cs.teacher_id = ? AND s.status = 'active'" + ORDER;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) students.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return students;
    }

    public static int count() {
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM students WHERE status = 'active'")) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    public static void insert(Student s) {
        String sql = "INSERT INTO students (first_name, last_name, email, phone, date_of_birth, class_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getFirstName());
            ps.setString(2, s.getLastName());
            ps.setString(3, s.getEmail());
            ps.setString(4, s.getPhone());
            ps.setString(5, s.getDateOfBirth());
            if (s.getClassId() == null) ps.setNull(6, java.sql.Types.INTEGER);
            else ps.setInt(6, s.getClassId());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) s.setId(keys.getInt(1));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void update(Student s) {
        String sql = "UPDATE students SET first_name = ?, last_name = ?, email = ?, phone = ?, "
                + "date_of_birth = ?, class_id = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getFirstName());
            ps.setString(2, s.getLastName());
            ps.setString(3, s.getEmail());
            ps.setString(4, s.getPhone());
            ps.setString(5, s.getDateOfBirth());
            if (s.getClassId() == null) ps.setNull(6, java.sql.Types.INTEGER);
            else ps.setInt(6, s.getClassId());
            ps.setInt(7, s.getId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    */
/**
     * Returns false if the delete failed — e.g. the student already has
     * attendance, grade or fee rows (foreign keys protect those records).
     *//*

    public static boolean delete(int id) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM students WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private static Student mapRow(ResultSet rs) throws SQLException {
        int classId = rs.getInt("class_id");
        return new Student(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("date_of_birth"),
                rs.wasNull() ? null : classId,
                rs.getString("class_name"),
                rs.getString("admission_date"),
                rs.getString("status"));
    }
}*/


package com.school.dao;

import com.school.database.Database;
import com.school.models.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    private static final String SELECT =
            "SELECT s.*, c.name AS class_name FROM students s "
                    + "LEFT JOIN classes c ON c.id = s.class_id ";
    private static final String ORDER =
            " ORDER BY c.level_order, c.section, s.last_name, s.first_name";

    public static List<Student> getAll() {
        List<Student> students = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SELECT + ORDER)) {
            while (rs.next()) students.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return students;
    }

    public static List<Student> getForTeacher(int teacherId) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT DISTINCT s.*, c.name AS class_name FROM students s "
                + "LEFT JOIN classes c ON c.id = s.class_id "
                + "JOIN class_subjects cs ON cs.class_id = s.class_id "
                + "WHERE cs.teacher_id = ? AND s.status = 'active'" + ORDER;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) students.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return students;
    }

    public static int count() {
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM students WHERE status = 'active'")) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    public static void insert(Student s) {
        String sql = "INSERT INTO students (first_name, last_name, email, phone, date_of_birth, class_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getFirstName());
            ps.setString(2, s.getLastName());
            ps.setString(3, s.getEmail());
            ps.setString(4, s.getPhone());
            ps.setString(5, s.getDateOfBirth());
            if (s.getClassId() == null) ps.setNull(6, Types.INTEGER);
            else ps.setInt(6, s.getClassId());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) s.setId(keys.getInt(1));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void update(Student s) {
        String sql = "UPDATE students SET first_name = ?, last_name = ?, email = ?, phone = ?, "
                + "date_of_birth = ?, class_id = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getFirstName());
            ps.setString(2, s.getLastName());
            ps.setString(3, s.getEmail());
            ps.setString(4, s.getPhone());
            ps.setString(5, s.getDateOfBirth());
            if (s.getClassId() == null) ps.setNull(6, Types.INTEGER);
            else ps.setInt(6, s.getClassId());
            ps.setInt(7, s.getId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static boolean delete(int id) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM students WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { return false; }
    }

    private static Student mapRow(ResultSet rs) throws SQLException {
        int classId = rs.getInt("class_id");
        Integer cId = rs.wasNull() ? null : classId;
        return new Student(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("date_of_birth"),
                cId,
                rs.getString("class_name"),
                rs.getString("admission_date"),
                rs.getString("status")
        );
    }

    // Bulk Promote (Transaction)
    public static boolean promoteStudents(java.util.List<Integer> studentIds, int newClassId) {
        String sql = "UPDATE students SET class_id = ? WHERE id = ?";
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int id : studentIds) {
                    ps.setInt(1, newClassId);
                    ps.setInt(2, id);
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) { return false; }
    }

    // Bulk Graduate (Transaction)
    public static boolean graduateStudents(java.util.List<Integer> studentIds) {
        String sql = "UPDATE students SET status = 'graduated', class_id = NULL WHERE id = ?";
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int id : studentIds) {
                    ps.setInt(1, id);
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) { return false; }
    }

    // Add this method to StudentDAO.java
    public static void transferClass(int studentId, int newClassId) {
        String sql = "UPDATE students SET class_id = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newClassId);
            ps.setInt(2, studentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}