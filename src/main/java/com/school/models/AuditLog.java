package com.school.models;

public class AuditLog {
    private final String timestamp;
    private final String user;
    private final String action;
    private final String details;

    public AuditLog(String timestamp, String user, String action, String details) {
        this.timestamp = timestamp;
        this.user = user;
        this.action = action;
        this.details = details;
    }

    public String getTimestamp() { return timestamp; }
    public String getUser() { return user; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
}