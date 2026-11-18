package com.school.controllers;

import com.school.dao.ClassDAO;
import com.school.dao.StudentDAO;
import com.school.dao.TeacherDAO;
import com.school.models.PromotionRow;
import com.school.models.SchoolClass;
import com.school.models.Student;
import com.school.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;

import java.util.ArrayList;
import java.util.List;

public class PromotionController {

    @FXML private ChoiceBox<SchoolClass> classChoice;
    @FXML private Label targetClassLabel, msgLabel;
    @FXML private TableView<PromotionRow> studentTable;
    @FXML private TableColumn<PromotionRow, Boolean> colCheck;
    @FXML private TableColumn<PromotionRow, String> colName;

    private User currentUser;
    private ObservableList<PromotionRow> tableData = FXCollections.observableArrayList();
    private SchoolClass targetClass = null; // Null means graduation

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadClasses();
    }

    @FXML
    private void initialize() {
        // Setup Checkbox Column
        colCheck.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        colCheck.setCellFactory(CheckBoxTableCell.forTableColumn(colCheck));

        // Setup Name Column
        colName.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getStudent().getFirstName() + " " + cd.getValue().getStudent().getLastName()));

        studentTable.setEditable(true);
        studentTable.setItems(tableData);

        classChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> loadStudents());
    }

    private void loadClasses() {
        classChoice.setItems(FXCollections.observableArrayList(
                TeacherDAO.getClassesForTeacher(currentUser.getTeacherId())
        ));
    }

    private void loadStudents() {
        tableData.clear();
        SchoolClass selectedClass = classChoice.getValue();
        if (selectedClass == null) return;

        // 1. Find the target class
        targetClass = ClassDAO.getNextClass(selectedClass.getLevelOrder(), selectedClass.getSection());

        if (targetClass != null) {
            targetClassLabel.setText("Promoting to: " + targetClass.getName());
            targetClassLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        } else {
            targetClassLabel.setText("Final Year! Promoting will GRADUATE these students.");
            targetClassLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }

        // 2. Load students
        List<Student> students = StudentDAO.getAll().stream()
                .filter(s -> s.getClassId() != null && s.getClassId() == selectedClass.getId())
                .toList();

        for (Student s : students) {
            tableData.add(new PromotionRow(s));
        }
    }

    @FXML
    private void handlePromote() {
        SchoolClass selectedClass = classChoice.getValue();
        if (selectedClass == null || tableData.isEmpty()) return;

        // 1. Gather checked students
        List<Integer> selectedIds = new ArrayList<>();
        for (PromotionRow row : tableData) {
            if (row.isSelected()) selectedIds.add(row.getStudent().getId());
        }

        if (selectedIds.isEmpty()) {
            msgLabel.setText("No students selected.");
            msgLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // 2. Confirm Action
        String action = (targetClass != null) ? "promote" : "graduate";
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to " + action + " " + selectedIds.size() + " students?",
                ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                boolean success;
                if (targetClass != null) {
                    success = StudentDAO.promoteStudents(selectedIds, targetClass.getId());
                } else {
                    success = StudentDAO.graduateStudents(selectedIds);
                }

                if (success) {
                    msgLabel.setText("Operation successful!");
                    msgLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    loadStudents(); // Refresh the table (the promoted students disappear)
                } else {
                    msgLabel.setText("Database error. Check console.");
                    msgLabel.setStyle("-fx-text-fill: red;");
                }
            }
        });
    }
}