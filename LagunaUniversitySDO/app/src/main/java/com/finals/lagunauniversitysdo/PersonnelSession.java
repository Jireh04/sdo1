package com.finals.lagunauniversitysdo;

import java.util.ArrayList;
import java.util.List;

public class PersonnelSession {
    // Personnel fields
    private static String personnelId;  // Personnel ID
    private static String personnelName;
    private static String email;
    private static Long contactNum;
    private static String department;

    // Unique identifier for personnel
    private static String personnelUniqueId; // Unique identifier for personnel

    // Added fields for scanned data
    private static String scannedPersonnelNo; // For storing scanned personnel number
    private static String scannedName; // For storing scanned name
    private static String scannedPosition; // For storing scanned position

    // Student fields
    private static String studentId; // Student ID
    private static String studentName;
    private static String studentEmail;
    private static Long studentContactNum;
    private static String studentDepartment;

    // Setters for Personnel
    public static void setPersonnelId(String id) {
        personnelId = id;
    }

    public static void setPersonnelUniqueId(String uniqueId) { // Method to set unique identifier for personnel
        personnelUniqueId = uniqueId;
    }

    public static void setPersonnelDetails(String name, String email, Long contact, String department) {
        personnelName = name;
        PersonnelSession.email = email;
        contactNum = contact;
        PersonnelSession.department = department; // Correctly set personnel department
    }

    // New individual setters for Personnel
    public static void setPersonnelName(String name) {
        personnelName = name;
    }

    public static void setEmail(String email) {
        PersonnelSession.email = email;
    }

    public static void setContactNum(Long contact) {
        contactNum = contact;
    }

    public static void setDepartment(String department) {
        PersonnelSession.department = department; // Use the class variable correctly
    }

    // New setters for scanned data
    public static void setScannedPersonnelNo(String personnelNo) {
        scannedPersonnelNo = personnelNo;
    }

    public static void setScannedName(String name) {
        scannedName = name;
    }

    public static void setScannedPosition(String position) {
        scannedPosition = position;
    }

    // Setters for Student
    public static void setStudentId(String id) {
        studentId = id;
    }

    public static void setStudentName(String name) {
        studentName = name;
    }

    public static void setStudentEmail(String email) {
        studentEmail = email;
    }

    public static void setStudentContactNum(Long contact) {
        studentContactNum = contact;
    }

    public static void setStudentDepartment(String department) {
        studentDepartment = department; // Set student department correctly
    }

    // Submission Method
    public static void submitData() {
        // Here, you would implement logic to handle data submission (e.g., save to a database)
        System.out.println("Submitting personnel and student data...");

        // Clear the session after submission
        clearSession();
        System.out.println("Data submitted and session cleared.");
    }

    // Getters for Personnel
    public static String getPersonnelId() {
        return personnelId;
    }

    public static String getPersonnelUniqueId() {
        return personnelUniqueId; // Get unique identifier for personnel
    }

    public static String getPersonnelName() {
        return personnelName;
    }

    public static String getEmail() {
        return email;
    }

    public static Long getContactNum() {
        return contactNum;
    }

    public static String getDepartment() {
        return department;
    }

    // New getters for scanned data
    public static String getScannedPersonnelNo() {
        return scannedPersonnelNo;
    }

    public static String getScannedName() {
        return scannedName;
    }

    public static String getScannedPosition() {
        return scannedPosition;
    }

    // Getters for Student
    public static String getStudentId() {
        return studentId;
    }

    public static String getStudentName() {
        return studentName;
    }

    public static String getStudentEmail() {
        return studentEmail;
    }

    public static Long getStudentContactNum() {
        return studentContactNum;
    }

    public static String getStudentDepartment() {
        return studentDepartment;
    }

    // Clear session
    public static void clearSession() {
        personnelId = null;
        personnelName = null;
        email = null;
        contactNum = null;
        department = null;

        // Clear unique identifier
        personnelUniqueId = null; // Clear unique identifier for personnel

        // Clear scanned data
        scannedPersonnelNo = null;
        scannedName = null;
        scannedPosition = null;

        // Clear student data
        studentId = null;
        studentName = null;
        studentEmail = null;
        studentContactNum = null;
        studentDepartment = null;
    }
}
