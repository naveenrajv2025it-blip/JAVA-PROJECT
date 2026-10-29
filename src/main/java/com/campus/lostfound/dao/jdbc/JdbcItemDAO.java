package com.campus.lostfound.dao.jdbc;

import com.campus.lostfound.config.DBConnection;
import com.campus.lostfound.dao.ItemDAO;
import com.campus.lostfound.model.Item;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of ItemDAO.
 * Safe SQL parameterized queries, dynamic multi-facet filtering, and relational joins.
 */
public class JdbcItemDAO implements ItemDAO {

    @Override
    public Item createItem(Item item) {
        String sql = "INSERT INTO items (user_id, item_type, title, category_id, location_id, specific_spot, " +
                     "incident_date, status, description, image_url, contact_phone, contact_email, contact_preference) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, item.getUserId());
            ps.setString(2, item.getItemType());
            ps.setString(3, item.getTitle());
            ps.setInt(4, item.getCategoryId());
            ps.setInt(5, item.getLocationId());
            ps.setString(6, item.getSpecificSpot());
            ps.setDate(7, item.getIncidentDate());
            ps.setString(8, item.getStatus() != null ? item.getStatus() : "OPEN");
            ps.setString(9, item.getDescription());
            ps.setString(10, item.getImageUrl());
            ps.setString(11, item.getContactPhone());
            ps.setString(12, item.getContactEmail());
            ps.setString(13, item.getContactPreference() != null ? item.getContactPreference() : "CAMPUS_DESK");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        item.setItemId(keys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("JdbcItemDAO createItem error: " + e.getMessage());
            throw new RuntimeException("Database error creating item: " + e.getMessage(), e);
        }
        return item;
    }

    @Override
    public Item findById(int itemId) {
        String sql = "SELECT i.item_id, i.user_id, i.item_type, i.title, i.category_id, i.location_id, " +
                     "i.specific_spot, i.incident_date, i.status, i.description, i.image_url, " +
                     "i.contact_phone, i.contact_email, i.contact_preference, " +
                     "i.created_at, i.updated_at, c.category_name, l.building_name, " +
                     "u.full_name as reporter_name, u.roll_number as reporter_roll, u.email as reporter_email, u.phone as reporter_phone, u.role as reporter_role " +
                     "FROM items i " +
                     "LEFT JOIN categories c ON i.category_id = c.category_id " +
                     "LEFT JOIN locations l ON i.location_id = l.location_id " +
                     "LEFT JOIN users u ON i.user_id = u.user_id " +
                     "WHERE i.item_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapItem(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("JdbcItemDAO findById error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Item> findAll(String type, String status, Integer categoryId, Integer locationId, String query, String sortBy) {
        List<Item> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT i.item_id, i.user_id, i.item_type, i.title, i.category_id, i.location_id, " +
            "i.specific_spot, i.incident_date, i.status, i.description, i.image_url, " +
            "i.contact_phone, i.contact_email, i.contact_preference, " +
            "i.created_at, i.updated_at, c.category_name, l.building_name, " +
            "u.full_name as reporter_name, u.roll_number as reporter_roll, u.email as reporter_email, u.phone as reporter_phone, u.role as reporter_role " +
            "FROM items i " +
            "LEFT JOIN categories c ON i.category_id = c.category_id " +
            "LEFT JOIN locations l ON i.location_id = l.location_id " +
            "LEFT JOIN users u ON i.user_id = u.user_id " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (type != null && !type.equalsIgnoreCase("ALL")) {
            sql.append("AND i.item_type = ? ");
            params.add(type.toUpperCase());
        }

        if (status != null && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND i.status = ? ");
            params.add(status.toUpperCase());
        }

        if (categoryId != null && categoryId > 0) {
            sql.append("AND i.category_id = ? ");
            params.add(categoryId);
        }

        if (locationId != null && locationId > 0) {
            sql.append("AND i.location_id = ? ");
            params.add(locationId);
        }

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(i.title) LIKE ? OR LOWER(i.description) LIKE ? OR LOWER(i.specific_spot) LIKE ?) ");
            String q = "%" + query.trim().toLowerCase() + "%";
            params.add(q);
            params.add(q);
            params.add(q);
        }

        if ("oldest".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY i.incident_date ASC, i.item_id ASC");
        } else {
            sql.append("ORDER BY i.created_at DESC, i.item_id DESC");
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapItem(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("JdbcItemDAO findAll error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean updateStatus(int itemId, String status) {
        String sql = "UPDATE items SET status = ? WHERE item_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("JdbcItemDAO updateStatus error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean deleteItem(int itemId) {
        String sql = "DELETE FROM items WHERE item_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("JdbcItemDAO deleteItem error: " + e.getMessage());
        }
        return false;
    }

    private Item mapItem(ResultSet rs) throws SQLException {
        Item item = new Item();
        item.setItemId(rs.getInt("item_id"));
        item.setUserId(rs.getInt("user_id"));
        item.setItemType(rs.getString("item_type"));
        item.setTitle(rs.getString("title"));
        item.setCategoryId(rs.getInt("category_id"));
        item.setLocationId(rs.getInt("location_id"));
        item.setSpecificSpot(rs.getString("specific_spot"));
        item.setIncidentDate(rs.getDate("incident_date"));
        item.setStatus(rs.getString("status"));
        item.setDescription(rs.getString("description"));
        item.setImageUrl(rs.getString("image_url"));
        item.setContactPhone(rs.getString("contact_phone"));
        item.setContactEmail(rs.getString("contact_email"));
        item.setContactPreference(rs.getString("contact_preference"));
        item.setCreatedAt(rs.getTimestamp("created_at"));
        item.setUpdatedAt(rs.getTimestamp("updated_at"));

        item.setCategoryName(rs.getString("category_name"));
        item.setBuildingName(rs.getString("building_name"));
        item.setReporterName(rs.getString("reporter_name"));
        item.setReporterRoll(rs.getString("reporter_roll"));
        item.setReporterEmail(rs.getString("reporter_email"));
        item.setReporterPhone(rs.getString("reporter_phone"));
        item.setReporterRole(rs.getString("reporter_role"));
        return item;
    }
}
