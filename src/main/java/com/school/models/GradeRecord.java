package com.school.models;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.DoubleProperty;

public class GradeRecord {
    private final int studentId;
    private final String studentName;
    private final DoubleProperty score;

    public GradeRecord(int studentId, String studentName, double score) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.score = new SimpleDoubleProperty(score);
    }

    public int getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public double getScore() { return score.get(); }
    public void setScore(double score) { this.score.set(score); }
    public DoubleProperty scoreProperty() { return score; }
}