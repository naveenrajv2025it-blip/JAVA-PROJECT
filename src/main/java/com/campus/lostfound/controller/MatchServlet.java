package com.campus.lostfound.controller;

import com.campus.lostfound.dao.DAOFactory;
import com.campus.lostfound.dao.ItemDAO;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.MatchResult;
import com.campus.lostfound.service.MatchingEngine;
import com.campus.lostfound.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/matches")
public class MatchServlet extends HttpServlet {
    private final ItemDAO itemDAO = DAOFactory.getItemDAO();
    private final MatchingEngine matchingEngine = new MatchingEngine();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String itemIdParam = req.getParameter("itemId");
        if (itemIdParam == null || itemIdParam.trim().isEmpty()) {
            JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing required itemId parameter.");
            return;
        }

        try {
            int itemId = Integer.parseInt(itemIdParam.trim());
            Item targetItem = itemDAO.findById(itemId);

            if (targetItem == null) {
                JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Target item not found with ID #" + itemId);
                return;
            }

            // Retrieve all items for matching comparison
            List<Item> allItems = itemDAO.findAll(null, null, null, null, null, null);
            List<MatchResult> matches = matchingEngine.findMatches(targetItem, allItems);

            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_OK, matches);
        } catch (NumberFormatException e) {
            JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid numeric itemId format.");
        } catch (Exception e) {
            JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Matching engine error: " + e.getMessage());
        }
    }
}
