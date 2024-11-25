package com.finals.lagunauniversitysdo;

public class PrefectSession {
    private static String prefectId;
    private static String prefectName;
    private static String prefectEmail;
    private static Long prefectContactNum;
    private static String prefectDepartment;
    private static String prefectUsername;  // New variable for username
    private static String prefectPassword;  // New variable for password
    private static String studentId;  // New variable for student ID
    private static String personnelId;  // New variable for personnel ID

    // Getter and setter methods for all fields
    public static String getPrefectName() {
        return prefectName;
    }

    public static void setPrefectName(String name) {
        prefectName = name;
    }

    public static String getPrefectEmail() {
        return prefectEmail;
    }

    public static void setPrefectEmail(String email) {
        prefectEmail = email;
    }

    public static Long getPrefectContactNum() {
        return prefectContactNum;
    }

    public static void setPrefectContactNum(Long contactNum) {
        prefectContactNum = contactNum;
    }

    public static String getPrefectDepartment() {
        return prefectDepartment;
    }

    public static void setPrefectDepartment(String department) {
        prefectDepartment = department;
    }

    public static String getPrefectUsername() {
        return prefectUsername;
    }

    public static void setPrefectUsername(String username) {
        prefectUsername = username;
    }

    public static String getPrefectPassword() {
        return prefectPassword;
    }

    public static void setPrefectPassword(String password) {
        prefectPassword = password;
    }

    public static String getPrefectId() {
        return prefectId;
    }

    public static void setPrefectId(String id) {
        prefectId = id;
    }

    // Getter and setter methods for studentId and personnelId
    public static String getStudentId() {
        return studentId;
    }

    public static void setStudentId(String id) {
        studentId = id;
    }

    public static String getPersonnelId() {
        return personnelId;
    }

    public static void setPersonnelId(String id) {
        personnelId = id;
    }

    // Method to store all prefect details at once
    public static void setPrefectDetails(String name, String email, Long contactNum, String department, String username, String password) {
        setPrefectName(name);
        setPrefectEmail(email);
        setPrefectContactNum(contactNum);
        setPrefectDepartment(department);
        setPrefectUsername(username);
        setPrefectPassword(password);
    }

    // Method to check if a prefect is logged in
    public static boolean isLoggedIn() {
        // If the prefectId is not null, we assume that the prefect is logged in
        return prefectId != null && !prefectId.isEmpty();
    }

    // Optionally, you can add a method to clear the session when needed
    public static void clearSession() {
        prefectId = null;
        prefectName = null;
        prefectEmail = null;
        prefectContactNum = null;
        prefectDepartment = null;
        prefectUsername = null;
        prefectPassword = null;
        studentId = null;  // Clear studentId
        personnelId = null;  // Clear personnelId
    }
}
