package com.campus.lostfound.service;

import com.campus.lostfound.dao.DAOFactory;
import com.campus.lostfound.dao.ItemDAO;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.User;
import com.campus.lostfound.util.ValidationUtil;

import java.sql.Date;
import java.util.List;

public class ItemService {
    private final ItemDAO itemDAO;

    public ItemService() {
        this.itemDAO = DAOFactory.getItemDAO();
    }

    public Item reportItem(Item item, User reporter) throws IllegalArgumentException {
        if (reporter == null) {
            throw new IllegalArgumentException("User must be authenticated to report an item.");
        }
        if (!ValidationUtil.isNotEmpty(item.getTitle())) {
            throw new IllegalArgumentException("Item title is required.");
        }
        if (!ValidationUtil.isValidItemType(item.getItemType())) {
            throw new IllegalArgumentException("Item type must be either LOST or FOUND.");
        }
        if (item.getCategoryId() <= 0) {
            item.setCategoryId(1); // Default category (General)
        }
        if (item.getLocationId() <= 0) {
            item.setLocationId(1); // Default campus hub
        }
        if (item.getDescription() == null || item.getDescription().trim().isEmpty()) {
            item.setDescription(item.getSpecificSpot() != null ? "Missed/found at: " + item.getSpecificSpot() : "Reported on campus");
        }

        if (item.getIncidentDate() == null) {
            item.setIncidentDate(new Date(System.currentTimeMillis()));
        }

        item.setUserId(reporter.getUserId());
        item.setStatus("OPEN");

        if (!ValidationUtil.isNotEmpty(item.getContactPreference())) {
            item.setContactPreference("CAMPUS_DESK");
        }

        return itemDAO.createItem(item);
    }

    public Item getItemById(int itemId) {
        return itemDAO.findById(itemId);
    }

    public List<Item> searchItems(String type, String status, Integer categoryId, Integer locationId, String query, String sortBy) {
        return itemDAO.findAll(type, status, categoryId, locationId, query, sortBy);
    }

    public boolean resolveItem(int itemId, User requester) throws SecurityException {
        Item item = itemDAO.findById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Item not found.");
        }

        boolean isReporter = requester != null && requester.getUserId() == item.getUserId();
        boolean isAdmin = requester != null && "ADMIN".equalsIgnoreCase(requester.getRole());

        if (!isReporter && !isAdmin) {
            throw new SecurityException("Unauthorized: Only the reporter or a campus admin can resolve this item.");
        }

        return itemDAO.updateStatus(itemId, "RESOLVED");
    }

    public boolean deleteItem(int itemId, User requester) throws SecurityException {
        Item item = itemDAO.findById(itemId);
        if (item == null) return false;

        boolean isReporter = requester != null && requester.getUserId() == item.getUserId();
        boolean isAdmin = requester != null && "ADMIN".equalsIgnoreCase(requester.getRole());

        if (!isReporter && !isAdmin) {
            throw new SecurityException("Unauthorized: You do not have permission to delete this listing.");
        }

        return itemDAO.deleteItem(itemId);
    }
}
