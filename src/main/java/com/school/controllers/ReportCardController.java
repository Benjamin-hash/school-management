package com.school.controllers;

import com.school.dao.*;
import com.school.models.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.List;

public class ReportCardController {

    @FXML private ChoiceBox<SchoolClass> classChoice;
    @FXML private ChoiceBox<Student> studentChoice;
    @FXML private Label sessionLabel, totalScoreLabel, averageLabel;
    @FXML private Label studentNameLabel, attendanceLabel; // NEW LABELS

    @FXML private TableView<ReportCardRow> reportTable;
    @FXML private TableColumn<ReportCardRow, String> colSubject, colGrade;
    @FXML private TableColumn<ReportCardRow, Double> colScore;
    @FXML private VBox printArea;

    private User currentUser;
    private ObservableList<ReportCardRow> reportList = FXCollections.observableArrayList();

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadClasses();
    }

    @FXML
    private void initialize() {
        reportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        String year = SettingsDAO.getActiveYear();
        int term = SettingsDAO.getActiveTerm();
        sessionLabel.setText("Report Card: " + year + " (Term " + term + ")");

        colSubject.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getSubjectName()));
        colScore.setCellValueFactory(cd -> new javafx.beans.property.SimpleObjectProperty<>(cd.getValue().getScore()));
        colGrade.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getGradeLetter()));

        reportTable.setItems(reportList);

        classChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> loadStudents());
        studentChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> loadReport());
    }

    private void loadClasses() {
        if ("ADMIN".equals(currentUser.getRole())) {
            classChoice.setItems(FXCollections.observableArrayList(ClassDAO.getAll()));
        } else {
            classChoice.setItems(FXCollections.observableArrayList(TeacherDAO.getClassesForTeacher(currentUser.getTeacherId())));
        }
    }

    private void loadStudents() {
        SchoolClass c = classChoice.getValue();
        if (c == null) { studentChoice.getItems().clear(); return; }

        List<Student> students = StudentDAO.getAll().stream()
                .filter(s -> s.getClassId() != null && s.getClassId() == c.getId()).toList();
        studentChoice.setItems(FXCollections.observableArrayList(students));
    }

    private void loadReport() {
        Student s = studentChoice.getValue();
        if (s == null) {
            reportList.clear();
            studentNameLabel.setText("");
            attendanceLabel.setText("");
            calculateTotals();
            return;
        }

        // Set the student name and attendance summary
        studentNameLabel.setText("Student: " + s.getFirstName() + " " + s.getLastName());
        attendanceLabel.setText(AttendanceDAO.getStudentAttendanceSummary(s.getId()));

        int term = SettingsDAO.getActiveTerm();
        String year = SettingsDAO.getActiveYear();

        reportList.setAll(ReportCardDAO.getReportCard(s.getId(), term, year));
        calculateTotals();
    }

    private void calculateTotals() {
        if (reportList.isEmpty()) {
            totalScoreLabel.setText("Total Score: 0");
            averageLabel.setText("Average: 0%");
            return;
        }

        double total = 0;
        for (ReportCardRow row : reportList) total += row.getScore();
        double average = total / reportList.size();

        totalScoreLabel.setText(String.format("Total Score: %.1f", total));
        averageLabel.setText(String.format("Average: %.1f%%", average));
    }

    @FXML
    private void handlePrint() {
        if (reportList.isEmpty()) return;

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(reportTable.getScene().getWindow())) {
            boolean success = job.printPage(printArea);
            if (success) job.endJob();
        }
    }
}