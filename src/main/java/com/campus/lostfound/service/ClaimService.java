package com.campus.lostfound.service;

import com.campus.lostfound.dao.ClaimDAO;
import com.campus.lostfound.dao.DAOFactory;
import com.campus.lostfound.dao.ItemDAO;
import com.campus.lostfound.model.Claim;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.User;
import com.campus.lostfound.util.ValidationUtil;

import java.util.List;

public class ClaimService {
    private final ClaimDAO claimDAO;
    private final ItemDAO itemDAO;

    public ClaimService() {
        this.claimDAO = DAOFactory.getClaimDAO();
        this.itemDAO = DAOFactory.getItemDAO();
    }

    public Claim submitClaim(int itemId, String proofDescription, String proofAttachmentUrl, User claimant) throws IllegalArgumentException {
        if (claimant == null) {
            throw new IllegalArgumentException("Authentication required to submit an ownership claim.");
        }

        Item item = itemDAO.findById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("The target item does not exist.");
        }

        if (!"FOUND".equalsIgnoreCase(item.getItemType())) {
            throw new IllegalArgumentException("Claims can only be filed against items reported as FOUND.");
        }

        if ("RESOLVED".equalsIgnoreCase(item.getStatus())) {
            throw new IllegalArgumentException("This item has already been reunited and marked as RESOLVED.");
        }

        if (item.getUserId() == claimant.getUserId()) {
            throw new IllegalArgumentException("You cannot file an ownership claim against an item you reported yourself.");
        }

        if (!ValidationUtil.hasMinLength(proofDescription, 10)) {
            throw new IllegalArgumentException("Please provide descriptive proof of ownership (minimum 10 characters).");
        }

        // Check if user already has an active pending claim for this item
        List<Claim> existingClaims = claimDAO.findByClaimant(claimant.getUserId());
        for (Claim c : existingClaims) {
            if (c.getItemId() == itemId && "PENDING".equalsIgnoreCase(c.getClaimStatus())) {
                throw new IllegalArgumentException("You already have a pending verification claim under review for this item.");
            }
        }

        Claim claim = new Claim();
        claim.setItemId(itemId);
        claim.setClaimantId(claimant.getUserId());
        claim.setProofDescription(proofDescription.trim());
        claim.setProofAttachmentUrl(proofAttachmentUrl);

        return claimDAO.createClaim(claim);
    }

    public List<Claim> getAllClaims(String statusFilter) {
        return claimDAO.findAll(statusFilter);
    }

    public List<Claim> getClaimsByClaimant(int claimantId) {
        return claimDAO.findByClaimant(claimantId);
    }

    public boolean reviewClaim(int claimId, int itemId, String newStatus, String adminRemarks, User reviewer) throws SecurityException, IllegalArgumentException {
        if (reviewer == null || (!"ADMIN".equalsIgnoreCase(reviewer.getRole()) && !"STAFF".equalsIgnoreCase(reviewer.getRole()))) {
            throw new SecurityException("Unauthorized: Only Campus Security Desk administrators or staff can review claims.");
        }

        if (!ValidationUtil.isValidClaimStatus(newStatus)) {
            throw new IllegalArgumentException("Invalid claim decision status. Must be APPROVED or REJECTED.");
        }

        return claimDAO.reviewClaim(claimId, itemId, newStatus.toUpperCase(), adminRemarks, reviewer.getUserId());
    }
}
