package com.school.controllers;

import com.school.dao.ClassDAO;
import com.school.dao.ClassSubjectDAO;
import com.school.dao.TeacherDAO;
import com.school.models.SchoolClass;
import com.school.models.Teacher;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class HomeroomController {

    @FXML private ChoiceBox<SchoolClass> classChoice;
    @FXML private ChoiceBox<Teacher> teacherChoice;
    @FXML private TableView<SchoolClass> homeroomTable;
    @FXML private TableColumn<SchoolClass, String> colClass, colTeacher;
    @FXML private Label msgLabel;

    private ObservableList<SchoolClass> classList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colClass.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getName()));
        colTeacher.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getHomeroomTeacherName()));

        homeroomTable.setItems(classList);

        // USE THE NEW FILTERED LIST HERE
        classChoice.setItems(FXCollections.observableArrayList(ClassDAO.getPrimaryClasses()));
        teacherChoice.setItems(FXCollections.observableArrayList(TeacherDAO.getAll()));

        refreshTable();
    }

    private void refreshTable() {
        // USE THE NEW FILTERED LIST HERE TOO
        classList.setAll(ClassDAO.getPrimaryClasses());
    }

    @FXML
    private void handleSave() {
        SchoolClass selectedClass = classChoice.getValue();
        Teacher selectedTeacher = teacherChoice.getValue();

        if (selectedClass == null || selectedTeacher == null) {
            msgLabel.setText("Please select both a Class and a Teacher.");
            msgLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // 1. Update the class record
        ClassDAO.updateHomeroomTeacher(selectedClass.getId(), selectedTeacher.getId());
        
        // 2. Automatically assign them to ALL subjects for this class (Primary logic)
        ClassSubjectDAO.assignTeacherToAllSubjects(selectedClass.getId(), selectedTeacher.getId());

        refreshTable();
        
        msgLabel.setText(selectedTeacher.getFirstName() + " is now the Homeroom Teacher for " + selectedClass.getName() + " (Assigned to all subjects).");
        msgLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        
        classChoice.setValue(null);
        teacherChoice.setValue(null);
    }
}