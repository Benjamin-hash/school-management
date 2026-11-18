package com.school.controllers;

import com.school.dao.ClassDAO;
import com.school.dao.StudentDAO;
import com.school.models.SchoolClass;
import com.school.models.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.school.models.SchoolClass;
import com.school.dao.ClassDAO;
import javafx.scene.control.ChoiceDialog;
import java.util.List;

public class StudentsController {

    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> colFirstName, colLastName, colClassName;
    @FXML private TextField firstNameField, lastNameField, emailField, phoneField, dobField;
    @FXML private ChoiceBox<SchoolClass> classChoice;

    private ObservableList<Student> studentList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colFirstName.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getFirstName()));
        colLastName.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getLastName()));
        colClassName.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getClassName()));

        studentTable.setItems(studentList);
        classChoice.setItems(FXCollections.observableArrayList(ClassDAO.getAll()));
        refreshTable();

        studentTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, s) -> fillForm(s));
    }

    private void refreshTable() { studentList.setAll(StudentDAO.getAll()); }

    private void fillForm(Student s) {
        if (s == null) return;
        firstNameField.setText(s.getFirstName());
        lastNameField.setText(s.getLastName());
        emailField.setText(s.getEmail());
        phoneField.setText(s.getPhone());
        dobField.setText(s.getDateOfBirth());
    }

    @FXML private void handleAdd() {
        Student s = new Student(0, firstNameField.getText(), lastNameField.getText(),
                emailField.getText(), phoneField.getText(), dobField.getText(),
                classChoice.getValue() != null ? classChoice.getValue().getId() : null,
                null, null, "active");
        StudentDAO.insert(s);
        refreshTable();
        handleClear();
    }

    @FXML private void handleUpdate() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        selected.setFirstName(firstNameField.getText());
        selected.setLastName(lastNameField.getText());
        selected.setEmail(emailField.getText());
        selected.setPhone(phoneField.getText());
        selected.setDateOfBirth(dobField.getText());
        if (classChoice.getValue() != null) selected.setClassId(classChoice.getValue().getId());
        StudentDAO.update(selected);
        refreshTable();
    }

    @FXML private void handleDelete() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (StudentDAO.delete(selected.getId())) refreshTable();
    }

    @FXML private void handleClear() {
        firstNameField.clear(); lastNameField.clear(); emailField.clear();
        phoneField.clear(); dobField.clear(); classChoice.setValue(null);
        studentTable.getSelectionModel().clearSelection();
    }

    // Add this method at the bottom of the class
    @FXML
    private void handleTransfer() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Student Selected", "Please select a student from the table to transfer.");
            return;
        }

        // 1. Figure out the student's current level category
        SchoolClass currentClass = ClassDAO.getAll().stream()
                .filter(c -> c.getId() == selected.getClassId())
                .findFirst().orElse(null);

        if (currentClass == null) {
            showAlert("Error", "Student is not assigned to a valid class.");
            return;
        }

        boolean isJhs = currentClass.getLevel().contains("JHS") || currentClass.getLevel().contains("SS");

        // 2. Filter the available classes
        List<SchoolClass> allowedClasses = ClassDAO.getAll().stream()
                .filter(c -> {
                    boolean classIsJhs = c.getLevel().contains("JHS") || c.getLevel().contains("SS");
                    return isJhs == classIsJhs; // Match JHS with JHS, Primary with Primary
                })
                .toList();

        if (allowedClasses.isEmpty()) {
            showAlert("No Classes", "No valid classes found for this student's level.");
            return;
        }

        // 3. Show the ChoiceDialog
        ChoiceDialog<SchoolClass> dialog = new ChoiceDialog<>(allowedClasses.get(0), allowedClasses);
        dialog.setTitle("Transfer Student");
        dialog.setHeaderText("Transferring: " + selected.getFirstName() + " " + selected.getLastName() + " (Current: " + currentClass.getName() + ")");
        dialog.setContentText("Select New Class:");

        dialog.showAndWait().ifPresent(newClass -> {
            if (newClass.getId() != selected.getClassId()) {
                StudentDAO.transferClass(selected.getId(), newClass.getId());
                refreshTable();
                showAlert("Success", selected.getFirstName() + " has been transferred to " + newClass.getName() + ".");
            }
        });
    }

    // Helper method for alerts (if you don't already have it)
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}