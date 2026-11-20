package com.school.controllers;

import com.school.dao.ClassDAO;
import com.school.dao.ClassSubjectDAO;
import com.school.dao.SubjectDAO;
import com.school.dao.TeacherDAO;
import com.school.models.ClassSubject;
import com.school.models.SchoolClass;
import com.school.models.Subject;
import com.school.models.Teacher;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PrimarySubjectController {
    @FXML private TableView<ClassSubject> mapTable;
    @FXML private TableColumn<ClassSubject, String> colClass, colSubject, colTeacher;
    @FXML private ComboBox<SchoolClass> classChoice;
    @FXML private ComboBox<Subject> subjectChoice;
    @FXML private ComboBox<Teacher> teacherChoice;
    @FXML private TextField searchField; // NEW

    private final ObservableList<ClassSubject> list = FXCollections.observableArrayList();
    private FilteredList<ClassSubject> filteredList;

    @FXML
    private void initialize() {
        mapTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        colClass.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getClassName()));
        colSubject.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getSubjectName()));
        colTeacher.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getTeacherName()));

        // Setup Search
        filteredList = new FilteredList<>(list, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(cs -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return cs.getClassName().toLowerCase().contains(lowerCaseFilter) ||
                        cs.getSubjectName().toLowerCase().contains(lowerCaseFilter) ||
                        cs.getTeacherName().toLowerCase().contains(lowerCaseFilter);
            });
        });

        mapTable.setItems(filteredList);

        classChoice.setItems(FXCollections.observableArrayList(ClassDAO.getPrimaryClasses()));
        subjectChoice.setItems(FXCollections.observableArrayList(SubjectDAO.getAll()));
        teacherChoice.setItems(FXCollections.observableArrayList(TeacherDAO.getAll()));

        refresh();
    }

    private void refresh() {
        list.setAll(ClassSubjectDAO.getPrimaryAssignments());
    }

    @FXML
    private void handleAssign() {
        if (classChoice.getValue() != null && subjectChoice.getValue() != null && teacherChoice.getValue() != null) {
            ClassSubjectDAO.assign(
                    classChoice.getValue().getId(),
                    subjectChoice.getValue().getId(),
                    teacherChoice.getValue().getId()
            );
            refresh();
            classChoice.setValue(null);
            subjectChoice.setValue(null);
            teacherChoice.setValue(null);
        }
    }

    @FXML
    private void handleRemove() {
        ClassSubject selected = mapTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ClassSubjectDAO.removeAssignment(selected.getId());
            refresh();
        }
    }
}