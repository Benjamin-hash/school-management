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
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ClassSubjectController {
    @FXML private TableView<ClassSubject> mapTable;
    @FXML private TableColumn<ClassSubject, String> colClass, colSubject, colTeacher;
    @FXML private ChoiceBox<SchoolClass> classChoice;
    @FXML private ChoiceBox<Subject> subjectChoice;
    @FXML private ChoiceBox<Teacher> teacherChoice;

    private ObservableList<ClassSubject> list = FXCollections.observableArrayList();

    @FXML private void initialize() {
        colClass.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getClassName()));
        colSubject.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getSubjectName()));
        colTeacher.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getTeacherName()));

        mapTable.setItems(list);
        classChoice.setItems(FXCollections.observableArrayList(ClassDAO.getAll()));
        subjectChoice.setItems(FXCollections.observableArrayList(SubjectDAO.getAll()));

        // NOTE: Teacher class needs a toString() method returning their name for this to display correctly!
        teacherChoice.setItems(FXCollections.observableArrayList(TeacherDAO.getAll()));

        refresh();
    }

    private void refresh() { list.setAll(ClassSubjectDAO.getAll()); }

    @FXML private void handleAssign() {
        if (classChoice.getValue() != null && subjectChoice.getValue() != null && teacherChoice.getValue() != null) {
            ClassSubjectDAO.assign(
                    classChoice.getValue().getId(),
                    subjectChoice.getValue().getId(),
                    teacherChoice.getValue().getId()
            );
            refresh();
        }
    }
}