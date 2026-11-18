package com.school.models;

public class FeeRecord {
    private final int feeId;
    private final int studentId;
    private final String studentName;
    private final double amountDue;
    private final double amountPaid;
    private final String status;

    public FeeRecord(int feeId, int studentId, String studentName, double amountDue, double amountPaid, String status) {
        this.feeId = feeId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.amountDue = amountDue;
        this.amountPaid = amountPaid;
        this.status = status;
    }

    public int getFeeId() { return feeId; }
    public int getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public double getAmountDue() { return amountDue; }
    public double getAmountPaid() { return amountPaid; }

    // Automatically calculates remaining balance
    public double getBalance() { return amountDue - amountPaid; }
    public String getStatus() { return status; }
}