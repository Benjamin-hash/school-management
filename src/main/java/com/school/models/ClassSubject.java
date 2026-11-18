package com.school.models;

public class ClassSubject {
    private int id;
    private String className;
    private String subjectName;
    private String teacherName;

    public ClassSubject(int id, String className, String subjectName, String teacherName) {
        this.id = id;
        this.className = className;
        this.subjectName = subjectName;
        this.teacherName = teacherName;
    }

    public int getId() { return id; }
    public String getClassName() { return className; }
    public String getSubjectName() { return subjectName; }
    public String getTeacherName() { return teacherName; }
}