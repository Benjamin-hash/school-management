package com.school.dao;

import com.school.database.Database;
import com.school.models.FeeRecord;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeeDAO {

    // 1. Generate bills for the whole class based on the fee_structures table
    public static void generateBillsForClass(int classId, int term, String year) {
        String sql = "INSERT OR IGNORE INTO fees (student_id, term, academic_year, amount_due, status) " +
                "SELECT s.id, ?, ?, fs.amount, 'UNPAID' " +
                "FROM students s " +
                "JOIN classes c ON c.id = s.class_id " +
                "JOIN fee_structures fs ON fs.level = c.level AND fs.term = ? " +
                "WHERE s.class_id = ? AND s.status = 'active'";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, term);
            ps.setString(2, year);
            ps.setInt(3, term);
            ps.setInt(4, classId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // 2. Get fees and calculate total paid so far (handles installments)
    public static List<FeeRecord> getFeesForClass(int classId, int term, String year) {
        List<FeeRecord> list = new ArrayList<>();
        String sql = "SELECT f.id AS fee_id, s.id AS student_id, s.first_name || ' ' || s.last_name AS full_name, " +
                "f.amount_due, f.status, COALESCE(SUM(fp.amount), 0) AS total_paid " +
                "FROM students s " +
                "JOIN fees f ON f.student_id = s.id AND f.term = ? AND f.academic_year = ? " +
                "LEFT JOIN fee_payments fp ON fp.fee_id = f.id " +
                "WHERE s.class_id = ? " +
                "GROUP BY s.id, f.id " +
                "ORDER BY s.last_name, s.first_name";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, term);
            ps.setString(2, year);
            ps.setInt(3, classId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new FeeRecord(
                        rs.getInt("fee_id"), rs.getInt("student_id"), rs.getString("full_name"),
                        rs.getDouble("amount_due"), rs.getDouble("total_paid"), rs.getString("status")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 3. Record an installment payment
    public static void recordPayment(int feeId, double amountPaid, int receivedBy) {
        String insertPayment = "INSERT INTO fee_payments (fee_id, amount, received_by) VALUES (?, ?, ?)";
        String updateStatus = "UPDATE fees SET status = ? WHERE id = ?";

        try (Connection conn = Database.getConnection()) {
            // Insert the payment record
            try (PreparedStatement ps = conn.prepareStatement(insertPayment)) {
                ps.setInt(1, feeId);
                ps.setDouble(2, amountPaid);
                ps.setInt(3, receivedBy);
                ps.executeUpdate();
            }

            // Recalculate total paid to update status
            String checkSql = "SELECT amount_due, (SELECT COALESCE(SUM(amount), 0) FROM fee_payments WHERE fee_id = ?) AS total_paid FROM fees WHERE id = ?";
            double due = 0, paid = 0;
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, feeId); ps.setInt(2, feeId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) { due = rs.getDouble("amount_due"); paid = rs.getDouble("total_paid"); }
            }

            // Update the status
            String newStatus = (paid >= due) ? "PAID" : "PARTIAL";
            try (PreparedStatement ps = conn.prepareStatement(updateStatus)) {
                ps.setString(1, newStatus);
                ps.setInt(2, feeId);
                ps.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
    public static double getTotalOutstanding() {
        String sql = "SELECT SUM(f.amount_due) - (SELECT COALESCE(SUM(amount), 0) FROM fee_payments) FROM fees f";
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    // Total amount billed for the active term
    public static double getTotalBilled(int term, String year) {
        String sql = "SELECT SUM(amount_due) FROM fees WHERE term = ? AND academic_year = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, term); ps.setString(2, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    // Total amount collected (paid) for the active term
    public static double getTotalCollected(int term, String year) {
        String sql = "SELECT SUM(fp.amount) FROM fee_payments fp " +
                "JOIN fees f ON f.id = fp.fee_id " +
                "WHERE f.term = ? AND f.academic_year = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, term); ps.setString(2, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    // Total outstanding debt for the active term
    public static double getTotalOutstanding(int term, String year) {
        return getTotalBilled(term, year) - getTotalCollected(term, year);
    }
}