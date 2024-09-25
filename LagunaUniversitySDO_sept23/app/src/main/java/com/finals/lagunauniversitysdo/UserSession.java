package com.finals.lagunauniversitysdo;

public class UserSession {
    private static String stud_id;  // Student ID
    private static String studentName;
    private static String email;
    private static Long contactNum;
    private static String program;

    // Setters
    public static void setStudId(String id) {
        stud_id = id;
    }

    public static void setStudentDetails(String name, String email, Long contact, String program) {
        studentName = name;
        UserSession.email = email;
        contactNum = contact;
        UserSession.program = program;
    }

    // Getters
    public static String getStudId() {
        return stud_id;
    }

    public static String getStudentName() {
        return studentName;
    }

    public static String getEmail() {
        return email;
    }

    public static Long getContactNum() {
        return contactNum;
    }

    public static String getProgram() {
        return program;
    }

    // Clear session
    public static void clearSession() {
        stud_id = null;
        studentName = null;
        email = null;
        contactNum = null;
        program = null;
    }
}
