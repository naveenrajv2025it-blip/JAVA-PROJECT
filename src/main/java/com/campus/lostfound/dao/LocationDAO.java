package com.campus.lostfound.dao;

import com.campus.lostfound.model.Location;
import java.util.List;

public interface LocationDAO {
    List<Location> getAllLocations();
    Location findById(int locationId);
}
