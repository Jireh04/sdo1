package com.finals.lagunauniversitysdo;

public class student_displaying {
    private String studentNo;
    private String name;
    private String programYearBlock;

    // Constructor
    public student_displaying (String studentNo, String name, String programYearBlock) {
        this.studentNo = studentNo;
        this.name = name;
        this.programYearBlock = programYearBlock;
    }

    // Getters
    public String getStudentNo() {
        return studentNo;
    }

    public String getName() {
        return name;
    }

    public String getProgramYearBlock() {
        return programYearBlock;
    }
}

