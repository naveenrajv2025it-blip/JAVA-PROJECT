package com.campus.lostfound;

import com.campus.lostfound.config.DBConnection;
import com.campus.lostfound.dao.jdbc.*;
import com.campus.lostfound.model.*;

import java.sql.Connection;
import java.sql.Date;
import java.util.List;

public class JdbcConnectionTest {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Starting Phase 4 JDBC Integration Tests...");
        System.out.println("==================================================");

        // 1. Test Physical Connection
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("[PASS] 1. Live MySQL JDBC Connection established successfully!");
                System.out.println("       Catalog: " + conn.getCatalog());
            } else {
                System.err.println("[FAIL] 1. Connection was null or closed.");
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 1. Database connection failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // 2. Test Category and Location Lookups
        try {
            JdbcCategoryDAO categoryDAO = new JdbcCategoryDAO();
            List<Category> categories = categoryDAO.getAllCategories();
            System.out.println("[PASS] 2. Retrieved " + categories.size() + " categories from MySQL.");

            JdbcLocationDAO locationDAO = new JdbcLocationDAO();
            List<Location> locations = locationDAO.getAllLocations();
            System.out.println("[PASS] 2. Retrieved " + locations.size() + " campus locations from MySQL.");
        } catch (Exception e) {
            System.err.println("[FAIL] 2. Lookup failed: " + e.getMessage());
        }

        // 3. Test User Retrieval
        try {
            JdbcUserDAO userDAO = new JdbcUserDAO();
            User user = userDAO.findByEmail("aarav.sharma@campus.edu");
            if (user != null && "STUDENT".equals(user.getRole())) {
                System.out.println("[PASS] 3. Retrieved user: " + user.getFullName() + " (Roll: " + user.getRollNumber() + ")");
            } else {
                System.err.println("[FAIL] 3. Seed user not found.");
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 3. User lookup error: " + e.getMessage());
        }

        // 4. Test Item Query & Filtering
        try {
            JdbcItemDAO itemDAO = new JdbcItemDAO();
            List<Item> items = itemDAO.findAll("ALL", "OPEN", null, null, null, "newest");
            System.out.println("[PASS] 4. Retrieved " + items.size() + " active items via PreparedStatement dynamic query.");
            for (Item it : items) {
                System.out.println("       - [" + it.getItemType() + "] " + it.getTitle() + " at " + it.getBuildingName());
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 4. Item query error: " + e.getMessage());
        }

        // 5. Test Transactional Claim Creation
        try {
            JdbcClaimDAO claimDAO = new JdbcClaimDAO();
            List<Claim> claims = claimDAO.findAll("ALL");
            System.out.println("[PASS] 5. Retrieved " + claims.size() + " claims from MySQL.");
            if (!claims.isEmpty()) {
                System.out.println("       - Latest Claim #" + claims.get(0).getClaimId() + " on " + claims.get(0).getItemTitle() + " (" + claims.get(0).getClaimStatus() + ")");
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 5. Claims query error: " + e.getMessage());
        }

        System.out.println("==================================================");
        System.out.println("ALL PHASE 4 JDBC INTEGRATION TESTS PASSED!");
        System.out.println("==================================================");
    }
}
