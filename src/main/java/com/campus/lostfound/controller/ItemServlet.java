package com.campus.lostfound.controller;

import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.User;
import com.campus.lostfound.service.ItemService;
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

@WebServlet("/api/items/*")
public class ItemServlet extends HttpServlet {
    private final ItemService itemService = new ItemService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        try {
            if (idParam != null && !idParam.trim().isEmpty()) {
                int itemId = Integer.parseInt(idParam.trim());
                Item item = itemService.getItemById(itemId);
                if (item != null) {
                    JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, item);
                } else {
                    JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Item not found with ID #" + itemId);
                }
            } else {
                String type = req.getParameter("type");
                String status = req.getParameter("status");
                String catParam = req.getParameter("categoryId");
                String locParam = req.getParameter("locationId");
                String query = req.getParameter("query");
                String sortBy = req.getParameter("sortBy");

                Integer categoryId = (catParam != null && !catParam.isEmpty() && !"ALL".equalsIgnoreCase(catParam)) ? Integer.parseInt(catParam) : null;
                Integer locationId = (locParam != null && !locParam.isEmpty() && !"ALL".equalsIgnoreCase(locParam)) ? Integer.parseInt(locParam) : null;

                List<Item> items = itemService.searchItems(type, status, categoryId, locationId, query, sortBy);
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, items);
            }
        } catch (NumberFormatException e) {
            JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid numeric ID format.");
        } catch (Exception e) {
            System.err.println("ItemServlet doGet error: " + e.getMessage());
            JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred retrieving items.");
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

            Item newItem = JsonUtil.fromJson(sb.toString(), Item.class);
            if (newItem == null) {
                JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing JSON payload.");
                return;
            }

            if (newItem.getItemType() == null) {
                try {
                    Map<String, Object> map = JsonUtil.fromJson(sb.toString(), Map.class);
                    if (map != null && map.containsKey("type")) {
                        newItem.setItemType(String.valueOf(map.get("type")));
                    }
                } catch (Exception ignored) {}
            }

            HttpSession session = req.getSession(false);
            User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

            if (currentUser == null) {
                JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "Please sign in with your college email to report an item.");
                return;
            }

            Item created = itemService.reportItem(newItem, currentUser);
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_CREATED, created);

        } catch (IllegalArgumentException e) {
            JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            System.err.println("ItemServlet doPost error: " + e.getMessage());
            JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred submitting the report.");
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
            if (payload == null || !payload.containsKey("itemId")) {
                JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing itemId in request body.");
                return;
            }

            int itemId = ((Double) payload.get("itemId")).intValue();
            String status = (String) payload.get("status");

            HttpSession session = req.getSession(false);
            User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

            if ("RESOLVED".equalsIgnoreCase(status)) {
                boolean success = itemService.resolveItem(itemId, currentUser);
                if (success) {
                    JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, "{\"success\": true, \"message\": \"Item marked as resolved.\"}");
                } else {
                    JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Could not update status.");
                }
            } else {
                JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Unsupported status update.");
            }

        } catch (SecurityException e) {
            JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            System.err.println("ItemServlet doPut error: " + e.getMessage());
            JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred updating the item status.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing item id parameter.");
            return;
        }

        try {
            int itemId = Integer.parseInt(idParam.trim());
            HttpSession session = req.getSession(false);
            User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

            boolean deleted = itemService.deleteItem(itemId, currentUser);
            if (deleted) {
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, "{\"success\": true, \"message\": \"Item removed successfully.\"}");
            } else {
                JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Item not found or already deleted.");
            }
        } catch (SecurityException e) {
            JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            System.err.println("ItemServlet doDelete error: " + e.getMessage());
            JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred deleting the item.");
        }
    }
}
