package com.finals.lagunauniversitysdo;

public class UserSession {
    private static String student_id;  // Student ID
    private static String studentName;
    private static String firstName;
    private static String lastName;
    private static String email;
    private static Long contactNum;
    private static String program;

    // Added fields for scanned data
    private static String scannedStudentNo; // For storing scanned student number
    private static String scannedName; // For storing scanned name
    private static String scannedBlock; // For storing scanned block

    // Setters
    public static void setStudId(String id) {
        student_id = id;
    }

    public static void setStudentDetails(String firstName, String lastName, String email, Long contacts, String program, String name) {
        studentName = name;
        UserSession.email = email;
        contactNum = contacts;
        UserSession.program = program;
        UserSession.firstName = firstName;
        UserSession.lastName = lastName;
    }

    // New setters for scanned data
    public static void setScannedStudentNo(String studentNo) {
        scannedStudentNo = studentNo;
    }

    public static void setScannedName(String name) {
        scannedName = name;
    }

    public static void setScannedBlock(String block) {
        scannedBlock = block;
    }

    // Getters
    public static String getStudentId() {
        return student_id;
    }

    public static String getStudentName() {
        return studentName;
    }

    public static String getFirstName() {
        return firstName;
    }

    public static String getLastName() {
        return lastName;
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

    // New getters for scanned data
    public static String getScannedStudentNo() {
        return scannedStudentNo;
    }

    public static String getScannedName() {
        return scannedName;
    }

    public static String getScannedBlock() {
        return scannedBlock;
    }

    // Clear session
    public static void clearSession() {
        student_id = null;
        studentName = null;
        firstName = null;
        lastName = null;
        email = null;
        contactNum = null;
        program = null;

        // Clear scanned data
        scannedStudentNo = null;
        scannedName = null;
        scannedBlock = null;
    }
}
