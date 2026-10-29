package com.campus.lostfound.dao.mock;

import com.campus.lostfound.dao.UserDAO;
import com.campus.lostfound.model.User;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MockUserDAO implements UserDAO {
    private final MockDataStore store = MockDataStore.getInstance();

    @Override
    public User findByEmail(String email) {
        if (email == null) return null;
        return store.users.values().stream()
                .filter(u -> email.equalsIgnoreCase(u.getEmail()))
                .findFirst().orElse(null);
    }

    @Override
    public User findById(int userId) {
        return store.users.get(userId);
    }

    @Override
    public User createUser(User user) {
        user.setUserId(store.userIdSeq.incrementAndGet());
        user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        store.users.put(user.getUserId(), user);
        return user;
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(store.users.values());
    }
}
