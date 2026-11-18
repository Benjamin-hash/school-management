package com.school.controllers;

import com.school.dao.StudentDAO;
import com.school.models.Student;
import com.school.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class MyStudentsController {
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> colFirstName, colLastName, colClassName;
    private User currentUser;

    public void setCurrentUser(User user) { this.currentUser = user; refreshTable(); }

    @FXML private void initialize() {
        colFirstName.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getFirstName()));
        colLastName.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getLastName()));
        colClassName.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getClassName()));
    }

    private void refreshTable() {
        if (currentUser != null && currentUser.getTeacherId() != null) {
            ObservableList<Student> list = FXCollections.observableArrayList(
                    StudentDAO.getForTeacher(currentUser.getTeacherId()));
            studentTable.setItems(list);
        }
    }
}