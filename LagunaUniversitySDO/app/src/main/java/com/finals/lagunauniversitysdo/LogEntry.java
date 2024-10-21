package com.finals.lagunauniversitysdo;


public class LogEntry {
    private String insightsStudent;
    private String status;
    private String studentId;
    private String studentName;
    private String studentProgram;
    private String term;
    private String userConcern;
    private String violation;

    // Constructor
    public LogEntry(String insightsStudent, String status, String studentId, String studentName,
                    String studentProgram, String term, String userConcern, String violation) {
        this.insightsStudent = insightsStudent;
        this.status = status;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentProgram = studentProgram;
        this.term = term;
        this.userConcern = userConcern;
        this.violation = violation;
    }

    // Getters for all fields
    public String getInsightsStudent() { return insightsStudent; }
    public String getStatus() { return status; }
    public String getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getStudentProgram() { return studentProgram; }
    public String getTerm() { return term; }
    public String getUserConcern() { return userConcern; }
    public String getViolation() { return violation; }
}
