package com.school.controllers;

import com.school.dao.TeacherDAO;
import com.school.models.SchoolClass;
import com.school.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;

import java.io.IOException;

public class DashboardController {

    @FXML private BorderPane contentArea;
    @FXML private Label welcomeLabel;
    @FXML private VBox menuBox;

    private User currentUser;



    public void setCurrentUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText(user.getUsername() + " (" + user.getRole() + ") ▾");
        buildMenu();
        showDashboard();
    }


    private boolean isAdmin() { return "ADMIN".equals(currentUser.getRole()); }

    private void buildMenu() {
        menuBox.getChildren().clear();
        addMenuItem("Dashboard", this::showDashboard);

        if (isAdmin()) {
            addMenuItem("Students", () -> loadView("/fxml/students.fxml", controller -> {
                if (controller instanceof StudentsController) {
                    ((StudentsController) controller).setCurrentUser(currentUser);
                }
            }));
            addMenuItem("Classes", this::showClasses);
            addMenuItem("Teachers", () -> loadView("/fxml/teachers.fxml", null));
            addMenuItem("Subjects", () -> loadView("/fxml/subjects.fxml", null));
            addMenuItem("Assignments", () -> loadView("/fxml/class_subjects.fxml", null));
            addMenuItem("Homeroom Setup", () -> loadView("/fxml/homeroom_assignment.fxml", null));
            /*// --- Modules for Admin ---
            addMenuItem("Attendance", this::showAttendance);
            addMenuItem("Grades", this::showGrades);*/

            // --- NEW: Fee Setup ---
            addMenuItem("Fee Setup", () -> loadView("/fxml/fee_structures.fxml", null));

            addMenuItem("Fees", () -> loadView("/fxml/fees.fxml", controller -> {
                if (controller instanceof FeeController) {
                    ((FeeController) controller).setCurrentUser(currentUser);
                }
            }));
            addMenuItem("Report Cards", () -> loadView("/fxml/report_card.fxml", controller -> {
                if (controller instanceof ReportCardController) {
                    ((ReportCardController) controller).setCurrentUser(currentUser);
                }
            }));

            addMenuItem("Settings", () -> loadView("/fxml/settings.fxml", null));
            addMenuItem("User accounts", () -> loadView("/fxml/user_accounts.fxml", controller -> {
                if (controller instanceof UserAccountController) {
                    ((UserAccountController) controller).setCurrentUser(currentUser);
                }
            }));
            addMenuItem("Audit Logs", () -> loadView("/fxml/audit_logs.fxml", null));
        } else {
            addMenuItem("My students", this::showMyStudents);
            addMenuItem("Attendance", this::showAttendance);
            addMenuItem("Grades", this::showGrades);

            // NEW PROMOTION WIRING
            addMenuItem("Promotion", () -> loadView("/fxml/promotion.fxml", controller -> {
                if (controller instanceof PromotionController) {
                    ((PromotionController) controller).setCurrentUser(currentUser);
                }
            }));
            addMenuItem("Report Cards", () -> loadView("/fxml/report_card.fxml", controller -> {
                if (controller instanceof ReportCardController) {
                    ((ReportCardController) controller).setCurrentUser(currentUser);
                }
            }));
        }
    }

    private void addMenuItem(String label, Runnable action) {
        // Add emojis based on the label name
        String icon = "";
        if (label.equals("Dashboard")) icon = "📊 ";
        else if (label.equals("Students")) icon = "🎓 ";
        else if (label.equals("Teachers")) icon = "\uD83D\uDC68\uD83C\uDFFF\u200D\uD83C\uDFEB ";
        else if (label.equals("Classes")) icon = "\uD83D\uDCDA ";
        else if (label.equals("Fees")) icon = "💰 ";
        else if (label.equals("Settings")) icon = "⚙️ ";
        else if (label.equals("Audit Logs")) icon = "🔒 ";
        else if (label.equals("Report Cards")) icon = "\uD83D\uDD6E ";
        else if (label.equals("Attendance")) icon = "✔\uFE0F ";
        else icon = "• "; // Default dot for others

        Button btn = new Button(icon + label);
        btn.getStyleClass().add("menu-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> {
            menuBox.getChildren().forEach(n -> n.getStyleClass().remove("active"));
            btn.getStyleClass().add("active");
            action.run();
        });
        menuBox.getChildren().add(btn);
    }

    // --- SCREEN LOADERS ---

    private void showStudents() {
        loadView("/fxml/students.fxml", null);
    }

    private void showClasses() {
        loadView("/fxml/classes.fxml", null);
    }

    private void showMyStudents() {
        loadView("/fxml/my_students.fxml", controller -> {
            if (controller instanceof MyStudentsController) {
                ((MyStudentsController) controller).setCurrentUser(currentUser);
            }
        });
    }

    private void showAttendance() {
        loadView("/fxml/attendance.fxml", controller -> {
            if (controller instanceof AttendanceController) {
                ((AttendanceController) controller).setCurrentUser(currentUser);
            }
        });
    }

    private void showGrades() {
        loadView("/fxml/grades.fxml", controller -> {
            if (controller instanceof GradeController) {
                ((GradeController) controller).setCurrentUser(currentUser);
            }
        });
    }

    private void loadView(String fxmlPath, java.util.function.Consumer<Object> controllerSetup) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (controllerSetup != null) {
                controllerSetup.accept(loader.getController());
            }

            contentArea.setCenter(root);
        } catch (IOException e) {
            e.printStackTrace();
            contentArea.setCenter(new Label("Error loading screen: " + fxmlPath));
        }
    }

    private void showDashboard() {
        if (isAdmin()) {
            // --- ADMIN COMMAND CENTER ---
            String activeYear = com.school.dao.SettingsDAO.getActiveYear();
            int activeTerm = com.school.dao.SettingsDAO.getActiveTerm();

            Label heading = new Label("Admin Overview");
            heading.getStyleClass().add("module-title");

            Label sessionLabel = new Label("Current Session: " + activeYear + " (Term " + activeTerm + ")");
            sessionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

            FlowPane cards = new FlowPane();
            cards.setHgap(15);
            cards.setVgap(15);

            // People Stats
            int totalStudents = com.school.dao.StudentDAO.count();
            int totalTeachers = com.school.dao.TeacherDAO.count();
            int totalClasses = com.school.dao.ClassDAO.getAll().size();

            // Financial Stats (Current Term)
            double billed = com.school.dao.FeeDAO.getTotalBilled(activeTerm, activeYear);
            double collected = com.school.dao.FeeDAO.getTotalCollected(activeTerm, activeYear);
            double outstanding = com.school.dao.FeeDAO.getTotalOutstanding(activeTerm, activeYear);

            // Create Stat Cards
            cards.getChildren().addAll(
                    createStatCard("Active Students", String.valueOf(totalStudents), "#3498db"),
                    createStatCard("Total Teachers", String.valueOf(totalTeachers), "#9b59b6"),
                    createStatCard("Active Classes", String.valueOf(totalClasses), "#e67e22"),
                    createStatCard("Term Billed", "$" + String.format("%.2f", billed), "#475569"),
                    createStatCard("Term Collected", "$" + String.format("%.2f", collected), "#27ae60"),
                    createStatCard("Outstanding", "$" + String.format("%.2f", outstanding), "#e74c3c")
            );

            // --- CHARTS ---
            HBox chartsBox = new HBox(20);
            chartsBox.setPadding(new javafx.geometry.Insets(20, 0, 0, 0));

            // 1. Fee Pie Chart
            PieChart feeChart = new PieChart();
            feeChart.setTitle("Term Fee Collection");
            feeChart.getData().add(new PieChart.Data("Collected", collected));
            feeChart.getData().add(new PieChart.Data("Outstanding", outstanding));
            feeChart.setPrefWidth(400);
            feeChart.setMinHeight(300);

            // 2. Attendance Bar Chart
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            BarChart<String, Number> attendanceChart = new BarChart<>(xAxis, yAxis);
            attendanceChart.setTitle("School Attendance Overview");
            attendanceChart.setLegendVisible(false);
            attendanceChart.setPrefWidth(500);
            attendanceChart.setMinHeight(300);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            int[] attStats = com.school.dao.AttendanceDAO.getSchoolAttendanceStats();
            series.getData().add(new XYChart.Data<>("Present", attStats[0]));
            series.getData().add(new XYChart.Data<>("Absent", attStats[1]));
            series.getData().add(new XYChart.Data<>("Late", attStats[2]));
            attendanceChart.getData().add(series);

            chartsBox.getChildren().addAll(feeChart, attendanceChart);

            VBox mainContainer = new VBox(15, heading, sessionLabel, cards, chartsBox);
            VBox.setVgrow(chartsBox, javafx.scene.layout.Priority.ALWAYS);

            ScrollPane scrollPane = new ScrollPane(mainContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: transparent;");

            // ONLY THIS ONE SETCENTER SHOULD EXIST
            contentArea.setCenter(scrollPane);

        } else {
            // ... (Teacher Dashboard remains the same) ...
            // --- TEACHER DASHBOARD ---
            Label heading = new Label("My Classes");
            heading.getStyleClass().add("module-title");

            int myStudentCount = TeacherDAO.getStudentCountForTeacher(currentUser.getTeacherId());
            Label summaryLabel = new Label("You have " + myStudentCount + " total students across your assigned classes.");
            summaryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

            FlowPane cards = new FlowPane();
            cards.setHgap(12); cards.setVgap(12);

            var classes = TeacherDAO.getClassesForTeacher(currentUser.getTeacherId());
            if (classes.isEmpty()) {
                cards.getChildren().add(new Label("No classes assigned yet. Ask the Admin to assign you."));
            }
            for (SchoolClass c : classes) {
                VBox card = new VBox(4);
                card.getStyleClass().add("class-card");

                Label name = new Label(c.getName());
                name.getStyleClass().add("class-card-name");

                Label meta = new Label(c.getLevel() + " · Section " + c.getSection());
                meta.getStyleClass().add("module-note");

                Label countLabel = new Label("Students: " + c.getStudentCount());
                countLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #3498db;");

                card.getChildren().addAll(name, meta, countLabel);
                cards.getChildren().add(card);
            }
            contentArea.setCenter(new VBox(15, heading, summaryLabel, cards));
        }
    }

    private VBox createStatCard(String title, String value, String colorHex) {
        VBox card = new VBox(5);
        card.getStyleClass().add("class-card");
        card.setPrefWidth(200); // Slightly smaller to fit 6 on screen

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("module-note");
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: 800; -fx-text-fill: " + colorHex + ";");

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) menuBox.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("School Management System - Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}