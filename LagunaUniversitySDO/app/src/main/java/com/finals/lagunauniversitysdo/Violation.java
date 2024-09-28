package com.finals.lagunauniversitysdo;

import com.google.firebase.Timestamp;

public class Violation {
    private String studentNo;
    private String studentName;
    private String block;
    private String offenseType;
    private String remarks;
    private String referrer;
    private String location;
    private String violation;
    private Timestamp dateTime;

    // Default constructor (required for Firestore)
    public Violation() {
        // Initialize with default values if necessary
    }

    // Constructor with all fields
    public Violation(String studentNo, String studentName, String block, String offenseType, String remarks, String location, String violation, String referrer, Timestamp dateTime) {
        this.studentNo = studentNo;
        this.studentName = studentName;
        this.block = block;
        this.offenseType = offenseType;
        this.remarks = remarks;
        this.referrer = referrer;
        this.location = location;
        this.violation = violation;
        this.dateTime = dateTime;
    }

    // Getters
    public String getStudentNo() {
        return studentNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getBlock() {
        return block;
    }

    public String getOffenseType() {
        return offenseType;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getReferrer() {
        return referrer;
    }

    public String getLocation() {
        return location;
    }

    public String getViolation() {
        return violation;
    }

    public Timestamp getDateTime() {
        return dateTime;
    }

    // Setters
    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public void setOffenseType(String offenseType) {
        this.offenseType = offenseType;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setViolation(String violation) {
        this.violation = violation;
    }

    public void setDateTime(Timestamp dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return "Violation{" +
                "studentNo='" + studentNo + '\'' +
                ", studentName='" + studentName + '\'' +
                ", block='" + block + '\'' +
                ", offenseType='" + offenseType + '\'' +
                ", remarks='" + remarks + '\'' +
                ", referrer='" + referrer + '\'' +
                ", location='" + location + '\'' +
                ", violation='" + violation + '\'' +
                ", dateTime=" + dateTime +
                '}';
    }
}
