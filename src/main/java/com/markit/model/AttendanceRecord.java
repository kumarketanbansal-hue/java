package com.markit.model;

public class AttendanceRecord {
    private String email;
    private String regNo;
    private String name;
    private String year;
    private String department;
    private String section;
    private String pin;
    private long at;

    public AttendanceRecord() {}

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public long getAt() { return at; }
    public void setAt(long at) { this.at = at; }
}