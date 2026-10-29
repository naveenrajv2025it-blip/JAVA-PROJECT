package com.campus.lostfound.dao.mock;

import com.campus.lostfound.dao.ClaimDAO;
import com.campus.lostfound.model.Claim;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.User;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

public class MockClaimDAO implements ClaimDAO {
    private final MockDataStore store = MockDataStore.getInstance();

    @Override
    public Claim createClaim(Claim claim) {
        claim.setClaimId(store.claimIdSeq.incrementAndGet());
        claim.setClaimStatus("PENDING");
        claim.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        claim.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        Item item = store.items.get(claim.getItemId());
        if (item != null) {
            claim.setItemTitle(item.getTitle());
            item.setStatus("CLAIM_PENDING");
        }

        User claimant = store.users.get(claim.getClaimantId());
        if (claimant != null) {
            claim.setClaimantName(claimant.getFullName());
            claim.setClaimantRoll(claimant.getRollNumber());
            claim.setClaimantEmail(claimant.getEmail());
        }

        store.claims.put(claim.getClaimId(), claim);
        return claim;
    }

    @Override
    public List<Claim> findAll(String statusFilter) {
        return store.claims.values().stream()
                .filter(c -> statusFilter == null || "ALL".equalsIgnoreCase(statusFilter) || c.getClaimStatus().equalsIgnoreCase(statusFilter))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Claim> findByClaimant(int claimantId) {
        return store.claims.values().stream()
                .filter(c -> c.getClaimantId() == claimantId)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public Claim findById(int claimId) {
        return store.claims.get(claimId);
    }

    @Override
    public boolean reviewClaim(int claimId, int itemId, String newStatus, String adminRemarks, int reviewerUserId) {
        Claim claim = store.claims.get(claimId);
        if (claim == null) return false;

        claim.setClaimStatus(newStatus);
        claim.setAdminRemarks(adminRemarks);
        claim.setReviewedBy(reviewerUserId);
        claim.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        User reviewer = store.users.get(reviewerUserId);
        if (reviewer != null) {
            claim.setReviewerName(reviewer.getFullName());
        }

        Item item = store.items.get(itemId);
        if (item != null) {
            if ("APPROVED".equalsIgnoreCase(newStatus)) {
                item.setStatus("RESOLVED");
            } else if ("REJECTED".equalsIgnoreCase(newStatus)) {
                boolean hasOtherPending = store.claims.values().stream()
                        .anyMatch(c -> c.getItemId() == itemId && c.getClaimId() != claimId && "PENDING".equalsIgnoreCase(c.getClaimStatus()));
                if (!hasOtherPending) {
                    item.setStatus("OPEN");
                }
            }
        }

        return true;
    }
}
