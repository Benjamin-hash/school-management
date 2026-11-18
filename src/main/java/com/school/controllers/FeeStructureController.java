package com.school.controllers;

import com.school.dao.FeeStructureDAO;
import com.school.models.FeeStructure;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class FeeStructureController {

    @FXML private ChoiceBox<String> levelChoice;
    @FXML private ChoiceBox<Integer> termChoice;
    @FXML private TextField amountField;
    @FXML private TableView<FeeStructure> feeTable;
    @FXML private TableColumn<FeeStructure, String> colLevel;
    @FXML private TableColumn<FeeStructure, Integer> colTerm;
    @FXML private TableColumn<FeeStructure, Double> colAmount;
    @FXML private Label msgLabel;

    private ObservableList<FeeStructure> feeList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Setup Dropdowns
        levelChoice.setItems(FXCollections.observableArrayList(FeeStructureDAO.getAllLevels()));
        termChoice.setItems(FXCollections.observableArrayList(1, 2, 3));

        // Setup Table Columns
        colLevel.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getLevel()));
        colTerm.setCellValueFactory(cd -> new javafx.beans.property.SimpleObjectProperty<>(cd.getValue().getTerm()));
        colAmount.setCellValueFactory(cd -> new javafx.beans.property.SimpleObjectProperty<>(cd.getValue().getAmount()));

        feeTable.setItems(feeList);
        refreshTable();
    }

    private void refreshTable() {
        feeList.setAll(FeeStructureDAO.getAll());
    }

    @FXML
    private void handleSave() {
        String level = levelChoice.getValue();
        Integer term = termChoice.getValue();
        String amountStr = amountField.getText().trim();

        if (level == null || term == null || amountStr.isEmpty()) {
            msgLabel.setText("Please fill in all fields.");
            msgLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount < 0) throw new NumberFormatException();

            FeeStructureDAO.saveOrUpdate(level, term, amount);
            refreshTable();
            msgLabel.setText("Fee amount saved successfully!");
            msgLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

            // Clear inputs
            levelChoice.setValue(null);
            termChoice.setValue(null);
            amountField.clear();
        } catch (NumberFormatException e) {
            msgLabel.setText("Invalid amount. Please enter a valid number.");
            msgLabel.setStyle("-fx-text-fill: red;");
        }
    }
}