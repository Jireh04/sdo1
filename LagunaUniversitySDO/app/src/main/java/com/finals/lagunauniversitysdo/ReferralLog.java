package com.finals.lagunauniversitysdo;

import java.io.Serializable;

public  class ReferralLog {
    private String studentId;
    private String status;
    private String date;
    private String studentName;
    private String studentProgram;
    private String term;
    private String userConcern;
    private String violation;
    private String referrerType;  // New field to track the referrer type

    public ReferralLog(String studentId, String status, String date, String studentName, String studentProgram,
                       String term, String userConcern, String violation, String referrerType) {
        this.studentId = studentId;
        this.status = status;
        this.date = date;
        this.studentName = studentName;
        this.studentProgram = studentProgram;
        this.term = term;
        this.userConcern = userConcern;
        this.violation = violation;
        this.referrerType = referrerType;  // Initialize the referrerType
    }

    // Getters for all fields
    public String getStudentId() {
        return studentId;
    }

    public String getStatus() {
        return status;
    }

    public String getDate() {
        return date;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentProgram() {
        return studentProgram;
    }

    public String getTerm() {
        return term;
    }

    public String getUserConcern() {
        return userConcern;
    }

    public String getViolation() {
        return violation;
    }

    public String getReferrerType() {
        return referrerType;  // Getter for referrerType
    }
}
