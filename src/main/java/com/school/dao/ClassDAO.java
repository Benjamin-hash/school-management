package com.school.dao;

import com.school.database.Database;
import com.school.models.SchoolClass;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClassDAO {

    private static final String SELECT_WITH_COUNT =
            "SELECT c.id, c.name, c.level, c.section, c.level_order, c.homeroom_teacher_id, "
                    + "(SELECT COUNT(*) FROM students WHERE students.class_id = c.id) AS student_count "
                    + "FROM classes c ORDER BY c.level_order, c.section";

    public static List<SchoolClass> getAll() {
        List<SchoolClass> classes = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SELECT_WITH_COUNT)) {
            while (rs.next()) {
                int hrId = rs.getInt("homeroom_teacher_id");
                Integer homeroomId = rs.wasNull() ? null : hrId;

                classes.add(new SchoolClass(
                        rs.getInt("id"), rs.getString("name"),
                        rs.getString("level"), rs.getString("section"),
                        rs.getInt("level_order"), rs.getInt("student_count"),
                        homeroomId));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return classes;
    }

    public static void insert(SchoolClass c) {
        String sql = "INSERT INTO classes (name, level, section, level_order, homeroom_teacher_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getLevel());
            ps.setString(3, c.getSection());
            ps.setInt(4, c.getLevelOrder());
            if (c.getHomeroomTeacherId() == null) ps.setNull(5, Types.INTEGER);
            else ps.setInt(5, c.getHomeroomTeacherId());

            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) c.setId(keys.getInt(1));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void update(SchoolClass c) {
        String sql = "UPDATE classes SET name = ?, level = ?, section = ?, level_order = ?, homeroom_teacher_id = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getLevel());
            ps.setString(3, c.getSection());
            ps.setInt(4, c.getLevelOrder());
            if (c.getHomeroomTeacherId() == null) ps.setNull(5, Types.INTEGER);
            else ps.setInt(5, c.getHomeroomTeacherId());
            ps.setInt(6, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void delete(int id) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM classes WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static SchoolClass getNextClass(int currentLevelOrder, String section) {
        String sql = "SELECT id, name, level, section, level_order, homeroom_teacher_id FROM classes WHERE level_order = ? AND section = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentLevelOrder + 1);
            ps.setString(2, section);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int hrId = rs.getInt("homeroom_teacher_id");
                Integer homeroomId = rs.wasNull() ? null : hrId;
                return new SchoolClass(rs.getInt("id"), rs.getString("name"), rs.getString("level"), rs.getString("section"), rs.getInt("level_order"), 0, homeroomId);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // Fetch all classes and their homeroom teacher's name
    public static List<SchoolClass> getAllHomeroomAssignments() {
        List<SchoolClass> list = new ArrayList<>();
        String sql = "SELECT c.id, c.name, c.level, c.section, c.level_order, c.homeroom_teacher_id, " +
                "t.first_name || ' ' || t.last_name AS teacher_name " +
                "FROM classes c LEFT JOIN teachers t ON t.id = c.homeroom_teacher_id " +
                "ORDER BY c.level_order, c.section";
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int hrId = rs.getInt("homeroom_teacher_id");
                Integer homeroomId = rs.wasNull() ? null : hrId;
                SchoolClass sc = new SchoolClass(rs.getInt("id"), rs.getString("name"), rs.getString("level"),
                        rs.getString("section"), rs.getInt("level_order"), 0, homeroomId);
                sc.setHomeroomTeacherName(rs.getString("teacher_name"));
                list.add(sc);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // Save the homeroom teacher
    public static void updateHomeroomTeacher(int classId, Integer teacherId) {
        String sql = "UPDATE classes SET homeroom_teacher_id = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (teacherId == null) ps.setNull(1, Types.INTEGER);
            else ps.setInt(1, teacherId);
            ps.setInt(2, classId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    // Fetch ONLY Primary/Nursery/KG classes for Homeroom Setup
    public static List<SchoolClass> getPrimaryClasses() {
        List<SchoolClass> list = new ArrayList<>();
        // Filter out JHS and SS classes
        String sql = "SELECT c.id, c.name, c.level, c.section, c.level_order, c.homeroom_teacher_id, " +
                "t.first_name || ' ' || t.last_name AS teacher_name " +
                "FROM classes c LEFT JOIN teachers t ON t.id = c.homeroom_teacher_id " +
                "WHERE c.level NOT LIKE '%JHS%' AND c.level NOT LIKE '%SS%' " +
                "ORDER BY c.level_order, c.section";
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int hrId = rs.getInt("homeroom_teacher_id");
                Integer homeroomId = rs.wasNull() ? null : hrId;
                SchoolClass sc = new SchoolClass(rs.getInt("id"), rs.getString("name"), rs.getString("level"),
                        rs.getString("section"), rs.getInt("level_order"), 0, homeroomId);
                sc.setHomeroomTeacherName(rs.getString("teacher_name"));
                list.add(sc);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    public static List<SchoolClass> getSecondaryClasses() {
        List<SchoolClass> list = new ArrayList<>();
        // Filter to only show JHS and SS classes
        String sql = "SELECT id, name, level, section, level_order, homeroom_teacher_id FROM classes " +
                "WHERE level LIKE '%JHS%' OR level LIKE '%SS%' ORDER BY level_order, section";
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int hrId = rs.getInt("homeroom_teacher_id");
                Integer homeroomId = rs.wasNull() ? null : hrId;
                list.add(new SchoolClass(rs.getInt("id"), rs.getString("name"), rs.getString("level"),
                        rs.getString("section"), rs.getInt("level_order"), 0, homeroomId));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}