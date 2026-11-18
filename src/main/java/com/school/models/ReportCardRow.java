package com.school.models;

public class ReportCardRow {
    private final String subjectName;
    private final double score;

    public ReportCardRow(String subjectName, double score) {
        this.subjectName = subjectName;
        this.score = score;
    }

    public String getSubjectName() { return subjectName; }
    public double getScore() { return score; }
    
    // Automatically calculate the Grade Letter
    public String getGradeLetter() {
        if (score >= 80) return "A";
        if (score >= 70) return "B";
        if (score >= 60) return "C";
        if (score >= 50) return "D";
        if (score >= 40) return "E";
        return "F";
    }
}