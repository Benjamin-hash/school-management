package com.school.controllers;

import com.school.dao.SettingsDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

public class SettingsController {

    @FXML private TextField yearField;
    @FXML private ChoiceBox<Integer> termChoice;
    @FXML private Label msgLabel;

    @FXML
    private void initialize() {
        termChoice.setItems(FXCollections.observableArrayList(1, 2, 3));
        yearField.setText(SettingsDAO.getActiveYear());
        termChoice.setValue(SettingsDAO.getActiveTerm());
    }

    @FXML
    private void handleSave() {
        String year = yearField.getText().trim();
        Integer term = termChoice.getValue();

        if (year.isEmpty() || term == null) {
            msgLabel.setText("Please fill in all fields.");
            msgLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        SettingsDAO.updateSettings(year, term);

        msgLabel.setText("Global settings updated successfully! Teachers will now see Term " + term + " (" + year + ").");
        msgLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    // --- NEW BACKUP LOGIC ---
    @FXML
    private void handleBackup() {
        File sourceFile = new File("school.db");
        if (!sourceFile.exists()) {
            msgLabel.setText("Database file not found.");
            msgLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Database Backup");
        // Suggest a filename like "school_backup_2026-11-17.db"
        fileChooser.setInitialFileName("school_backup_" + LocalDate.now() + ".db");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite Database", "*.db"));

        File destFile = fileChooser.showSaveDialog(new Stage());

        if (destFile != null) {
            try {
                Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                msgLabel.setText("Backup saved successfully to: " + destFile.getAbsolutePath());
                msgLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            } catch (Exception e) {
                msgLabel.setText("Error saving backup: " + e.getMessage());
                msgLabel.setStyle("-fx-text-fill: red;");
                e.printStackTrace();
            }
        }
    }
}