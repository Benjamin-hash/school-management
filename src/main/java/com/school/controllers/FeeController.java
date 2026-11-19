package com.school.controllers;

import com.school.dao.ClassDAO;
import com.school.dao.FeeDAO;
import com.school.dao.SettingsDAO;
import com.school.models.FeeRecord;
import com.school.models.SchoolClass;
import com.school.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class FeeController {

    @FXML private ChoiceBox<SchoolClass> classChoice;
    @FXML private Label activeSessionLabel, msgLabel;
    @FXML private TableView<FeeRecord> feeTable;
    @FXML private TableColumn<FeeRecord, String> colName, colStatus;
    @FXML private TableColumn<FeeRecord, Double> colDue, colPaid, colBalance;

    private User currentUser;
    private ObservableList<FeeRecord> feeList = FXCollections.observableArrayList();

    public void setCurrentUser(User user) { this.currentUser = user; }

    @FXML
    private void initialize() {
        feeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        String activeYear = SettingsDAO.getActiveYear();
        int activeTerm = SettingsDAO.getActiveTerm();
        activeSessionLabel.setText("Active Session: " + activeYear + " (Term " + activeTerm + ")");

        colName.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getStudentName()));
        colDue.setCellValueFactory(cd -> new javafx.beans.property.SimpleObjectProperty<>(cd.getValue().getAmountDue()));
        colPaid.setCellValueFactory(cd -> new javafx.beans.property.SimpleObjectProperty<>(cd.getValue().getAmountPaid()));
        colBalance.setCellValueFactory(cd -> new javafx.beans.property.SimpleObjectProperty<>(cd.getValue().getBalance()));
        colStatus.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getStatus()));

        feeTable.setItems(feeList);
        classChoice.setItems(FXCollections.observableArrayList(ClassDAO.getAll()));
        classChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> loadFees());
    }

    private void loadFees() {
        SchoolClass c = classChoice.getValue();
        if (c == null) { feeList.clear(); return; }

        int term = SettingsDAO.getActiveTerm();
        String year = SettingsDAO.getActiveYear();
        feeList.setAll(FeeDAO.getFeesForClass(c.getId(), term, year));
        msgLabel.setText("");
    }

    @FXML
    private void handleGenerateBills() {
        SchoolClass c = classChoice.getValue();
        if (c == null) return;

        int term = SettingsDAO.getActiveTerm();
        String year = SettingsDAO.getActiveYear();
        FeeDAO.generateBillsForClass(c.getId(), term, year);
        loadFees();
        msgLabel.setText("Bills generated for " + c.getName() + "!");
        msgLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    @FXML
    private void handleRecordPayment() {
        FeeRecord selected = feeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            msgLabel.setText("Please select a student to record a payment.");
            msgLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        if (selected.getBalance() <= 0) {
            msgLabel.setText("This student's fees are already fully paid.");
            msgLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Record Payment");
        dialog.setHeaderText("Recording payment for: " + selected.getStudentName() + "\nOutstanding Balance: $" + selected.getBalance());
        dialog.setContentText("Enter Amount Paid:");

        dialog.showAndWait().ifPresent(input -> {
            try {
                double amount = Double.parseDouble(input);
                if (amount <= 0) throw new NumberFormatException();
                if (amount > selected.getBalance()) amount = selected.getBalance();

                FeeDAO.recordPayment(selected.getFeeId(), amount, currentUser.getId());
                loadFees();
                msgLabel.setText("Payment of $" + amount + " recorded successfully!");
                msgLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

                // SHOW RECEIPT
                showReceipt(selected, amount);

            } catch (NumberFormatException e) {
                msgLabel.setText("Invalid amount entered.");
                msgLabel.setStyle("-fx-text-fill: red;");
            }
        });
    }

    private void showReceipt(FeeRecord fee, double amountPaid) {
        Dialog<ButtonType> receiptDialog = new Dialog<>();
        receiptDialog.setTitle("Payment Receipt");
        receiptDialog.setHeaderText("Payment Successful");

        ButtonType printButton = new ButtonType("Print Receipt", ButtonBar.ButtonData.OK_DONE);
        receiptDialog.getDialogPane().getButtonTypes().addAll(printButton, ButtonType.CLOSE);

        VBox receiptBox = new VBox(10);
        receiptBox.setStyle("-fx-padding: 20; -fx-background-color: white; -fx-font-family: 'Courier New';");

        Label title = new Label("GREENFIELD HIGH SCHOOL");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label date = new Label("Date: " + java.time.LocalDate.now());
        Label student = new Label("Student: " + fee.getStudentName());
        Label item = new Label("Term Fee Payment");
        Label amount = new Label("Amount Paid: $" + String.format("%.2f", amountPaid));
        amount.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label balance = new Label("Remaining Balance: $" + String.format("%.2f", (fee.getBalance() - amountPaid)));

        receiptBox.getChildren().addAll(title, new Separator(), date, student, item, amount, balance);
        receiptDialog.getDialogPane().setContent(receiptBox);

        receiptDialog.showAndWait().ifPresent(response -> {
            if (response == printButton) {
                PrinterJob job = PrinterJob.createPrinterJob();
                if (job != null && job.showPrintDialog(receiptDialog.getOwner())) {
                    job.printPage(receiptBox);
                    job.endJob();
                }
            }
        });
    }
}