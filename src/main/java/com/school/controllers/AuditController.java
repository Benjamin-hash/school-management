package com.school.controllers;

import com.school.database.Database;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.sql.*;

public class AuditController {
    @FXML private TableView<String[]> auditTable;
    @FXML private TableColumn<String[], String> colTime, colUser, colAction, colDetails;

    @FXML
    private void initialize() {
        auditTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colTime.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue()[0]));
        colUser.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue()[1]));
        colAction.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue()[2]));
        colDetails.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue()[3]));

        ObservableList<String[]> list = FXCollections.observableArrayList();
        String sql = "SELECT a.timestamp, COALESCE(u.username, 'System') as user, a.action, a.details " +
                     "FROM audit_logs a LEFT JOIN users u ON u.id = a.user_id ORDER BY a.timestamp DESC";
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{rs.getString("timestamp"), rs.getString("user"), rs.getString("action"), rs.getString("details")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
        auditTable.setItems(list);
    }
}