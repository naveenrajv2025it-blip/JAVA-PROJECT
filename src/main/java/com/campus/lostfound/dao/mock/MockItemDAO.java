package com.campus.lostfound.dao.mock;

import com.campus.lostfound.dao.ItemDAO;
import com.campus.lostfound.model.Category;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.Location;
import com.campus.lostfound.model.User;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

public class MockItemDAO implements ItemDAO {
    private final MockDataStore store = MockDataStore.getInstance();

    @Override
    public Item createItem(Item item) {
        item.setItemId(store.itemIdSeq.incrementAndGet());
        item.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        item.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        Category cat = store.categories.get(item.getCategoryId());
        if (cat != null) item.setCategoryName(cat.getCategoryName());

        Location loc = store.locations.get(item.getLocationId());
        if (loc != null) item.setBuildingName(loc.getBuildingName());

        User reporter = store.users.get(item.getUserId());
        if (reporter != null) {
            item.setReporterName(reporter.getFullName());
            item.setReporterEmail(reporter.getEmail());
            item.setReporterRoll(reporter.getRollNumber());
            item.setReporterRole(reporter.getRole());
        }

        store.items.put(item.getItemId(), item);
        return item;
    }

    @Override
    public Item findById(int itemId) {
        return store.items.get(itemId);
    }

    @Override
    public List<Item> findAll(String type, String status, Integer categoryId, Integer locationId, String query, String sortBy) {
        return store.items.values().stream()
                .filter(it -> type == null || "ALL".equalsIgnoreCase(type) || it.getItemType().equalsIgnoreCase(type))
                .filter(it -> status == null || "ALL".equalsIgnoreCase(status) || it.getStatus().equalsIgnoreCase(status))
                .filter(it -> categoryId == null || categoryId == 0 || it.getCategoryId() == categoryId)
                .filter(it -> locationId == null || locationId == 0 || it.getLocationId() == locationId)
                .filter(it -> {
                    if (query == null || query.trim().isEmpty()) return true;
                    String q = query.trim().toLowerCase();
                    return it.getTitle().toLowerCase().contains(q) ||
                           it.getDescription().toLowerCase().contains(q) ||
                           (it.getSpecificSpot() != null && it.getSpecificSpot().toLowerCase().contains(q));
                })
                .sorted((a, b) -> {
                    if ("oldest".equalsIgnoreCase(sortBy)) {
                        return a.getIncidentDate().compareTo(b.getIncidentDate());
                    }
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean updateStatus(int itemId, String status) {
        Item item = store.items.get(itemId);
        if (item != null) {
            item.setStatus(status);
            item.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteItem(int itemId) {
        return store.items.remove(itemId) != null;
    }
}
