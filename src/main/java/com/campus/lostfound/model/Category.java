package com.campus.lostfound.model;

public class Category {
    private int categoryId;
    private String categoryName;
    private String description;
    private String iconName;

    public Category() {}

    public Category(int categoryId, String categoryName, String description, String iconName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
        this.iconName = iconName;
    }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }
}
