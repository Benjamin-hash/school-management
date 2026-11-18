package com.school.controllers;

import com.school.dao.TeacherDAO;
import com.school.models.Teacher;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class TeacherController {
    @FXML private TableView<Teacher> teacherTable;
    @FXML private TableColumn<Teacher, String> colFirst, colLast, colEmail;
    @FXML private TextField fnField, lnField, emailField, phoneField, userField, passField;
    @FXML private Label msgLabel;

    private ObservableList<Teacher> list = FXCollections.observableArrayList();

    @FXML private void initialize() {
        colFirst.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getFirstName()));
        colLast.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getLastName()));
        colEmail.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getEmail()));
        teacherTable.setItems(list);

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
        refresh();
    }

    private void refresh() { list.setAll(TeacherDAO.getAll()); }

    @FXML private void handleAdd() {
        // 1. Validation
        if (fnField.getText().trim().isEmpty() || lnField.getText().trim().isEmpty() ||
                userField.getText().trim().isEmpty() || passField.getText().trim().isEmpty()) {
            msgLabel.setText("First Name, Last Name, Username and Password are required.");
            msgLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // 2. Try to save
        boolean success = TeacherDAO.insertTeacherWithUser(
                fnField.getText().trim(), lnField.getText().trim(), emailField.getText().trim(), phoneField.getText().trim(),
                userField.getText().trim(), passField.getText().trim()
        );

        if (success) {
            msgLabel.setText("Teacher & Login Created!");
            msgLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            refresh();
            fnField.clear(); lnField.clear(); emailField.clear(); phoneField.clear(); userField.clear(); passField.clear();
        } else {
            msgLabel.setText("Error: Username already exists or database failed.");
            msgLabel.setStyle("-fx-text-fill: red;");
        }
    }
}