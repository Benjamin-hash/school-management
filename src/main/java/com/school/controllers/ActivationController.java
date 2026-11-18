package com.school.controllers;

import com.school.utils.LicenseManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ActivationController {

    @FXML private Label statusLabel, codeRevealLabel, errorLabel;
    @FXML private TextField licenseField;
    @FXML private Button trialButton;

    @FXML
    public void initialize() {
        // If trial is used on this PC OR expired in the DB, hide the trial completely
        if (LicenseManager.isTrialCompletelyUsed()) {
            trialButton.setVisible(false);
            trialButton.setManaged(false); // Removes the empty space it would leave behind

            codeRevealLabel.setVisible(false);
            codeRevealLabel.setManaged(false);

            statusLabel.setText("Your free trial has expired. Please purchase a license to continue.");
            statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else {
            statusLabel.setText("Please activate a license or start your free trial.");
        }
    }

    @FXML
    private void handleActivate() {
        String key = licenseField.getText().trim().toUpperCase();

        if (key.equals("TRIAL-8M-FREE")) {
            if (LicenseManager.isTrialCompletelyUsed()) {
                errorLabel.setText("Trial has already been used.");
            } else if (LicenseManager.activateTrial()) {
                unlockApp();
            }
        } else if (LicenseManager.activateLicense(key)) {
            unlockApp();
        } else {
            errorLabel.setText("Invalid License Key. Please try again.");
        }
    }

    @FXML
    private void handleStartTrial() {
        if (LicenseManager.isTrialCompletelyUsed()) return;
        codeRevealLabel.setText("Your Trial Code is: TRIAL-8M-FREE");
        codeRevealLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 16px;");
    }

    @FXML
    private void handlePaystack() {
        LicenseManager.openPaystack();
    }

    private void unlockApp() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) licenseField.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("School Management System - Login");
        } catch (Exception e) { e.printStackTrace(); }
    }
}