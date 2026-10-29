package com.campus.lostfound.dao.jdbc;

import com.campus.lostfound.config.DBConnection;
import com.campus.lostfound.dao.ClaimDAO;
import com.campus.lostfound.model.Claim;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of ClaimDAO.
 * Implements atomic multi-table transactions for claim submissions and administrative reviews.
 */
public class JdbcClaimDAO implements ClaimDAO {

    @Override
    public Claim createClaim(Claim claim) {
        String insertSql = "INSERT INTO claims (item_id, claimant_id, proof_description, proof_attachment_url, claim_status, admin_remarks) " +
                           "VALUES (?, ?, ?, ?, 'PENDING', ?)";
        String updateItemSql = "UPDATE items SET status = 'CLAIM_PENDING' WHERE item_id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Atomic Transaction

            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, claim.getItemId());
                ps.setInt(2, claim.getClaimantId());
                ps.setString(3, claim.getProofDescription());
                ps.setString(4, claim.getProofAttachmentUrl());
                ps.setString(5, "Awaiting review by Campus Security Desk");

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            claim.setClaimId(keys.getInt(1));
                            claim.setClaimStatus("PENDING");
                            claim.setAdminRemarks("Awaiting review by Campus Security Desk");
                        }
                    }
                }
            }

            try (PreparedStatement psItem = conn.prepareStatement(updateItemSql)) {
                psItem.setInt(1, claim.getItemId());
                psItem.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignored */ }
            }
            System.err.println("JdbcClaimDAO createClaim transaction rolled back: " + e.getMessage());
            throw new RuntimeException("Database error submitting claim: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) { /* ignored */ }
            }
        }

        return claim;
    }

    @Override
    public List<Claim> findAll(String statusFilter) {
        List<Claim> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT c.claim_id, c.item_id, c.claimant_id, c.proof_description, c.proof_attachment_url, " +
            "c.claim_status, c.admin_remarks, c.reviewed_by, c.created_at, c.updated_at, " +
            "i.title as item_title, u.full_name as claimant_name, u.roll_number as claimant_roll, " +
            "u.email as claimant_email, a.full_name as reviewer_name " +
            "FROM claims c " +
            "JOIN items i ON c.item_id = i.item_id " +
            "JOIN users u ON c.claimant_id = u.user_id " +
            "LEFT JOIN users a ON c.reviewed_by = a.user_id " +
            "WHERE 1=1 "
        );

        if (statusFilter != null && !statusFilter.equalsIgnoreCase("ALL")) {
            sql.append("AND c.claim_status = ? ");
        }
        sql.append("ORDER BY c.created_at DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            if (statusFilter != null && !statusFilter.equalsIgnoreCase("ALL")) {
                ps.setString(1, statusFilter.toUpperCase());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapClaim(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("JdbcClaimDAO findAll error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Claim> findByClaimant(int claimantId) {
        List<Claim> list = new ArrayList<>();
        String sql = "SELECT c.claim_id, c.item_id, c.claimant_id, c.proof_description, c.proof_attachment_url, " +
                     "c.claim_status, c.admin_remarks, c.reviewed_by, c.created_at, c.updated_at, " +
                     "i.title as item_title, u.full_name as claimant_name, u.roll_number as claimant_roll, " +
                     "u.email as claimant_email, a.full_name as reviewer_name " +
                     "FROM claims c " +
                     "JOIN items i ON c.item_id = i.item_id " +
                     "JOIN users u ON c.claimant_id = u.user_id " +
                     "LEFT JOIN users a ON c.reviewed_by = a.user_id " +
                     "WHERE c.claimant_id = ? " +
                     "ORDER BY c.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, claimantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapClaim(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("JdbcClaimDAO findByClaimant error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Claim findById(int claimId) {
        String sql = "SELECT c.claim_id, c.item_id, c.claimant_id, c.proof_description, c.proof_attachment_url, " +
                     "c.claim_status, c.admin_remarks, c.reviewed_by, c.created_at, c.updated_at, " +
                     "i.title as item_title, u.full_name as claimant_name, u.roll_number as claimant_roll, " +
                     "u.email as claimant_email, a.full_name as reviewer_name " +
                     "FROM claims c " +
                     "JOIN items i ON c.item_id = i.item_id " +
                     "JOIN users u ON c.claimant_id = u.user_id " +
                     "LEFT JOIN users a ON c.reviewed_by = a.user_id " +
                     "WHERE c.claim_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, claimId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapClaim(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("JdbcClaimDAO findById error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean reviewClaim(int claimId, int itemId, String newStatus, String adminRemarks, int reviewerUserId) {
        String updateClaimSql = "UPDATE claims SET claim_status = ?, admin_remarks = ?, reviewed_by = ? WHERE claim_id = ?";
        String updateItemResolvedSql = "UPDATE items SET status = 'RESOLVED' WHERE item_id = ?";
        String checkPendingClaimsSql = "SELECT COUNT(*) FROM claims WHERE item_id = ? AND claim_id != ? AND claim_status = 'PENDING'";
        String updateItemOpenSql = "UPDATE items SET status = 'OPEN' WHERE item_id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Atomic Transaction

            // 1. Update claim record
            try (PreparedStatement psClaim = conn.prepareStatement(updateClaimSql)) {
                psClaim.setString(1, newStatus.toUpperCase());
                psClaim.setString(2, adminRemarks);
                psClaim.setInt(3, reviewerUserId);
                psClaim.setInt(4, claimId);
                psClaim.executeUpdate();
            }

            // 2. Synchronize item status
            if ("APPROVED".equalsIgnoreCase(newStatus)) {
                try (PreparedStatement psItem = conn.prepareStatement(updateItemResolvedSql)) {
                    psItem.setInt(1, itemId);
                    psItem.executeUpdate();
                }
            } else if ("REJECTED".equalsIgnoreCase(newStatus)) {
                int otherPendingCount = 0;
                try (PreparedStatement psCheck = conn.prepareStatement(checkPendingClaimsSql)) {
                    psCheck.setInt(1, itemId);
                    psCheck.setInt(2, claimId);
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next()) otherPendingCount = rs.getInt(1);
                    }
                }

                if (otherPendingCount == 0) {
                    try (PreparedStatement psOpen = conn.prepareStatement(updateItemOpenSql)) {
                        psOpen.setInt(1, itemId);
                        psOpen.executeUpdate();
                    }
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignored */ }
            }
            System.err.println("JdbcClaimDAO reviewClaim transaction rolled back: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) { /* ignored */ }
            }
        }
    }

    private Claim mapClaim(ResultSet rs) throws SQLException {
        Claim c = new Claim();
        c.setClaimId(rs.getInt("claim_id"));
        c.setItemId(rs.getInt("item_id"));
        c.setClaimantId(rs.getInt("claimant_id"));
        c.setProofDescription(rs.getString("proof_description"));
        c.setProofAttachmentUrl(rs.getString("proof_attachment_url"));
        c.setClaimStatus(rs.getString("claim_status"));
        c.setAdminRemarks(rs.getString("admin_remarks"));
        int reviewer = rs.getInt("reviewed_by");
        c.setReviewedBy(rs.wasNull() ? null : reviewer);
        c.setCreatedAt(rs.getTimestamp("created_at"));
        c.setUpdatedAt(rs.getTimestamp("updated_at"));

        c.setItemTitle(rs.getString("item_title"));
        c.setClaimantName(rs.getString("claimant_name"));
        c.setClaimantRoll(rs.getString("claimant_roll"));
        c.setClaimantEmail(rs.getString("claimant_email"));
        c.setReviewerName(rs.getString("reviewer_name"));
        return c;
    }
}
