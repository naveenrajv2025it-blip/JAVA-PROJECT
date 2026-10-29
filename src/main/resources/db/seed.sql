-- ============================================================================
-- Rajalakshmi Engineering College - Clean Database Setup
-- Database: campus_lost_found_db
-- ============================================================================

USE campus_lost_found_db;

-- Clear all user records, items, and claims for a clean production start
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE claims;
TRUNCATE TABLE items;
TRUNCATE TABLE users;
TRUNCATE TABLE locations;
TRUNCATE TABLE categories;
SET FOREIGN_KEY_CHECKS = 1;

-- Default Categories (for system internal mapping)
INSERT INTO categories (category_id, category_name, description, icon_name) VALUES
(1, 'General Belongings', 'Default category for all campus items', 'box');

-- Default Campus Location (for system internal mapping)
INSERT INTO locations (location_id, building_name, campus_zone, description) VALUES
(1, 'Rajalakshmi Engineering College Campus', 'CENTRAL', 'REC Thandalam Campus');

-- No dummy items, claims, or users are seeded. 
-- The database starts 100% clean for real students to register.
