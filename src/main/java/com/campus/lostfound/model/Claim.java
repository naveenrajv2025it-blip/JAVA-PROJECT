package com.campus.lostfound.model;

import java.sql.Timestamp;

public class Claim {
    private int claimId;
    private int itemId;
    private int claimantId;
    private String proofDescription;
    private String proofAttachmentUrl;
    private String claimStatus; // PENDING, APPROVED, REJECTED
    private String adminRemarks;
    private Integer reviewedBy;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Joined fields
    private String itemTitle;
    private String claimantName;
    private String claimantRoll;
    private String claimantEmail;
    private String reviewerName;

    public Claim() {}

    public int getClaimId() { return claimId; }
    public void setClaimId(int claimId) { this.claimId = claimId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getClaimantId() { return claimantId; }
    public void setClaimantId(int claimantId) { this.claimantId = claimantId; }

    public String getProofDescription() { return proofDescription; }
    public void setProofDescription(String proofDescription) { this.proofDescription = proofDescription; }

    public String getProofAttachmentUrl() { return proofAttachmentUrl; }
    public void setProofAttachmentUrl(String proofAttachmentUrl) { this.proofAttachmentUrl = proofAttachmentUrl; }

    public String getClaimStatus() { return claimStatus; }
    public void setClaimStatus(String claimStatus) { this.claimStatus = claimStatus; }

    public String getAdminRemarks() { return adminRemarks; }
    public void setAdminRemarks(String adminRemarks) { this.adminRemarks = adminRemarks; }

    public Integer getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Integer reviewedBy) { this.reviewedBy = reviewedBy; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getItemTitle() { return itemTitle; }
    public void setItemTitle(String itemTitle) { this.itemTitle = itemTitle; }

    public String getClaimantName() { return claimantName; }
    public void setClaimantName(String claimantName) { this.claimantName = claimantName; }

    public String getClaimantRoll() { return claimantRoll; }
    public void setClaimantRoll(String claimantRoll) { this.claimantRoll = claimantRoll; }

    public String getClaimantEmail() { return claimantEmail; }
    public void setClaimantEmail(String claimantEmail) { this.claimantEmail = claimantEmail; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }
}
