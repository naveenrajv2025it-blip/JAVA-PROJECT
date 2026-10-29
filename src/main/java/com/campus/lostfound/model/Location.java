package com.campus.lostfound.model;

public class Location {
    private int locationId;
    private String buildingName;
    private String campusZone;
    private String description;

    public Location() {}

    public Location(int locationId, String buildingName, String campusZone, String description) {
        this.locationId = locationId;
        this.buildingName = buildingName;
        this.campusZone = campusZone;
        this.description = description;
    }

    public int getLocationId() { return locationId; }
    public void setLocationId(int locationId) { this.locationId = locationId; }

    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    public String getCampusZone() { return campusZone; }
    public void setCampusZone(String campusZone) { this.campusZone = campusZone; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
