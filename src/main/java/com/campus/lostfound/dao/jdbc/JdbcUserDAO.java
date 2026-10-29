package com.campus.lostfound.dao.jdbc;

import com.campus.lostfound.config.DBConnection;
import com.campus.lostfound.dao.UserDAO;
import com.campus.lostfound.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of UserDAO.
 * Parameterized queries with PreparedStatement and try-with-resources.
 */
public class JdbcUserDAO implements UserDAO {

    @Override
    public User findByEmail(String email) {
        if (email == null) return null;
        String sql = "SELECT user_id, full_name, email, roll_number, phone, password_hash, role, created_at " +
                     "FROM users WHERE LOWER(email) = LOWER(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("JdbcUserDAO findByEmail error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public User findById(int userId) {
        String sql = "SELECT user_id, full_name, email, roll_number, phone, password_hash, role, created_at " +
                     "FROM users WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("JdbcUserDAO findById error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public User createUser(User user) {
        String sql = "INSERT INTO users (full_name, email, roll_number, phone, password_hash, role) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getRollNumber());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getPasswordHash());
            ps.setString(6, user.getRole() != null ? user.getRole().toUpperCase() : "STUDENT");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        user.setUserId(keys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("JdbcUserDAO createUser error: " + e.getMessage());
            throw new RuntimeException("Database error creating user: " + e.getMessage(), e);
        }
        return user;
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, full_name, email, roll_number, phone, password_hash, role, created_at " +
                     "FROM users ORDER BY full_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("JdbcUserDAO findAll error: " + e.getMessage());
        }
        return list;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setRollNumber(rs.getString("roll_number"));
        u.setPhone(rs.getString("phone"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }
}
