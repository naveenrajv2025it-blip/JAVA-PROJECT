package com.campus.lostfound.dao.mock;

import com.campus.lostfound.model.*;
import com.campus.lostfound.util.PasswordUtil;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Shared in-memory data store for Phase 3 mock repository implementations.
 */
public class MockDataStore {
    private static final MockDataStore instance = new MockDataStore();

    public final Map<Integer, User> users = new ConcurrentHashMap<>();
    public final Map<Integer, Item> items = new ConcurrentHashMap<>();
    public final Map<Integer, Claim> claims = new ConcurrentHashMap<>();
    public final Map<Integer, Category> categories = new ConcurrentHashMap<>();
    public final Map<Integer, Location> locations = new ConcurrentHashMap<>();

    public final AtomicInteger userIdSeq = new AtomicInteger(10);
    public final AtomicInteger itemIdSeq = new AtomicInteger(200);
    public final AtomicInteger claimIdSeq = new AtomicInteger(600);

    public static MockDataStore getInstance() {
        return instance;
    }

    private MockDataStore() {
        initSeedData();
    }

    private void initSeedData() {
        // Categories
        categories.put(1, new Category(1, "Electronics", "Laptops, phones, headphones, chargers", "laptop"));
        categories.put(2, new Category(2, "ID & Cards", "Student RFID ID cards, driver licenses", "id-card"));
        categories.put(3, new Category(3, "Keys & Wallets", "Dorm keys, car keys, cash wallets", "key"));
        categories.put(4, new Category(4, "Bags & Backpacks", "Backpacks, laptop sleeves, tote bags", "briefcase"));
        categories.put(5, new Category(5, "Books & Notes", "Course textbooks, binders, lab records", "book"));
        categories.put(6, new Category(6, "Accessories", "Wristwatches, jewelry, prescription glasses", "watch"));
        categories.put(7, new Category(7, "Clothing", "Jackets, hoodies, caps, umbrellas", "tag"));
        categories.put(8, new Category(8, "Other", "Water bottles, sports gear, musical instruments", "box"));

        // Locations
        locations.put(1, new Location(1, "Central Library", "CENTRAL", "Main reading hall, 2nd floor cubicles"));
        locations.put(2, new Location(2, "Computer Science Block", "NORTH", "Labs 1-4, seminar halls"));
        locations.put(3, new Location(3, "Main Cafeteria & Food Court", "CENTRAL", "Ground floor dining area"));
        locations.put(4, new Location(4, "Indoor Sports Arena", "SOUTH", "Gymnasium, badminton courts"));
        locations.put(5, new Location(5, "Auditorium Complex", "CENTRAL", "Main stage, balcony seating"));
        locations.put(6, new Location(6, "Engineering Workshop", "EAST", "Thermal lab, CNC room"));
        locations.put(7, new Location(7, "Student Activity Center (SAC)", "WEST", "Clubs room, common lounge"));

        // Mock store starts empty for fresh registration & reporting
    }
}
