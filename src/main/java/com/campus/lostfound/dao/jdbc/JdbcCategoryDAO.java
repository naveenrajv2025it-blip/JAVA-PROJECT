package com.campus.lostfound.dao.jdbc;

import com.campus.lostfound.config.DBConnection;
import com.campus.lostfound.dao.CategoryDAO;
import com.campus.lostfound.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcCategoryDAO implements CategoryDAO {

    @Override
    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT category_id, category_name, description, icon_name FROM categories ORDER BY category_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Category(
                    rs.getInt("category_id"),
                    rs.getString("category_name"),
                    rs.getString("description"),
                    rs.getString("icon_name")
                ));
            }
        } catch (SQLException e) {
            System.err.println("JdbcCategoryDAO getAllCategories error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Category findById(int categoryId) {
        String sql = "SELECT category_id, category_name, description, icon_name FROM categories WHERE category_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Category(
                        rs.getInt("category_id"),
                        rs.getString("category_name"),
                        rs.getString("description"),
                        rs.getString("icon_name")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("JdbcCategoryDAO findById error: " + e.getMessage());
        }
        return null;
    }
}
