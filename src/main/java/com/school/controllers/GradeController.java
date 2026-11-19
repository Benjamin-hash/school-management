package com.school.controllers;

import com.school.dao.ClassDAO;
import com.school.dao.ClassSubjectDAO;
import com.school.dao.GradeDAO;
import com.school.dao.SettingsDAO;
import com.school.dao.SubjectDAO;
import com.school.dao.TeacherDAO;
import com.school.models.GradeRecord;
import com.school.models.SchoolClass;
import com.school.models.Subject;
import com.school.models.User;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;

public class GradeController {

    @FXML
    private ChoiceBox<SchoolClass> classChoice;
    @FXML
    private ChoiceBox<Subject> subjectChoice;
    @FXML
    private Label activeSessionLabel;
    @FXML
    private Label msgLabel;

    @FXML
    private TableView<GradeRecord> gradeTable;
    @FXML
    private TableColumn<GradeRecord, String> colName;
    @FXML
    private TableColumn<GradeRecord, Double> colScore;

    private User currentUser;
    private ObservableList<GradeRecord> gradeList = FXCollections.observableArrayList();

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadClasses();
    }

    @FXML
    private void initialize() {
        String activeYear = SettingsDAO.getActiveYear();
        int activeTerm = SettingsDAO.getActiveTerm();
        activeSessionLabel.setText("Active Session: " + activeYear + " (Term " + activeTerm + ")");

        colName.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getStudentName()));

        colScore.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getScore()));
        colScore.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colScore.setOnEditCommit(event -> {
            double newScore = event.getNewValue();
            if (newScore < 0 || newScore > 100) {
                msgLabel.setText("Error: Score must be between 0 and 100.");
                msgLabel.setStyle("-fx-text-fill: red;");
                gradeTable.refresh();
            } else {
                event.getRowValue().setScore(newScore);
                msgLabel.setText("");
            }
        });

        gradeTable.setEditable(true);
        gradeTable.setItems(gradeList);

        classChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            loadSubjects();
            loadGrades();
        });
        subjectChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> loadGrades());
    }

    private void loadClasses() {
        if ("ADMIN".equals(currentUser.getRole())) {
            classChoice.setItems(FXCollections.observableArrayList(ClassDAO.getAll()));
        } else {
            classChoice.setItems(FXCollections.observableArrayList(TeacherDAO.getClassesForTeacher(currentUser.getTeacherId())));
        }
    }

    private void loadSubjects() {
        SchoolClass selectedClass = classChoice.getValue();
        if (selectedClass == null) {
            subjectChoice.getItems().clear();
            return;
        }

        if ("ADMIN".equals(currentUser.getRole())) {
            subjectChoice.setItems(FXCollections.observableArrayList(SubjectDAO.getAll()));
        } else {
            subjectChoice.setItems(FXCollections.observableArrayList(
                    ClassSubjectDAO.getSubjectsForTeacherInClass(currentUser.getTeacherId(), selectedClass.getId())
            ));
        }
    }

    private void loadGrades() {
        SchoolClass c = classChoice.getValue();
        Subject s = subjectChoice.getValue();

        if (c == null || s == null) {
            gradeList.clear();
            return;
        }

        int term = SettingsDAO.getActiveTerm();
        String year = SettingsDAO.getActiveYear();

        gradeList.setAll(GradeDAO.getGradesForClass(c.getId(), s.getId(), term, year));
    }

    @FXML
    private void handleSave() {
        if (gradeList.isEmpty()) return;

        Subject s = subjectChoice.getValue();
        int term = SettingsDAO.getActiveTerm();
        String year = SettingsDAO.getActiveYear();

        for (GradeRecord record : gradeList) {
            // DEBUG PRINT: This will show in your IDE console!
            System.out.println("SAVING GRADE -> Student ID: " + record.getStudentId() + " | Term: " + term + " | Year: " + year + " | Score: " + record.getScore());

            GradeDAO.saveGrade(record.getStudentId(), s.getId(), term, year, record.getScore(), currentUser.getId());

            com.school.dao.AuditDAO.log(
                    currentUser.getId(),
                    "GRADE_UPDATE",
                    "Saved " + s.getName() + " score for " + record.getStudentName() + " as " + record.getScore() + " (Term " + term + ")"
            );
        }

        msgLabel.setText("Grades saved successfully!");
        msgLabel.setStyle("-fx-text-fill: #27ae60;");
    }
}