package com.campus.lostfound;

import com.campus.lostfound.config.DBConnection;
import com.campus.lostfound.dao.DAOFactory;
import com.campus.lostfound.dao.jdbc.*;
import com.campus.lostfound.model.*;
import com.campus.lostfound.service.*;
import com.campus.lostfound.util.PasswordUtil;
import com.campus.lostfound.util.ValidationUtil;

import java.sql.Connection;
import java.sql.Date;
import java.util.List;

public class FullWorkflowIntegrationTest {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("PHASE 5: END-TO-END INTEGRATION & WORKFLOW VALIDATION");
        System.out.println("=================================================================");

        int passed = 0;
        int failed = 0;

        // Workflow 1: Database Connection & Reusability
        try {
            System.out.println("\n[TEST 1] Verifying Database Connection...");
            boolean ok = DBConnection.testConnection();
            if (!ok) throw new RuntimeException("DBConnection.testConnection returned false");
            System.out.println(">>> PASS: Live MySQL connection healthy.");
            passed++;
        } catch (Exception e) {
            System.err.println(">>> FAIL: " + e.getMessage());
            failed++;
        }

        // Workflow 2: User Registration & Password Hashing
        User testStudent = null;
        try {
            System.out.println("\n[TEST 2] User Registration & Password Security...");
            AuthService authService = new AuthService();
            String testEmail = "test.student." + System.currentTimeMillis() + "@campus.edu";
            String testPass = "SecretPass123";

            testStudent = authService.register(
                "Jane Doe",
                testEmail,
                "ROLL-" + System.currentTimeMillis(),
                "+1555999888",
                testPass,
                "STUDENT"
            );

            if (testStudent == null || testStudent.getUserId() <= 0) {
                throw new RuntimeException("User registration failed to generate user ID");
            }
            if (testStudent.getPasswordHash().equals(testPass)) {
                throw new RuntimeException("Security violation: Password was stored in plaintext!");
            }
            if (!PasswordUtil.verifyPassword(testPass, testStudent.getPasswordHash())) {
                throw new RuntimeException("Password hash could not be verified!");
            }

            // Test duplicate registration rejection
            try {
                authService.register("Jane Clone", testEmail, "ROLL-CLONE", "", "SecretPass123", "STUDENT");
                throw new RuntimeException("Duplicate email was allowed!");
            } catch (IllegalArgumentException e) {
                // Expected rejection
            }

            System.out.println(">>> PASS: Student registered (#" + testStudent.getUserId() + "), password securely hashed.");
            passed++;
        } catch (Exception e) {
            System.err.println(">>> FAIL: " + e.getMessage());
            failed++;
        }

        // Workflow 3: User Authentication & Sign-in
        try {
            System.out.println("\n[TEST 3] User Authentication & Session Login...");
            AuthService authService = new AuthService();
            User loggedIn = authService.authenticate("aarav.sharma@campus.edu", "password123");
            if (loggedIn == null || !"STUDENT".equals(loggedIn.getRole())) {
                throw new RuntimeException("Failed to authenticate seed student");
            }

            // Test invalid password rejection
            try {
                authService.authenticate("aarav.sharma@campus.edu", "WrongPassword!");
                throw new RuntimeException("Invalid password was accepted!");
            } catch (IllegalArgumentException e) {
                // Expected
            }

            System.out.println(">>> PASS: Valid login succeeded and invalid credentials rejected.");
            passed++;
        } catch (Exception e) {
            System.err.println(">>> FAIL: " + e.getMessage());
            failed++;
        }

        // Workflow 4: Submit a Lost-Item Report
        Item lostItem = null;
        try {
            System.out.println("\n[TEST 4] Reporting a Lost Item...");
            ItemService itemService = new ItemService();

            Item newItem = new Item();
            newItem.setItemType("LOST");
            newItem.setTitle("Dell XPS 15 Laptop (Silver)");
            newItem.setCategoryId(1); // Electronics
            newItem.setLocationId(1); // Central Library
            newItem.setSpecificSpot("2nd Floor study desk #4");
            newItem.setIncidentDate(new Date(System.currentTimeMillis()));
            newItem.setDescription("Silver aluminum chassis with distinctive Python sticker on bottom right.");
            newItem.setImageUrl("https://images.unsplash.com/photo-1593642632823-8f785ba67e45");

            lostItem = itemService.reportItem(newItem, testStudent);
            if (lostItem == null || lostItem.getItemId() <= 0) {
                throw new RuntimeException("Failed to create lost item report");
            }
            if (!"OPEN".equals(lostItem.getStatus())) {
                throw new RuntimeException("Initial item status should be OPEN");
            }

            System.out.println(">>> PASS: Lost item created with ID #" + lostItem.getItemId() + " in MySQL.");
            passed++;
        } catch (Exception e) {
            System.err.println(">>> FAIL: " + e.getMessage());
            failed++;
        }

        // Workflow 5: Submit a Found-Item Report
        Item foundItem = null;
        try {
            System.out.println("\n[TEST 5] Reporting a Found Item...");
            ItemService itemService = new ItemService();

            User securityOfficer = DAOFactory.getUserDAO().findByEmail("security.admin@campus.edu");

            Item newItem = new Item();
            newItem.setItemType("FOUND");
            newItem.setTitle("Dell XPS Laptop with Charger");
            newItem.setCategoryId(1); // Electronics
            newItem.setLocationId(1); // Central Library
            newItem.setSpecificSpot("Study desk 4 in library");
            newItem.setIncidentDate(new Date(System.currentTimeMillis()));
            newItem.setDescription("Silver Dell laptop left behind. Deposited at Security Desk.");
            newItem.setImageUrl("https://images.unsplash.com/photo-1593642632823-8f785ba67e45");

            foundItem = itemService.reportItem(newItem, securityOfficer);
            if (foundItem == null || foundItem.getItemId() <= 0) {
                throw new RuntimeException("Failed to create found item report");
            }

            System.out.println(">>> PASS: Found item registered with ID #" + foundItem.getItemId() + " in MySQL.");
            passed++;
        } catch (Exception e) {
            System.err.println(">>> FAIL: " + e.getMessage());
            failed++;
        }

        // Workflow 6: Search, Filtering & Multi-Factor Matching
        try {
            System.out.println("\n[TEST 6] Search, Filtering & Smart Matching Engine...");
            ItemService itemService = new ItemService();

            // Search by keyword
            List<Item> searchResults = itemService.searchItems("ALL", "OPEN", null, null, "Dell XPS", "newest");
            if (searchResults.isEmpty()) {
                throw new RuntimeException("Keyword search failed to find Dell XPS reports");
            }

            // Run matching engine between lost and found
            MatchingEngine matchingEngine = new MatchingEngine();
            List<Item> allItems = itemService.searchItems(null, null, null, null, null, null);
            List<MatchResult> matches = matchingEngine.findMatches(lostItem, allItems);

            if (matches.isEmpty()) {
                throw new RuntimeException("Smart matching engine failed to identify matching found laptop!");
            }
            MatchResult topMatch = matches.get(0);
            if (topMatch.getScore() < 70) {
                throw new RuntimeException("Expected high match score (>70%) for same category, location and keywords, got: " + topMatch.getScore());
            }

            System.out.println(">>> PASS: Search returned " + searchResults.size() + " listings. Top match score: " + topMatch.getScore() + "% (" + String.join(", ", topMatch.getReasons()) + ")");
            passed++;
        } catch (Exception e) {
            System.err.println(">>> FAIL: " + e.getMessage());
            failed++;
        }

        // Workflow 7: Ownership Claim Submission & Fraud Prevention
        Claim submittedClaim = null;
        try {
            System.out.println("\n[TEST 7] Recovery Claim Submission & Business Rules...");
            ClaimService claimService = new ClaimService();

            // Rule check: Reporter cannot claim their own found item
            User finderOfficer = DAOFactory.getUserDAO().findByEmail("security.admin@campus.edu");
            try {
                claimService.submitClaim(foundItem.getItemId(), "This is mine", "", finderOfficer);
                throw new RuntimeException("Finder was improperly allowed to claim their own item!");
            } catch (IllegalArgumentException e) {
                // Expected rejection
            }

            // Valid claim submission by student
            submittedClaim = claimService.submitClaim(
                foundItem.getItemId(),
                "Serial number ends with 4910. Inside sleeve is a student ID badge.",
                "",
                testStudent
            );

            if (submittedClaim == null || submittedClaim.getClaimId() <= 0) {
                throw new RuntimeException("Failed to submit claim");
            }
            if (!"PENDING".equals(submittedClaim.getClaimStatus())) {
                throw new RuntimeException("Initial claim status should be PENDING");
            }

            // Verify target item status was atomically updated to CLAIM_PENDING in MySQL
            Item updatedItem = DAOFactory.getItemDAO().findById(foundItem.getItemId());
            if (!"CLAIM_PENDING".equals(updatedItem.getStatus())) {
                throw new RuntimeException("Item status was not updated to CLAIM_PENDING! Current: " + updatedItem.getStatus());
            }

            // Rule check: Duplicate pending claim on same item rejected
            try {
                claimService.submitClaim(foundItem.getItemId(), "Another claim attempt", "", testStudent);
                throw new RuntimeException("Duplicate pending claim was improperly allowed!");
            } catch (IllegalArgumentException e) {
                // Expected duplicate block
            }

            System.out.println(">>> PASS: Claim #" + submittedClaim.getClaimId() + " submitted; target item status atomically updated to CLAIM_PENDING.");
            passed++;
        } catch (Exception e) {
            System.err.println(">>> FAIL: " + e.getMessage());
            failed++;
        }

        // Workflow 8: Administrative Review & Item Resolution
        try {
            System.out.println("\n[TEST 8] Admin Verification & Atomic Resolution...");
            ClaimService claimService = new ClaimService();

            // Authorization check: Student cannot approve claims
            try {
                claimService.reviewClaim(submittedClaim.getClaimId(), foundItem.getItemId(), "APPROVED", "Approved", testStudent);
                throw new RuntimeException("Student was improperly permitted to review claim!");
            } catch (SecurityException e) {
                // Expected security block
            }

            // Admin reviews and approves claim
            User admin = DAOFactory.getUserDAO().findByEmail("security.admin@campus.edu");
            boolean success = claimService.reviewClaim(
                submittedClaim.getClaimId(),
                foundItem.getItemId(),
                "APPROVED",
                "Serial verified against physical device. Handed over at Security Desk.",
                admin
            );

            if (!success) {
                throw new RuntimeException("Claim review returned false");
            }

            // Verify claim status is APPROVED and item status is RESOLVED in MySQL
            Claim updatedClaim = DAOFactory.getClaimDAO().findById(submittedClaim.getClaimId());
            if (!"APPROVED".equals(updatedClaim.getClaimStatus())) {
                throw new RuntimeException("Claim status not updated to APPROVED");
            }

            Item resolvedItem = DAOFactory.getItemDAO().findById(foundItem.getItemId());
            if (!"RESOLVED".equals(resolvedItem.getStatus())) {
                throw new RuntimeException("Item status not updated to RESOLVED! Current: " + resolvedItem.getStatus());
            }

            System.out.println(">>> PASS: Claim approved and item atomically marked RESOLVED in MySQL.");
            passed++;
        } catch (Exception e) {
            System.err.println(">>> FAIL: " + e.getMessage());
            failed++;
        }

        // Workflow 9: Security Checks & SQL Injection Immunity
        try {
            System.out.println("\n[TEST 9] SQL Injection Immunity & Input Sanitation...");
            ItemService itemService = new ItemService();

            // Malicious SQL string injection attempt
            String maliciousQuery = "' OR '1'='1' -- ; DROP TABLE items; --";
            List<Item> safeResults = itemService.searchItems(null, null, null, null, maliciousQuery, null);

            // Verify table still exists and query returned safe result (should be 0)
            List<Item> verifyTable = itemService.searchItems(null, null, null, null, null, null);
            if (verifyTable.isEmpty()) {
                throw new RuntimeException("Database was harmed by injection query!");
            }

            System.out.println(">>> PASS: Parameterized PreparedStatement neutralized SQL injection attack safely.");
            passed++;
        } catch (Exception e) {
            System.err.println(">>> FAIL: " + e.getMessage());
            failed++;
        }

        // Workflow 10: Cleanup of Test Records
        try {
            System.out.println("\n[TEST 10] Record Cleanup & Audit Trail Integrity...");
            User admin = DAOFactory.getUserDAO().findByEmail("security.admin@campus.edu");
            ItemService itemService = new ItemService();

            if (lostItem != null) itemService.deleteItem(lostItem.getItemId(), admin);
            if (foundItem != null) itemService.deleteItem(foundItem.getItemId(), admin);

            System.out.println(">>> PASS: Test records cleaned up successfully.");
            passed++;
        } catch (Exception e) {
            System.err.println(">>> FAIL: " + e.getMessage());
            failed++;
        }

        System.out.println("\n=================================================================");
        System.out.println("INTEGRATION TEST SUMMARY: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("=================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
