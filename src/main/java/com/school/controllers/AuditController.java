package com.school.controllers;

import com.school.database.Database;
import com.school.models.AuditLog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.*;

public class AuditController {

    @FXML private TableView<AuditLog> auditTable;
    @FXML private TableColumn<AuditLog, String> colTime, colUser, colAction, colDetails;

    @FXML
    private void initialize() {
        // THE FIX: We can now use simple StringProperty!
        colTime.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getTimestamp()));
        colUser.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getUser()));
        colAction.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getAction()));
        colDetails.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getDetails()));

        ObservableList<AuditLog> list = FXCollections.observableArrayList();

        String sql = "SELECT a.timestamp, COALESCE(u.username, 'System') as user, a.action, a.details " +
                "FROM audit_logs a LEFT JOIN users u ON u.id = a.user_id ORDER BY a.id DESC";

        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new AuditLog(
                        rs.getString("timestamp"),
                        rs.getString("user"),
                        rs.getString("action"),
                        rs.getString("details")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error loading audit logs: " + e.getMessage());
        }

        auditTable.setItems(list);
    }
}