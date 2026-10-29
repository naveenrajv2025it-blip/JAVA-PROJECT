package com.campus.lostfound.model;

import java.sql.Date;
import java.sql.Timestamp;

public class Item {
    private int itemId;
    private int userId;
    private String itemType; // LOST, FOUND
    private String title;
    private int categoryId;
    private int locationId;
    private String specificSpot;
    private Date incidentDate;
    private String status; // OPEN, CLAIM_PENDING, RESOLVED, CLOSED
    private String description;
    private String imageUrl;
    private String contactPreference; // CAMPUS_DESK, EMAIL, PHONE
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Direct Contact Info for Finder / Reporter
    private String contactPhone;
    private String contactEmail;

    // Joins & display helpers
    private String categoryName;
    private String buildingName;
    private String reporterName;
    private String reporterRoll;
    private String reporterEmail;
    private String reporterPhone;
    private String reporterRole;

    public Item() {}

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public int getLocationId() { return locationId; }
    public void setLocationId(int locationId) { this.locationId = locationId; }

    public String getSpecificSpot() { return specificSpot; }
    public void setSpecificSpot(String specificSpot) { this.specificSpot = specificSpot; }

    public Date getIncidentDate() { return incidentDate; }
    public void setIncidentDate(Date incidentDate) { this.incidentDate = incidentDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getContactPreference() { return contactPreference; }
    public void setContactPreference(String contactPreference) { this.contactPreference = contactPreference; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public String getReporterRoll() { return reporterRoll; }
    public void setReporterRoll(String reporterRoll) { this.reporterRoll = reporterRoll; }

    public String getReporterEmail() { return reporterEmail; }
    public void setReporterEmail(String reporterEmail) { this.reporterEmail = reporterEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getReporterPhone() { return reporterPhone; }
    public void setReporterPhone(String reporterPhone) { this.reporterPhone = reporterPhone; }

    public String getReporterRole() { return reporterRole; }
    public void setReporterRole(String reporterRole) { this.reporterRole = reporterRole; }
}
