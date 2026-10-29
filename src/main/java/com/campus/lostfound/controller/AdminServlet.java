package com.campus.lostfound.controller;

import com.campus.lostfound.dao.DAOFactory;
import com.campus.lostfound.model.Claim;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/admin/metrics")
public class AdminServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<Item> items = DAOFactory.getItemDAO().findAll(null, null, null, null, null, null);
            List<Claim> claims = DAOFactory.getClaimDAO().findAll(null);

            int totalReported = items.size();
            int totalResolved = (int) items.stream().filter(it -> "RESOLVED".equalsIgnoreCase(it.getStatus())).count();
            int activeLost = (int) items.stream().filter(it -> "LOST".equalsIgnoreCase(it.getItemType()) && "OPEN".equalsIgnoreCase(it.getStatus())).count();
            int activeFound = (int) items.stream().filter(it -> "FOUND".equalsIgnoreCase(it.getItemType()) && "OPEN".equalsIgnoreCase(it.getStatus())).count();
            int pendingClaims = (int) claims.stream().filter(c -> "PENDING".equalsIgnoreCase(c.getClaimStatus())).count();

            int recoveryRate = totalReported > 0 ? (int) Math.round(((double) totalResolved / totalReported) * 100) : 0;

            Map<String, Object> metrics = new HashMap<>();
            metrics.put("totalReported", totalReported);
            metrics.put("totalResolved", totalResolved);
            metrics.put("activeLost", activeLost);
            metrics.put("activeFound", activeFound);
            metrics.put("pendingClaims", pendingClaims);
            metrics.put("recoveryRate", recoveryRate);

            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, metrics);
        } catch (Exception e) {
            System.err.println("AdminServlet error: " + e.getMessage());
            JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to load administrative metrics.");
        }
    }
}
