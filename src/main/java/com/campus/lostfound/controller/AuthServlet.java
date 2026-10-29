package com.campus.lostfound.controller;

import com.campus.lostfound.model.User;
import com.campus.lostfound.service.AuthService;
import com.campus.lostfound.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("currentUser") != null) {
            User user = (User) session.getAttribute("currentUser");
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, user);
        } else {
            JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if ("/logout".equalsIgnoreCase(pathInfo)) {
            HttpSession session = req.getSession(false);
            if (session != null) session.invalidate();
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, "{\"message\": \"Logged out successfully\"}");
            return;
        }

        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = req.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
            }

            Map<String, String> payload = JsonUtil.fromJson(sb.toString(), Map.class);
            if (payload == null) {
                JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing request payload.");
                return;
            }

            if ("/register".equalsIgnoreCase(pathInfo)) {
                User registered = authService.register(
                    payload.get("fullName"),
                    payload.get("email"),
                    payload.get("rollNumber"),
                    payload.get("phone"),
                    payload.get("password"),
                    payload.get("role")
                );

                HttpSession session = req.getSession(true);
                session.setAttribute("currentUser", registered);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("user", registered);
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_CREATED, response);
                return;
            }

            // Passwordless student email login (or password login if password provided)
            String email = payload.get("email");
            String password = payload.get("password");

            User authenticatedUser;
            if (password != null && !password.trim().isEmpty()) {
                authenticatedUser = authService.authenticate(email, password);
            } else {
                authenticatedUser = authService.loginWithStudentEmail(email);
            }

            HttpSession session = req.getSession(true);
            session.setAttribute("currentUser", authenticatedUser);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", authenticatedUser);
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, response);

        } catch (IllegalArgumentException e) {
            JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication error: " + e.getMessage());
        }
    }
}
