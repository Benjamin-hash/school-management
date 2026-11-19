package com.school.controllers;

import com.school.dao.UserDAO;
import com.school.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class UserAccountController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colUsername, colRole;
    @FXML private TextField newUserField;
    @FXML private PasswordField newPassField;
    @FXML private Label msgLabel;

    private ObservableList<User> userList = FXCollections.observableArrayList();
    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void initialize() {
        colUsername.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getUsername()));
        colRole.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getRole()));

        userTable.setItems(userList);
        refreshTable();
    }

    private void refreshTable() {
        userList.setAll(UserDAO.getAll());
    }

    @FXML
    private void handleCreateAdmin() {
        String username = newUserField.getText().trim();
        String password = newPassField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            msgLabel.setText("Please fill in both fields.");
            msgLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (UserDAO.createAdmin(username, password)) {
            if (currentUser != null) {
                com.school.dao.AuditDAO.log(currentUser.getId(), "ADMIN_CREATE", "Created new admin account: " + username);
            }

            msgLabel.setText("Admin account created successfully!");
            msgLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            newUserField.clear();
            newPassField.clear();
            refreshTable();
        } else {
            msgLabel.setText("Error: Username already exists.");
            msgLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleResetPassword() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            msgLabel.setText("Please select a user to reset their password.");
            msgLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Popup to ask for the new password
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Resetting password for: " + selected.getUsername());
        dialog.setContentText("Enter New Password:");

        dialog.showAndWait().ifPresent(newPass -> {
            if (!newPass.trim().isEmpty()) {
                UserDAO.resetPassword(selected.getId(), newPass.trim());

                if (currentUser != null) {
                    com.school.dao.AuditDAO.log(currentUser.getId(), "PASSWORD_RESET", "Reset password for user: " + selected.getUsername());
                }

                msgLabel.setText("Password updated successfully!");
                msgLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }
        });
    }
}