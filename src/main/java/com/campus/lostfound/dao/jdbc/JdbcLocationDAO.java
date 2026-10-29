package com.campus.lostfound.dao.jdbc;

import com.campus.lostfound.config.DBConnection;
import com.campus.lostfound.dao.LocationDAO;
import com.campus.lostfound.model.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcLocationDAO implements LocationDAO {

    @Override
    public List<Location> getAllLocations() {
        List<Location> list = new ArrayList<>();
        String sql = "SELECT location_id, building_name, campus_zone, description FROM locations ORDER BY building_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Location(
                    rs.getInt("location_id"),
                    rs.getString("building_name"),
                    rs.getString("campus_zone"),
                    rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            System.err.println("JdbcLocationDAO getAllLocations error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Location findById(int locationId) {
        String sql = "SELECT location_id, building_name, campus_zone, description FROM locations WHERE location_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, locationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Location(
                        rs.getInt("location_id"),
                        rs.getString("building_name"),
                        rs.getString("campus_zone"),
                        rs.getString("description")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("JdbcLocationDAO findById error: " + e.getMessage());
        }
        return null;
    }
}
