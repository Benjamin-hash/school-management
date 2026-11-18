package com.school.utils;

import com.school.database.Database;
import java.awt.Desktop;
import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;



public class LicenseManager {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final String PAYSTACK_LINK = "https://paystack.com/pay/your-school-app-link";
    private static final String LOCK_FILE_PATH = System.getProperty("user.home") + File.separator + ".school_mgmt_trial";

    public static void initializeFirstRun() {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM license_info")) {
            ResultSet rs = ps.executeQuery();
            rs.next();
            if (rs.getInt(1) == 0) {
                LocalDate now = LocalDate.now();
                String insert = "INSERT INTO license_info (id, install_date, last_run_date) VALUES (1, ?, ?)";
                try (PreparedStatement ins = conn.prepareStatement(insert)) {
                    ins.setString(1, now.format(FORMATTER));
                    ins.setString(2, now.format(FORMATTER));
                    ins.executeUpdate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isAccessAllowed() {
        LocalDate today = LocalDate.now();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM license_info WHERE id = 1")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Anti-Cheat: Clock Tampering
                LocalDate lastRun = LocalDate.parse(rs.getString("last_run_date"), FORMATTER);
                if (today.isBefore(lastRun)) return false;

                // Update last run
                try (PreparedStatement update = conn.prepareStatement("UPDATE license_info SET last_run_date = ? WHERE id = 1")) {
                    update.setString(1, today.format(FORMATTER));
                    update.executeUpdate();
                }

                // Check Subscription
                String subEndStr = rs.getString("subscription_end_date");
                if (subEndStr != null && !today.isAfter(LocalDate.parse(subEndStr, FORMATTER))) return true;

                // Check Trial
                String trialEndStr = rs.getString("trial_end_date");
                return trialEndStr != null && !today.isAfter(LocalDate.parse(trialEndStr, FORMATTER));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // --- NEW: Check if trial is completely dead (used on this PC OR expired) ---
    public static boolean isTrialCompletelyUsed() {
        if (isTrialUsedOnThisMachine()) return true;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT trial_end_date FROM license_info WHERE id = 1")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String trialEnd = rs.getString("trial_end_date");
                if (trialEnd != null) {
                    if (LocalDate.now().isAfter(LocalDate.parse(trialEnd, FORMATTER))) return true;
                }
            }
        } catch (Exception e) {}
        return false;
    }

    // --- HWID / LOCK FILE LOGIC ---
    public static boolean isTrialUsedOnThisMachine() {
        File lockFile = new File(LOCK_FILE_PATH);
        if (!lockFile.exists()) return false;

        try (Scanner scanner = new Scanner(lockFile)) {
            if (scanner.hasNextLine()) {
                String savedHash = scanner.nextLine().trim();
                String expectedHash = generateTrialHash();
                return !savedHash.equals(expectedHash);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return true;
    }

    public static void markTrialUsedOnThisMachine() {
        try (PrintWriter out = new PrintWriter(LOCK_FILE_PATH)) {
            out.println(generateTrialHash());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static String generateTrialHash() {
        try {
            String rawData = "SCHOOL_APP_TRIAL_V1_" + System.getProperty("user.name") + "_" + System.getProperty("os.name");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawData.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            return "FALLBACK_HASH_123";
        }
    }

    public static boolean activateTrial() {
        if (isTrialUsedOnThisMachine()) return false;

        LocalDate today = LocalDate.now();
        LocalDate trialEnd = today.plusMonths(8);

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE license_info SET trial_end_date = ?, is_trial_active = 1 WHERE id = 1")) {
            ps.setString(1, trialEnd.format(FORMATTER));
            ps.executeUpdate();
            markTrialUsedOnThisMachine();
            return true;
        } catch (Exception e) { return false; }
    }

    public static void openPaystack() {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(PAYSTACK_LINK));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- UPDATED: Lifetime Logic Added ---
    // PASTE THE EXACT SAME SECRET KEY YOU USED IN THE GENERATOR
    private static final String SECRET_KEY = "KiTechs/";

    public static boolean activateLicense(String key) {
        if (key == null || key.length() < 10) return false;

        try {
            // Split the key into parts: [PLAN] - [EXPIRY] - [HASH]
            String[] parts = key.split("-");
            if (parts.length != 3) return false;

            String plan = parts[0];
            String expiryDateStr = parts[1];
            String providedHash = parts[2];

            // Verify the plan type
            if (!plan.equals("TRM") && !plan.equals("YEA") && !plan.equals("LIF")) return false;

            // Re-generate the hash to verify it matches
            String rawData = plan + "-" + expiryDateStr + "-" + SECRET_KEY;
            String expectedHash = generateHash(rawData).substring(0, 6).toUpperCase();

            // If the hash matches, the key is legitimate!
            if (providedHash.equals(expectedHash)) {
                // Convert the YYYYMMDD string into a real date
                LocalDate newExpiry = LocalDate.parse(expiryDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));

                try (Connection conn = Database.getConnection();
                     PreparedStatement ps = conn.prepareStatement("UPDATE license_info SET license_key = ?, subscription_end_date = ?, trial_end_date = NULL WHERE id = 1")) {
                    ps.setString(1, key);
                    ps.setString(2, newExpiry.format(FORMATTER));
                    ps.executeUpdate();
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    // Helper to generate the SHA-256 hash
    private static String generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "ERROR";
        }
    }
}