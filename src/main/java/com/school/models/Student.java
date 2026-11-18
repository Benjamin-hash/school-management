package com.school.models;

public class Student {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String dateOfBirth;
    private Integer classId;
    private String className;      // filled by the JOIN — display only
    private String admissionDate;
    private String status;

    public Student(int id, String firstName, String lastName, String email, String phone,
                   String dateOfBirth, Integer classId, String className,
                   String admissionDate, String status) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.classId = classId;
        this.className = className;
        this.admissionDate = admissionDate;
        this.status = status;
    }

    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getDateOfBirth() { return dateOfBirth; }
    public Integer getClassId() { return classId; }
    public String getClassName() { return className; }
    public String getAdmissionDate() { return admissionDate; }
    public String getStatus() { return status; }

    public void setId(int id) { this.id = id; }
    public void setFirstName(String v) { firstName = v; }
    public void setLastName(String v) { lastName = v; }
    public void setEmail(String v) { email = v; }
    public void setPhone(String v) { phone = v; }
    public void setDateOfBirth(String v) { dateOfBirth = v; }
    public void setClassId(Integer v) { classId = v; }
    public void setStatus(String v) { status = v; }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}