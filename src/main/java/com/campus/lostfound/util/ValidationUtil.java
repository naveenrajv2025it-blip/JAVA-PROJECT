package com.campus.lostfound.util;

import java.util.regex.Pattern;

public class ValidationUtil {
    // Rajalakshmi College Email Format: name.initial.year.dept@rajalakshmi.edu.in
    // Example: naveen.v.2023.cse@rajalakshmi.edu.in
    private static final Pattern RAJALAKSHMI_EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)*\\.\\d{4}\\.[a-zA-Z]+@rajalakshmi\\.edu\\.in$",
        Pattern.CASE_INSENSITIVE
    );

    public static boolean isValidEmail(String email) {
        return email != null && RAJALAKSHMI_EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isRajalakshmiEmail(String email) {
        return isValidEmail(email);
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean hasMinLength(String value, int minLength) {
        return value != null && value.trim().length() >= minLength;
    }

    public static boolean isValidRole(String role) {
        if (role == null) return false;
        String r = role.trim().toUpperCase();
        return r.equals("STUDENT") || r.equals("STAFF") || r.equals("ADMIN");
    }

    public static boolean isValidItemType(String type) {
        if (type == null) return false;
        String t = type.trim().toUpperCase();
        return t.equals("LOST") || t.equals("FOUND");
    }

    public static boolean isValidItemStatus(String status) {
        if (status == null) return false;
        String s = status.trim().toUpperCase();
        return s.equals("OPEN") || s.equals("CLAIM_PENDING") || s.equals("RESOLVED") || s.equals("CLOSED");
    }

    public static boolean isValidClaimStatus(String status) {
        if (status == null) return false;
        String s = status.trim().toUpperCase();
        return s.equals("PENDING") || s.equals("APPROVED") || s.equals("REJECTED");
    }
}
