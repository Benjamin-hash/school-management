package com.school.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AttendanceRecord {
    private final int studentId;
    private final String studentName;
    private final StringProperty status;

    public AttendanceRecord(int studentId, String studentName, String status) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.status = new SimpleStringProperty(status);
    }

    public int getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }
    public StringProperty statusProperty() { return status; }
}