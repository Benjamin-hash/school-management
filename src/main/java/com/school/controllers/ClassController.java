package com.school.controllers;

import com.school.dao.ClassDAO;
import com.school.dao.TeacherDAO;
import com.school.models.SchoolClass;
import com.school.models.Teacher;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ClassController {

    @FXML private TableView<SchoolClass> classTable;
    @FXML private TableColumn<SchoolClass, String> colName, colLevel, colSection;
    @FXML private TableColumn<SchoolClass, Integer> colOrder, colCount;

    @FXML private TextField nameField, levelField, sectionField, orderField;
    @FXML private ChoiceBox<Teacher> homeroomChoice; // NEW

    private ObservableList<SchoolClass> classList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colName.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getName()));
        colLevel.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getLevel()));
        colSection.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getSection()));
        colOrder.setCellValueFactory(cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getLevelOrder()).asObject());
        colCount.setCellValueFactory(cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getStudentCount()).asObject());

        classTable.setItems(classList);

        // Load teachers into the dropdown
        homeroomChoice.setItems(FXCollections.observableArrayList(TeacherDAO.getAll()));

        refreshTable();

        classTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> fillForm(newSelection));

        // After classTable.setItems(filteredList) or classTable.setItems(classList);
        classTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                nameField.setText(newVal.getName());
                levelField.setText(newVal.getLevel());
                sectionField.setText(newVal.getSection());
                orderField.setText(String.valueOf(newVal.getLevelOrder()));
            }
        });
    }

    private void refreshTable() { classList.setAll(ClassDAO.getAll()); }

    private void fillForm(SchoolClass c) {
        if (c == null) return;
        nameField.setText(c.getName());
        levelField.setText(c.getLevel());
        sectionField.setText(c.getSection());
        orderField.setText(String.valueOf(c.getLevelOrder()));

        // Set the dropdown to the saved teacher
        if (c.getHomeroomTeacherId() != null) {
            homeroomChoice.getItems().stream()
                    .filter(t -> t.getId() == c.getHomeroomTeacherId())
                    .findFirst().ifPresent(homeroomChoice::setValue);
        } else {
            homeroomChoice.setValue(null);
        }
    }

    @FXML
    private void handleAdd() {
        try {
            String name = nameField.getText().trim();
            String level = levelField.getText().trim();
            String section = sectionField.getText().trim();
            int order = Integer.parseInt(orderField.getText().trim());

            if (name.isEmpty() || level.isEmpty() || section.isEmpty()) return;

            Teacher selectedTeacher = homeroomChoice.getValue();
            Integer teacherId = (selectedTeacher != null) ? selectedTeacher.getId() : null;

            SchoolClass c = new SchoolClass(0, name, level, section, order, 0, teacherId);
            ClassDAO.insert(c);
            refreshTable();
            handleClear();
        } catch (NumberFormatException e) {
            System.out.println("Level Order must be a number (e.g., 1 for JHS 1)");
        }
    }

    @FXML
    private void handleUpdate() {
        SchoolClass selected = classTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            selected.setName(nameField.getText().trim());
            selected.setLevel(levelField.getText().trim());
            selected.setSection(sectionField.getText().trim());
            selected.setLevelOrder(Integer.parseInt(orderField.getText().trim()));

            Teacher selectedTeacher = homeroomChoice.getValue();
            selected.setHomeroomTeacherId(selectedTeacher != null ? selectedTeacher.getId() : null);

            // You need to add a setter for homeroomTeacherId in SchoolClass.java!
            // Or recreate the object. Let's recreate it to be safe.
            SchoolClass updated = new SchoolClass(selected.getId(), selected.getName(), selected.getLevel(),
                    selected.getSection(), selected.getLevelOrder(), selected.getStudentCount(),
                    selectedTeacher != null ? selectedTeacher.getId() : null);

            ClassDAO.update(updated);
            refreshTable();
            handleClear();
        } catch (NumberFormatException e) {
            System.out.println("Level Order must be a number.");
        }
    }

    @FXML
    private void handleClear() {
        nameField.clear(); levelField.clear(); sectionField.clear(); orderField.clear();
        homeroomChoice.setValue(null);
        classTable.getSelectionModel().clearSelection();
    }
}