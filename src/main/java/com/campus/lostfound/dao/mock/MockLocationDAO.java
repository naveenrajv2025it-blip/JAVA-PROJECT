package com.campus.lostfound.dao.mock;

import com.campus.lostfound.dao.LocationDAO;
import com.campus.lostfound.model.Location;

import java.util.ArrayList;
import java.util.List;

public class MockLocationDAO implements LocationDAO {
    private final MockDataStore store = MockDataStore.getInstance();

    @Override
    public List<Location> getAllLocations() {
        return new ArrayList<>(store.locations.values());
    }

    @Override
    public Location findById(int locationId) {
        return store.locations.get(locationId);
    }
}
