package com.campus.lostfound.dao;

import com.campus.lostfound.model.Claim;
import java.util.List;

/**
 * Claim Data Access Interface.
 * Defines persistence contracts for ownership verification claims.
 * Concrete JDBC implementation will be implemented in Phase 4.
 */
public interface ClaimDAO {
    Claim createClaim(Claim claim);
    List<Claim> findAll(String statusFilter);
    List<Claim> findByClaimant(int claimantId);
    Claim findById(int claimId);
    boolean reviewClaim(int claimId, int itemId, String newStatus, String adminRemarks, int reviewerUserId);
}
