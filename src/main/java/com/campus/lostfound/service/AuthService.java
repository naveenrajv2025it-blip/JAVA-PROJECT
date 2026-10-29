package com.campus.lostfound.service;

import com.campus.lostfound.dao.DAOFactory;
import com.campus.lostfound.dao.UserDAO;
import com.campus.lostfound.model.User;
import com.campus.lostfound.util.PasswordUtil;
import com.campus.lostfound.util.ValidationUtil;

public class AuthService {
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = DAOFactory.getUserDAO();
    }

    public User authenticate(String email, String password) throws IllegalArgumentException {
        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid college email. Please use your official Rajalakshmi email: name.initial.year.dept@rajalakshmi.edu.in");
        }
        if (!ValidationUtil.isNotEmpty(password)) {
            throw new IllegalArgumentException("Password is required.");
        }

        User user = userDAO.findByEmail(email.trim());
        if (user == null || !PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        return user;
    }

    public User register(String fullName, String email, String rollNumber, String phone, String password, String role) throws IllegalArgumentException {
        if (!ValidationUtil.isNotEmpty(fullName)) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Only Rajalakshmi College emails in the format name.initial.year.dept@rajalakshmi.edu.in are allowed.");
        }
        if (!ValidationUtil.isNotEmpty(rollNumber)) {
            throw new IllegalArgumentException("Roll number / Campus ID is required.");
        }
        if (!ValidationUtil.hasMinLength(password, 6)) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }

        if (userDAO.findByEmail(email.trim()) != null) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        String assignedRole = ValidationUtil.isValidRole(role) ? role.toUpperCase() : "STUDENT";
        String hashedPassword = PasswordUtil.hashPassword(password);

        User newUser = new User(0, fullName.trim(), email.trim(), rollNumber.trim(), phone, hashedPassword, assignedRole);
        return userDAO.createUser(newUser);
    }

    public User loginWithStudentEmail(String email) throws IllegalArgumentException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter your college email address.");
        }
        String cleanEmail = email.trim().toLowerCase();
        if (!cleanEmail.endsWith("@rajalakshmi.edu.in")) {
            throw new IllegalArgumentException("Please enter a valid Rajalakshmi College email ending with @rajalakshmi.edu.in");
        }

        User user = userDAO.findByEmail(cleanEmail);
        if (user != null) {
            return user;
        }

        // Auto-provision student profile
        String localPart = cleanEmail.substring(0, cleanEmail.indexOf('@'));
        String[] tokens = localPart.split("\\.");
        StringBuilder nameBuilder = new StringBuilder();
        String rollNumber = localPart.length() > 30 ? localPart.substring(0, 30) : localPart;

        for (String token : tokens) {
            if (token.matches("\\d+")) {
                continue; // skip batch year
            }
            if (token.equalsIgnoreCase("cse") || token.equalsIgnoreCase("it") || token.equalsIgnoreCase("ece") 
                || token.equalsIgnoreCase("eee") || token.equalsIgnoreCase("mech") || token.equalsIgnoreCase("aids")
                || token.equalsIgnoreCase("aiml") || token.equalsIgnoreCase("csbs") || token.equalsIgnoreCase("bme")
                || token.equalsIgnoreCase("biotech") || token.equalsIgnoreCase("civil") || token.equalsIgnoreCase("aero")) {
                continue; // skip dept code
            }
            if (!token.isEmpty()) {
                if (nameBuilder.length() > 0) nameBuilder.append(" ");
                nameBuilder.append(Character.toUpperCase(token.charAt(0)));
                if (token.length() > 1) {
                    nameBuilder.append(token.substring(1));
                }
            }
        }

        String fullName = nameBuilder.toString().trim();
        if (fullName.isEmpty()) {
            fullName = "REC Student";
        }

        User newUser = new User(0, fullName, cleanEmail, rollNumber, "", "STUDENT_VERIFIED", "STUDENT");
        return userDAO.createUser(newUser);
    }
}
