package com.school.controllers;

import com.school.dao.AttendanceDAO;
import com.school.dao.ClassDAO;
import com.school.dao.TeacherDAO;
import com.school.models.AttendanceRecord;
import com.school.models.SchoolClass;
import com.school.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;

public class AttendanceController {

    @FXML private ChoiceBox<SchoolClass> classChoice;
    @FXML private DatePicker datePicker;
    @FXML private TableView<AttendanceRecord> attendanceTable;
    @FXML private TableColumn<AttendanceRecord, String> colName, colStatus;
    @FXML private Label msgLabel;

    // NEW LABELS FOR TOTALS
    @FXML private Label totalPresentLabel, totalAbsentLabel, totalLateLabel;

    private User currentUser;
    private ObservableList<AttendanceRecord> roster = FXCollections.observableArrayList();

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadClasses();
    }

    @FXML
    private void initialize() {
        datePicker.setValue(LocalDate.now());

        colName.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getStudentName()));

        // --- NEW ONE-CLICK TOGGLE CELL ---
        colStatus.setCellValueFactory(cd -> cd.getValue().statusProperty());
        colStatus.setCellFactory(column -> new TableCell<AttendanceRecord, String>() {
            {
                setOnMouseClicked(e -> {
                    if (!isEmpty() && getTableRow() != null && getTableRow().getItem() != null) {
                        AttendanceRecord record = getTableRow().getItem();
                        if ("PRESENT".equals(record.getStatus())) record.setStatus("ABSENT");
                        else if ("ABSENT".equals(record.getStatus())) record.setStatus("LATE");
                        else record.setStatus("PRESENT");

                        getTableView().refresh();
                        updateTotals(); // <--- UPDATE TOTALS WHEN CLICKED
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null); setStyle("");
                } else {
                    AttendanceRecord record = (AttendanceRecord) getTableRow().getItem();
                    setText(record.getStatus());
                    setAlignment(javafx.geometry.Pos.CENTER);
                    if ("PRESENT".equals(record.getStatus())) setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-font-weight: bold; -fx-cursor: hand;");
                    else if ("ABSENT".equals(record.getStatus())) setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-font-weight: bold; -fx-cursor: hand;");
                    else setStyle("-fx-background-color: #fef08a; -fx-text-fill: #854d0e; -fx-font-weight: bold; -fx-cursor: hand;");
                }
            }
        });

        attendanceTable.setItems(roster);

        classChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> loadRoster());
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> loadRoster());
    }

    private void loadClasses() {
        if ("ADMIN".equals(currentUser.getRole())) {
            classChoice.setItems(FXCollections.observableArrayList(ClassDAO.getAll()));
        } else {
            classChoice.setItems(FXCollections.observableArrayList(TeacherDAO.getClassesForTeacher(currentUser.getTeacherId())));
        }
    }

    private void loadRoster() {
        SchoolClass selectedClass = classChoice.getValue();
        if (selectedClass == null || datePicker.getValue() == null) {
            roster.clear();
            updateTotals();
            return;
        }

        String date = datePicker.getValue().toString();
        roster.setAll(AttendanceDAO.getRoster(selectedClass.getId(), date));
        msgLabel.setText("");
        updateTotals(); // <--- UPDATE TOTALS WHEN ROSTER LOADS
    }

    @FXML
    private void handleSave() {
        if (roster.isEmpty() || datePicker.getValue() == null) return;

        String date = datePicker.getValue().toString();
        int markedBy = currentUser.getId();

        for (AttendanceRecord record : roster) {
            AttendanceDAO.saveAttendance(record.getStudentId(), date, record.getStatus(), markedBy);
        }

        msgLabel.setText("Attendance saved successfully!");
        msgLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    // --- BULK ACTIONS ---
    @FXML
    private void handleMarkAllPresent() {
        for (AttendanceRecord record : roster) {
            record.setStatus("PRESENT");
        }
        attendanceTable.refresh();
        updateTotals(); // <--- UPDATE TOTALS AFTER BULK ACTION
    }

    @FXML
    private void handleMarkAllAbsent() {
        for (AttendanceRecord record : roster) {
            record.setStatus("ABSENT");
        }
        attendanceTable.refresh();
        updateTotals(); // <--- UPDATE TOTALS AFTER BULK ACTION
    }

    // --- NEW HELPER METHOD (PUT IT AT THE VERY BOTTOM) ---
    private void updateTotals() {
        int present = 0, absent = 0, late = 0;
        for (AttendanceRecord r : roster) {
            if ("PRESENT".equals(r.getStatus())) present++;
            else if ("ABSENT".equals(r.getStatus())) absent++;
            else if ("LATE".equals(r.getStatus())) late++;
        }

        // Check if labels are null (in case FXML hasn't been updated yet)
        if (totalPresentLabel != null) totalPresentLabel.setText("Present: " + present);
        if (totalAbsentLabel != null) totalAbsentLabel.setText("Absent: " + absent);
        if (totalLateLabel != null) totalLateLabel.setText("Late: " + late);
    }
}