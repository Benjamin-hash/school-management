package com.school.models;

public class User {
    private int id;
    private String username;
    private String role;
    private Integer teacherId;   // null for admin — links a teacher login to their record

    public User(int id, String username, String role, Integer teacherId) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.teacherId = teacherId;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public Integer getTeacherId() { return teacherId; }
}