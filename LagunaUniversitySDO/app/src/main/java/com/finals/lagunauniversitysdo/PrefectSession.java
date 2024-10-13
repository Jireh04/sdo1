package com.finals.lagunauniversitysdo;

public class PrefectSession {
    // Prefect fields (since there's only one account)
    private static String prefectId; // Prefect ID
    private static String prefectName;
    private static String email;
    private static Long contactNum;
    private static String department;

    // Student fields
    private static String studentId; // Student ID
    private static String studentName;
    private static String studentEmail;
    private static Long studentContactNum;
    private static String studentDepartment;

    // Setters for Prefect
    public static void setPrefectId(String id) {
        prefectId = id;
    }


    public static void setPrefectDetails(String name, String email, Long contact, String department) {
        prefectName = name;
        PrefectSession.email = email;
        contactNum = contact;
        PrefectSession.department = department; // Correctly set prefect department
    }

    // Individual setters for Prefect
    public static void setPrefectName(String name) {
        prefectName = name;
    }

    public static void setEmail(String email) {
        PrefectSession.email = email;
    }

    public static void setContactNum(Long contact) {
        contactNum = contact;
    }

    public static void setDepartment(String department) {
        PrefectSession.department = department; // Use the class variable correctly
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
        System.out.println("Submitting prefect and student data...");

        // Clear the session after submission
        clearSession();
        System.out.println("Data submitted and session cleared.");
    }

    // Getters for Prefect
    public static String getPrefectId() {
        return prefectId;
    }

    public static String getPrefectName() {
        return prefectName;
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
        prefectId = null;
        prefectName = null;
        email = null;
        contactNum = null;
        department = null;

        // Clear student data
        studentId = null;
        studentName = null;
        studentEmail = null;
        studentContactNum = null;
        studentDepartment = null;
    }
}
