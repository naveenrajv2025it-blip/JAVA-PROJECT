package com.campus.lostfound.dao.mock;

import com.campus.lostfound.dao.CategoryDAO;
import com.campus.lostfound.model.Category;

import java.util.ArrayList;
import java.util.List;

public class MockCategoryDAO implements CategoryDAO {
    private final MockDataStore store = MockDataStore.getInstance();

    @Override
    public List<Category> getAllCategories() {
        return new ArrayList<>(store.categories.values());
    }

    @Override
    public Category findById(int categoryId) {
        return store.categories.get(categoryId);
    }
}
