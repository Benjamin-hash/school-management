package com.school;

import com.school.database.Database;
import com.school.utils.LicenseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Database.initialize();
        LicenseManager.initializeFirstRun();

        String fxmlPath;
        String title = "School Management System";

        // THE GATEKEEPER
        if (LicenseManager.isAccessAllowed()) {
            fxmlPath = "/fxml/login.fxml";
            title += " - Login";
        } else {
            fxmlPath = "/fxml/activation.fxml";
            title += " - Activation Required";
        }

        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        stage.setTitle(title);

        // Set standard size for all screens
        stage.setScene(new Scene(root, 1200, 800));

        // Allow resizing so it doesn't break on small laptop screens
        stage.setResizable(true);

        // Center the window on the monitor
        stage.centerOnScreen();

        // Optional: If you want it to open in full screen, uncomment the line below:
        // stage.setMaximized(true);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}