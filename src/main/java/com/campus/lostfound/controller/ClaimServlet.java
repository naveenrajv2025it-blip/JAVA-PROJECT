package com.campus.lostfound.controller;

import com.campus.lostfound.model.Claim;
import com.campus.lostfound.model.User;
import com.campus.lostfound.service.ClaimService;
import com.campus.lostfound.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/claims/*")
public class ClaimServlet extends HttpServlet {
    private final ClaimService claimService = new ClaimService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String claimantIdParam = req.getParameter("claimantId");
        String status = req.getParameter("status");

        try {
            if (claimantIdParam != null && !claimantIdParam.trim().isEmpty()) {
                int claimantId = Integer.parseInt(claimantIdParam.trim());
                List<Claim> claims = claimService.getClaimsByClaimant(claimantId);
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, claims);
            } else {
                List<Claim> claims = claimService.getAllClaims(status);
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, claims);
            }
        } catch (NumberFormatException e) {
            JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid claimantId parameter.");
        } catch (Exception e) {
            System.err.println("ClaimServlet doGet error: " + e.getMessage());
            JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred retrieving claims.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = req.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
            }

            Map<String, Object> payload = JsonUtil.fromJson(sb.toString(), Map.class);
            if (payload == null || !payload.containsKey("itemId")) {
                JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing itemId or payload.");
                return;
            }

            int itemId = ((Double) payload.get("itemId")).intValue();
            String proofDescription = (String) payload.get("proofDescription");
            String proofAttachmentUrl = (String) payload.get("proofAttachmentUrl");

            HttpSession session = req.getSession(false);
            User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

            if (currentUser == null) {
                // In Phase 3 standalone test mode, allow fallback demo user if no session yet
                currentUser = new User(1, "Aarav Sharma", "aarav.sharma@campus.edu", "2023CS1042", "+1555100200", "", "STUDENT");
            }

            Claim created = claimService.submitClaim(itemId, proofDescription, proofAttachmentUrl, currentUser);
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_CREATED, created);

        } catch (IllegalArgumentException e) {
            JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            System.err.println("ClaimServlet doPost error: " + e.getMessage());
            JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal error occurred while submitting your claim.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = req.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
            }

            Map<String, Object> payload = JsonUtil.fromJson(sb.toString(), Map.class);
            if (payload == null || !payload.containsKey("claimId") || !payload.containsKey("itemId")) {
                JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing claimId or itemId.");
                return;
            }

            int claimId = ((Double) payload.get("claimId")).intValue();
            int itemId = ((Double) payload.get("itemId")).intValue();
            String status = (String) payload.get("claimStatus");
            String remarks = (String) payload.get("adminRemarks");

            HttpSession session = req.getSession(false);
            User reviewer = (session != null) ? (User) session.getAttribute("currentUser") : null;

            if (reviewer == null) {
                // In Phase 3 standalone test mode, fallback to Security Officer
                reviewer = new User(5, "Campus Security Officer", "security.admin@campus.edu", "SEC-001", "", "", "ADMIN");
            }

            boolean success = claimService.reviewClaim(claimId, itemId, status, remarks, reviewer);
            if (success) {
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, "{\"success\": true, \"message\": \"Claim reviewed successfully.\"}");
            } else {
                JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Could not process claim review.");
            }

        } catch (SecurityException e) {
            JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            System.err.println("ClaimServlet doPut error: " + e.getMessage());
            JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal error occurred while reviewing the claim.");
        }
    }
}
