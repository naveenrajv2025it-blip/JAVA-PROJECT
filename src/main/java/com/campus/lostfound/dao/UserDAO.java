package com.campus.lostfound.dao;

import com.campus.lostfound.model.User;
import java.util.List;

/**
 * User Data Access Interface.
 * Defines persistence contracts for university accounts.
 * Concrete JDBC implementation with PreparedStatement will be implemented in Phase 4.
 */
public interface UserDAO {
    User findByEmail(String email);
    User findById(int userId);
    User createUser(User user);
    List<User> findAll();
}
