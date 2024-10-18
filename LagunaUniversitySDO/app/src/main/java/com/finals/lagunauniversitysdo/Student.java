package com.finals.lagunauniversitysdo;

import java.util.Objects;

public class Student {
    private String name;
    private String department;
    private String email;
    private Long contact;
    private String id;

    public Student(String name, String department, String email, Long contact, String id) {
        this.name = name;
        this.department = department;
        this.email = email;
        this.contact = contact;
        this.id = id;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public String getEmail() {
        return email;
    }

    public Long getContact() {
        return contact;
    }

    public String getId() {
        return id;
    }

    // Override equals method
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Check reference equality
        if (o == null || getClass() != o.getClass()) return false; // Check type

        Student student = (Student) o; // Cast to Student
        return Objects.equals(id, student.id); // Compare based on unique ID
    }

    // Override hashCode method
    @Override
    public int hashCode() {
        return Objects.hash(id); // Use the unique ID for hash code
    }

    // Optional: Override toString for better representation
    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", department='" + department + '\'' +
                ", email='" + email + '\'' +
                ", contact=" + contact +
                ", id='" + id + '\'' +
                '}';
    }
}
