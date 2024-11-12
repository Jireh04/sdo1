package com.finals.lagunauniversitysdo;

import android.content.Context;
import android.content.SharedPreferences;

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

    private static final String PREF_NAME = "UserSession";  // SharedPreferences file name
    private static SharedPreferences sharedPreferences;  // SharedPreferences instance

    // Initialize SharedPreferences
    public static void init(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    // Setters
    public static void setStudId(String id) {
        student_id = id;
        sharedPreferences.edit().putString("USER_ID", id).apply();  // Save to SharedPreferences
    }

    public static void setStudentDetails(String firstName, String lastName, String email, Long contacts, String program, String name) {
        studentName = name;
        UserSession.email = email;
        contactNum = contacts;
        UserSession.program = program;
        UserSession.firstName = firstName;
        UserSession.lastName = lastName;

        // Save to SharedPreferences
        sharedPreferences.edit()
                .putString("STUDENT_NAME", name)
                .putString("FIRST_NAME", firstName)
                .putString("LAST_NAME", lastName)
                .putString("EMAIL", email)
                .putLong("CONTACT_NUM", contacts)
                .putString("PROGRAM", program)
                .apply();
    }

    // New setters for scanned data
    public static void setScannedStudentNo(String studentNo) {
        scannedStudentNo = studentNo;
        sharedPreferences.edit().putString("SCANNED_STUDENT_NO", studentNo).apply();
    }

    public static void setScannedName(String name) {
        scannedName = name;
        sharedPreferences.edit().putString("SCANNED_NAME", name).apply();
    }

    public static void setScannedBlock(String block) {
        scannedBlock = block;
        sharedPreferences.edit().putString("SCANNED_BLOCK", block).apply();
    }

    // Getters
    public static String getStudentId() {
        return student_id != null ? student_id : sharedPreferences.getString("USER_ID", null);
    }

    public static String getStudentName() {
        return studentName != null ? studentName : sharedPreferences.getString("STUDENT_NAME", null);
    }

    public static String getFirstName() {
        return firstName != null ? firstName : sharedPreferences.getString("FIRST_NAME", null);
    }

    public static String getLastName() {
        return lastName != null ? lastName : sharedPreferences.getString("LAST_NAME", null);
    }

    public static String getEmail() {
        return email != null ? email : sharedPreferences.getString("EMAIL", null);
    }

    public static Long getContactNum() {
        return contactNum != null ? contactNum : sharedPreferences.getLong("CONTACT_NUM", 0);
    }

    public static String getProgram() {
        return program != null ? program : sharedPreferences.getString("PROGRAM", null);
    }

    // New getters for scanned data
    public static String getScannedStudentNo() {
        return scannedStudentNo != null ? scannedStudentNo : sharedPreferences.getString("SCANNED_STUDENT_NO", null);
    }

    public static String getScannedName() {
        return scannedName != null ? scannedName : sharedPreferences.getString("SCANNED_NAME", null);
    }

    public static String getScannedBlock() {
        return scannedBlock != null ? scannedBlock : sharedPreferences.getString("SCANNED_BLOCK", null);
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

        // Clear SharedPreferences
        sharedPreferences.edit().clear().apply();
    }

    // Check if user is logged in by checking if student_id exists
    public static boolean isUserLoggedIn() {
        return student_id != null || sharedPreferences.contains("USER_ID");
    }
}
