package com.campus.lostfound.dao;

import com.campus.lostfound.model.Item;
import java.util.List;

/**
 * Item Data Access Interface.
 * Defines persistence contracts for lost and found reports.
 * Concrete JDBC implementation will be implemented in Phase 4.
 */
public interface ItemDAO {
    Item createItem(Item item);
    Item findById(int itemId);
    List<Item> findAll(String type, String status, Integer categoryId, Integer locationId, String query, String sortBy);
    boolean updateStatus(int itemId, String status);
    boolean deleteItem(int itemId);
}
