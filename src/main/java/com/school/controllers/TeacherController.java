package com.school.controllers;

import com.school.dao.TeacherDAO;
import com.school.models.Teacher;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
        refresh();
    }

    private void refresh() { list.setAll(TeacherDAO.getAll()); }

    @FXML private void handleAdd() {
        boolean success = TeacherDAO.insertTeacherWithUser(
                fnField.getText(), lnField.getText(), emailField.getText(), phoneField.getText(),
                userField.getText(), passField.getText()
        );
        if (success) {
            msgLabel.setText("Teacher & Login Created!");
            refresh();
            fnField.clear(); lnField.clear(); emailField.clear(); phoneField.clear(); userField.clear(); passField.clear();
        } else {
            msgLabel.setText("Error: Username might be taken.");
        }
    }
}