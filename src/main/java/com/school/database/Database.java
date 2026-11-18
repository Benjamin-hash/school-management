package com.school.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    // Change this line:
     private static final String URL = "jdbc:sqlite:school.db";

    // To this (Saves safely in the User's folder):
  //  private static final String URL = "jdbc:sqlite:" + System.getProperty("user.home") + java.io.File.separator + "school.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL);
        }
        return connection;
    }

    public static void initialize() {
        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {

            st.execute("PRAGMA foreign_keys = ON");

            st.execute("CREATE TABLE IF NOT EXISTS school_settings ("
                    + "id INTEGER PRIMARY KEY CHECK (id = 1),"
                    + "current_academic_year TEXT NOT NULL,"
                    + "current_term INTEGER NOT NULL)");

            st.execute("CREATE TABLE IF NOT EXISTS teachers ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "first_name TEXT NOT NULL,"
                    + "last_name TEXT NOT NULL,"
                    + "email TEXT,"
                    + "phone TEXT)");

            st.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "username TEXT UNIQUE NOT NULL,"
                    + "password TEXT NOT NULL,"
                    + "role TEXT NOT NULL CHECK(role IN ('ADMIN','TEACHER')),"
                    + "teacher_id INTEGER REFERENCES teachers(id))");

            // SINGLE DEFINITION OF CLASSES TABLE (with homeroom included)
            st.execute("CREATE TABLE IF NOT EXISTS classes ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT UNIQUE NOT NULL,"
                    + "level TEXT NOT NULL,"
                    + "section TEXT NOT NULL,"
                    + "level_order INTEGER NOT NULL,"
                    + "homeroom_teacher_id INTEGER REFERENCES teachers(id))");

            // Ensure column exists for older databases
            try {
                st.execute("ALTER TABLE classes ADD COLUMN homeroom_teacher_id INTEGER REFERENCES teachers(id)");
            } catch (SQLException e) { /* Ignored */ }

            st.execute("CREATE TABLE IF NOT EXISTS subjects ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT UNIQUE NOT NULL)");

            st.execute("CREATE TABLE IF NOT EXISTS class_subjects ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "class_id INTEGER NOT NULL REFERENCES classes(id),"
                    + "subject_id INTEGER NOT NULL REFERENCES subjects(id),"
                    + "teacher_id INTEGER NOT NULL REFERENCES teachers(id),"
                    + "UNIQUE(class_id, subject_id))");

            st.execute("CREATE TABLE IF NOT EXISTS students ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "first_name TEXT NOT NULL,"
                    + "last_name TEXT NOT NULL,"
                    + "email TEXT,"
                    + "phone TEXT,"
                    + "date_of_birth TEXT,"
                    + "class_id INTEGER REFERENCES classes(id),"
                    + "admission_date TEXT DEFAULT (date('now')),"
                    + "status TEXT NOT NULL DEFAULT 'active')");

            st.execute("CREATE TABLE IF NOT EXISTS attendance ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "student_id INTEGER NOT NULL REFERENCES students(id),"
                    + "date TEXT NOT NULL,"
                    + "status TEXT NOT NULL CHECK(status IN ('PRESENT','ABSENT','LATE')),"
                    + "marked_by INTEGER REFERENCES users(id),"
                    + "UNIQUE(student_id, date))");

            st.execute("CREATE TABLE IF NOT EXISTS grades ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "student_id INTEGER NOT NULL REFERENCES students(id),"
                    + "subject_id INTEGER NOT NULL REFERENCES subjects(id),"
                    + "term INTEGER NOT NULL CHECK(term IN (1,2,3)),"
                    + "academic_year TEXT NOT NULL,"
                    + "score REAL NOT NULL CHECK(score BETWEEN 0 AND 100),"
                    + "entered_by INTEGER REFERENCES users(id),"
                    + "UNIQUE(student_id, subject_id, term, academic_year))");

            st.execute("CREATE TABLE IF NOT EXISTS fee_structures ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "level TEXT NOT NULL,"
                    + "term INTEGER NOT NULL CHECK(term IN (1,2,3)),"
                    + "amount REAL NOT NULL,"
                    + "UNIQUE(level, term))");

            st.execute("CREATE TABLE IF NOT EXISTS fees ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "student_id INTEGER NOT NULL REFERENCES students(id),"
                    + "term INTEGER NOT NULL,"
                    + "academic_year TEXT NOT NULL,"
                    + "amount_due REAL NOT NULL,"
                    + "status TEXT NOT NULL DEFAULT 'UNPAID',"
                    + "UNIQUE(student_id, term, academic_year))");

            st.execute("CREATE TABLE IF NOT EXISTS fee_payments ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "fee_id INTEGER NOT NULL REFERENCES fees(id),"
                    + "amount REAL NOT NULL,"
                    + "paid_date TEXT DEFAULT (date('now')),"
                    + "received_by INTEGER REFERENCES users(id))");

            st.execute("CREATE TABLE IF NOT EXISTS license_info ("
                    + "id INTEGER PRIMARY KEY CHECK (id = 1),"
                    + "install_date TEXT NOT NULL,"
                    + "trial_end_date TEXT,"
                    + "is_trial_active INTEGER DEFAULT 0,"
                    + "license_key TEXT,"
                    + "subscription_end_date TEXT,"
                    + "last_run_date TEXT NOT NULL)");


            st.execute("CREATE TABLE IF NOT EXISTS audit_logs ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "user_id INTEGER,"
                    + "action TEXT NOT NULL,"
                    + "details TEXT NOT NULL,"
                    + "timestamp TEXT DEFAULT (datetime('now', 'localtime')))");

            seed(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private static void seed(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
            rs.next();
            if (rs.getInt(1) > 0) return;
        }

        // 1. Classes
        insertClass(conn, "JHS 1A", "JHS 1", "A", 1);
        insertClass(conn, "JHS 1B", "JHS 1", "B", 1);
        insertClass(conn, "JHS 2A", "JHS 2", "A", 2);
        insertClass(conn, "JHS 2B", "JHS 2", "B", 2);
        insertClass(conn, "JHS 3A", "JHS 3", "A", 3);

        // 2. Subjects
        insertSubject(conn, "Mathematics");
        insertSubject(conn, "English Language");
        insertSubject(conn, "Integrated Science");

        // 3. Users & Teachers
        insertUser(conn, "admin", "admin123", "ADMIN", null);
        int belloId = insertTeacher(conn, "Amina", "Bello", "bello@school.edu", "0244 000 000");
        insertUser(conn, "teacher", "teacher123", "TEACHER", belloId);

        // 4. Class Assignments
        int mathId = getId(conn, "subjects", "name", "Mathematics");
        insertClassSubject(conn, getId(conn, "classes", "name", "JHS 1A"), mathId, belloId);
        insertClassSubject(conn, getId(conn, "classes", "name", "JHS 2A"), mathId, belloId);

        // 5. Fee Structures
        String[] levels = {"JHS 1", "JHS 2", "JHS 3"};
        double[][] amounts = {{200, 180, 200}, {220, 200, 220}, {250, 230, 250}};
        for (int l = 0; l < levels.length; l++) {
            for (int t = 1; t <= 3; t++) {
                insertFeeStructure(conn, levels[l], t, amounts[l][t - 1]);
            }
        }

        // 6. Settings (MOVED OUTSIDE THE LOOP)
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO school_settings (id, current_academic_year, current_term) VALUES (1, '2026/2027', 1)")) {
            ps.executeUpdate();
        }

        // 7. DEMO STUDENTS (NOW THEY WILL ACTUALLY GET INSERTED)
        insertStudent(conn, "Kofi", "Mensah", "kofi@example.com", "0200 111 111", "2013-04-12", 1);
        insertStudent(conn, "Ama", "Serwaa", "ama@example.com", "0200 222 222", "2013-08-03", 1);
        insertStudent(conn, "Yaw", "Boateng", null, "0200 333 333", "2012-11-25", 1);
        insertStudent(conn, "Ngozi", "Eze", "ngozi@example.com", "0200 444 444", "2012-02-14", 3);
        insertStudent(conn, "Ibrahim", "Musa", null, "0200 555 555", "2012-06-30", 3);
        insertStudent(conn, "Amina", "Yusuf", "amina@example.com", "0200 666 666", "2011-12-01", 3);

        System.out.println("Seed complete. Logins -> admin/admin123 | teacher/teacher123");
    }

    private static void insertClass(Connection conn, String name, String level, String section, int order) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO classes (name, level, section, level_order) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, name); ps.setString(2, level);
            ps.setString(3, section); ps.setInt(4, order);
            ps.executeUpdate();
        }
    }

    private static void insertStudent(Connection conn, String fn, String ln, String email, String phone, String dob, int classId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO students (first_name, last_name, email, phone, date_of_birth, class_id) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, fn); ps.setString(2, ln); ps.setString(3, email);
            ps.setString(4, phone); ps.setString(5, dob); ps.setInt(6, classId);
            ps.executeUpdate();
        }
    }

    private static void insertSubject(Connection conn, String name) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO subjects (name) VALUES (?)")) {
            ps.setString(1, name);
            ps.executeUpdate();
        }
    }

    private static int insertTeacher(Connection conn, String fn, String ln, String email, String phone) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO teachers (first_name, last_name, email, phone) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fn); ps.setString(2, ln);
            ps.setString(3, email); ps.setString(4, phone);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            keys.next();
            return keys.getInt(1);
        }
    }

    private static void insertUser(Connection conn, String username, String plainPassword,
                                   String role, Integer teacherId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users (username, password, role, teacher_id) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, username);
            // MUST HASH THE PASSWORD HERE
            ps.setString(2, com.school.utils.PasswordUtils.hashPassword(plainPassword));
            ps.setString(3, role);
            if (teacherId == null) ps.setNull(4, java.sql.Types.INTEGER);
            else ps.setInt(4, teacherId);
            ps.executeUpdate();
        }
    }

    private static void insertClassSubject(Connection conn, int classId, int subjectId, int teacherId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO class_subjects (class_id, subject_id, teacher_id) VALUES (?, ?, ?)")) {
            ps.setInt(1, classId); ps.setInt(2, subjectId); ps.setInt(3, teacherId);
            ps.executeUpdate();
        }
    }

    private static void insertFeeStructure(Connection conn, String level, int term, double amount) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO fee_structures (level, term, amount) VALUES (?, ?, ?)")) {
            ps.setString(1, level); ps.setInt(2, term); ps.setDouble(3, amount);
            ps.executeUpdate();
        }
    }

    private static int getId(Connection conn, String table, String column, String value) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM " + table + " WHERE " + column + " = ?")) {
            ps.setString(1, value);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }
}