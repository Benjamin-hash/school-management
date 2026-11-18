package com.school.models;

public class SchoolClass {
    private int id;
    private String name;
    private String level;
    private String section;
    private int levelOrder;
    private int studentCount;
    private Integer homeroomTeacherId; // NEW

    // Constructor for UI (without homeroom)
    public SchoolClass(int id, String name, String level, String section, int levelOrder, int studentCount) {
        this(id, name, level, section, levelOrder, studentCount, null);
    }

    // Full Constructor
    public SchoolClass(int id, String name, String level, String section, int levelOrder, int studentCount, Integer homeroomTeacherId) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.section = section;
        this.levelOrder = levelOrder;
        this.studentCount = studentCount;
        this.homeroomTeacherId = homeroomTeacherId;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getLevel() { return level; }
    public String getSection() { return section; }
    public int getLevelOrder() { return levelOrder; }
    public int getStudentCount() { return studentCount; }
    public Integer getHomeroomTeacherId() { return homeroomTeacherId; }

    public void setId(int id) { this.id = id; }
    public void setName(String v) { name = v; }
    public void setLevel(String v) { level = v; }
    public void setSection(String v) { section = v; }
    public void setLevelOrder(int v) { levelOrder = v; }

    @Override
    public String toString() { return name; }

    public void setHomeroomTeacherId(Integer homeroomTeacherId) {
        this.homeroomTeacherId = homeroomTeacherId;
    }
    private String homeroomTeacherName; // Display only

    public String getHomeroomTeacherName() { return homeroomTeacherName; }
    public void setHomeroomTeacherName(String name) { this.homeroomTeacherName = name; }
}