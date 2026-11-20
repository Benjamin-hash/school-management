package com.school.controllers;

import com.school.dao.SubjectDAO;
import com.school.models.Subject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SubjectController {
    @FXML private TableView<Subject> subjectTable;
    @FXML private TableColumn<Subject, String> colName;
    @FXML private TextField nameField;
    private ObservableList<Subject> list = FXCollections.observableArrayList();

    @FXML private void initialize() {
        colName.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getName()));
        subjectTable.setItems(list);

        // After subjectTable.setItems(filteredList) or subjectTable.setItems(list);
        subjectTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                nameField.setText(newVal.getName());
            }
        });

        refresh();
    }

    private void refresh() { list.setAll(SubjectDAO.getAll()); }

    @FXML private void handleAdd() {
        if (!nameField.getText().isEmpty()) {
            SubjectDAO.insert(nameField.getText());
            refresh();
            nameField.clear();
        }
    }
}