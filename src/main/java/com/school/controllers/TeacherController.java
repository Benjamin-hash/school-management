package com.school.controllers;

import com.school.dao.TeacherDAO;
import com.school.models.Teacher;
import com.school.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class TeacherController {

    @FXML private TableView<Teacher> teacherTable;
    @FXML private TableColumn<Teacher, String> colFirst, colLast, colEmail;
    @FXML private TextField fnField, lnField, emailField, phoneField, userField, passField;
    @FXML private TextField searchField;
    @FXML private Label msgLabel;

    private User currentUser;
    public void setCurrentUser(User user) { this.currentUser = user; }

    private final ObservableList<Teacher> list = FXCollections.observableArrayList();
    private Teacher selectedTeacher; // Tracks the currently clicked teacher

    @FXML
    private void initialize() {
        colFirst.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getFirstName()));
        colLast.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getLastName()));
        colEmail.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getEmail()));

        // Setup Search Filter
        FilteredList<Teacher> filteredList = new FilteredList<>(list, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(teacher -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return teacher.getFirstName().toLowerCase().contains(lowerCaseFilter) ||
                        teacher.getLastName().toLowerCase().contains(lowerCaseFilter) ||
                        (teacher.getEmail() != null && teacher.getEmail().toLowerCase().contains(lowerCaseFilter));
            });
        });

        teacherTable.setItems(filteredList);
        refresh();

        // --- DOUBLE CLICK POPUP ---
        teacherTable.setRowFactory(tv -> {
            TableRow<Teacher> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Teacher selected = row.getItem();
                    List<String> assignments = com.school.dao.ClassSubjectDAO.getAssignmentsForTeacher(selected.getId());

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Teacher Assignments");
                    alert.setHeaderText("Classes & Subjects for " + selected.getFirstName() + " " + selected.getLastName());

                    if (assignments.isEmpty()) {
                        alert.setContentText("No subject assignments found. (Check Homeroom Setup if this is a Primary teacher).");
                    } else {
                        alert.setContentText(String.join("\n", assignments));
                    }
                    alert.showAndWait();
                }
            });
            return row;
        });

        // --- SELECTION LISTENER ---
        teacherTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedTeacher = newVal;
                fnField.setText(newVal.getFirstName());
                lnField.setText(newVal.getLastName());
                emailField.setText(newVal.getEmail());
                phoneField.setText(newVal.getPhone());
                // We don't fill username/password because they are for creating NEW logins only
            }
        });
    }

    private void refresh() {
        list.setAll(TeacherDAO.getAll());
    }

    @FXML
    private void handleAdd() {
        if (fnField.getText().trim().isEmpty() || lnField.getText().trim().isEmpty() ||
                userField.getText().trim().isEmpty() || passField.getText().trim().isEmpty()) {
            msgLabel.setText("First Name, Last Name, Username and Password are required.");
            msgLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        boolean success = TeacherDAO.insertTeacherWithUser(
                fnField.getText().trim(), lnField.getText().trim(), emailField.getText().trim(), phoneField.getText().trim(),
                userField.getText().trim(), passField.getText().trim()
        );

        if (success) {
            msgLabel.setText("Teacher & Login Created!");
            msgLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            refresh();
            handleClear();
        } else {
            msgLabel.setText("Error: Username already exists.");
            msgLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedTeacher == null) {
            msgLabel.setText("Please select a teacher to update.");
            msgLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        selectedTeacher.setFirstName(fnField.getText().trim());
        selectedTeacher.setLastName(lnField.getText().trim());
        selectedTeacher.setEmail(emailField.getText().trim());
        selectedTeacher.setPhone(phoneField.getText().trim());

        // Note: We are updating the model in memory, but we need to pass it to the DAO
        Teacher updatedTeacher = new Teacher(selectedTeacher.getId(), fnField.getText().trim(),
                lnField.getText().trim(), emailField.getText().trim(), phoneField.getText().trim());

        TeacherDAO.update(updatedTeacher);
        refresh();
        msgLabel.setText("Teacher updated successfully!");
        msgLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        handleClear();
    }

    @FXML
    private void handleDelete() {
        if (selectedTeacher == null) {
            msgLabel.setText("Please select a teacher to delete.");
            msgLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete " + selectedTeacher.getFirstName() + "?\nThis will remove their login and unassign them from all classes.",
                ButtonType.YES, ButtonType.NO);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                if (TeacherDAO.delete(selectedTeacher.getId())) {
                    refresh();
                    msgLabel.setText("Teacher deleted.");
                    msgLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    handleClear();
                } else {
                    msgLabel.setText("Error deleting teacher.");
                    msgLabel.setStyle("-fx-text-fill: red;");
                }
            }
        });
    }

    @FXML
    private void handleClear() {
        fnField.clear(); lnField.clear(); emailField.clear(); phoneField.clear();
        userField.clear(); passField.clear();
        teacherTable.getSelectionModel().clearSelection();
        selectedTeacher = null;
        msgLabel.setText("");
    }
}