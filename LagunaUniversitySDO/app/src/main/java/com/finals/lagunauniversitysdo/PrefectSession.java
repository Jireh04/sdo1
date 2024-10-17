package com.finals.lagunauniversitysdo;

public class PrefectSession {
    private static String prefectId;
    private static String prefectName;
    private static String prefectEmail;
    private static Long prefectContactNum;
    private static String prefectDepartment;
    private static String prefectUsername;  // New variable for username
    private static String prefectPassword;  // New variable for password

    // Getter and setter methods for all fields
    public static String getPrefectId() {
        return prefectId;
    }

    public static void setPrefectId(String id) {
        prefectId = id;
    }

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

    // Method to store all prefect details at once
    public static void setPrefectDetails(String name, String email, Long contactNum, String department, String username, String password) {
        setPrefectName(name);
        setPrefectEmail(email);
        setPrefectContactNum(contactNum);
        setPrefectDepartment(department);
        setPrefectUsername(username);
        setPrefectPassword(password);
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
    }
}