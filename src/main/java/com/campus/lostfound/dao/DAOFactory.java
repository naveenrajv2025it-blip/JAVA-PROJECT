package com.campus.lostfound.dao;

import com.campus.lostfound.config.DBConnection;
import com.campus.lostfound.dao.jdbc.*;
import com.campus.lostfound.dao.mock.*;

/**
 * Data Access Object Factory.
 * In Phase 4: Provides production JDBC implementations backed by MySQL.
 * Automatically falls back to in-memory store if database is offline.
 */
public class DAOFactory {
    private static final boolean USE_JDBC = true;

    private static final UserDAO jdbcUserDAO = new JdbcUserDAO();
    private static final ItemDAO jdbcItemDAO = new JdbcItemDAO();
    private static final ClaimDAO jdbcClaimDAO = new JdbcClaimDAO();
    private static final CategoryDAO jdbcCategoryDAO = new JdbcCategoryDAO();
    private static final LocationDAO jdbcLocationDAO = new JdbcLocationDAO();

    private static final UserDAO mockUserDAO = new MockUserDAO();
    private static final ItemDAO mockItemDAO = new MockItemDAO();
    private static final ClaimDAO mockClaimDAO = new MockClaimDAO();
    private static final CategoryDAO mockCategoryDAO = new MockCategoryDAO();
    private static final LocationDAO mockLocationDAO = new MockLocationDAO();

    public static UserDAO getUserDAO() {
        return (USE_JDBC && DBConnection.testConnection()) ? jdbcUserDAO : mockUserDAO;
    }

    public static ItemDAO getItemDAO() {
        return (USE_JDBC && DBConnection.testConnection()) ? jdbcItemDAO : mockItemDAO;
    }

    public static ClaimDAO getClaimDAO() {
        return (USE_JDBC && DBConnection.testConnection()) ? jdbcClaimDAO : mockClaimDAO;
    }

    public static CategoryDAO getCategoryDAO() {
        return (USE_JDBC && DBConnection.testConnection()) ? jdbcCategoryDAO : mockCategoryDAO;
    }

    public static LocationDAO getLocationDAO() {
        return (USE_JDBC && DBConnection.testConnection()) ? jdbcLocationDAO : mockLocationDAO;
    }
}
