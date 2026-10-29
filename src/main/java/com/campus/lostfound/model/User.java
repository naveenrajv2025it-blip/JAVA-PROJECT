package com.campus.lostfound.model;

import java.sql.Timestamp;

public class User {
    private int userId;
    private String fullName;
    private String email;
    private String rollNumber;
    private String phone;
    private transient String passwordHash; // Transient prevents serialization in API JSON responses
    private String role; // STUDENT, STAFF, ADMIN
    private Timestamp createdAt;

    public User() {}

    public User(int userId, String fullName, String email, String rollNumber, String phone, String passwordHash, String role) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.rollNumber = rollNumber;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
