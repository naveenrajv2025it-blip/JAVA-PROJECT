package com.campus.lostfound.dao;

import com.campus.lostfound.model.Category;
import java.util.List;

public interface CategoryDAO {
    List<Category> getAllCategories();
    Category findById(int categoryId);
}
