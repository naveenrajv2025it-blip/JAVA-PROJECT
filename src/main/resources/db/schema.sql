-- ============================================================================
-- CAMPUS LOST & FOUND SYSTEM — DATABASE SCHEMA & SEED SCRIPT
-- Database: MySQL 8.0+
-- Character Set: utf8mb4 (Full Unicode support including emojis)
-- ============================================================================

-- Step 1: Create and select Database
CREATE DATABASE IF NOT EXISTS campus_lost_found_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE campus_lost_found_db;

-- Step 2: Drop existing tables in reverse dependency order (if re-running)
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS claims;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS locations;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- TABLE 1: users
-- Purpose: Stores student, faculty, and security administration accounts.
-- ============================================================================
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    roll_number VARCHAR(30) NOT NULL UNIQUE,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('STUDENT', 'STAFF', 'ADMIN') NOT NULL DEFAULT 'STUDENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_user_role (role),
    INDEX idx_user_email (email)
) ENGINE=InnoDB COMMENT='Registered university users with role-based access';

-- ============================================================================
-- TABLE 2: categories
-- Purpose: Controlled vocabulary of campus item classifications.
-- ============================================================================
CREATE TABLE categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(60) NOT NULL UNIQUE,
    description VARCHAR(255),
    icon_name VARCHAR(50) DEFAULT 'box'
) ENGINE=InnoDB COMMENT='Controlled list of item categories';

-- ============================================================================
-- TABLE 3: locations
-- Purpose: University campus hubs, zones, and buildings.
-- ============================================================================
CREATE TABLE locations (
    location_id INT AUTO_INCREMENT PRIMARY KEY,
    building_name VARCHAR(100) NOT NULL,
    campus_zone ENUM('NORTH', 'SOUTH', 'CENTRAL', 'EAST', 'WEST', 'SPORTS') NOT NULL DEFAULT 'CENTRAL',
    description VARCHAR(255),
    
    INDEX idx_building_name (building_name)
) ENGINE=InnoDB COMMENT='Campus buildings and zones for location proximity matching';

-- ============================================================================
-- TABLE 4: items
-- Purpose: Central inventory for all reported LOST and FOUND belongings.
-- ============================================================================
CREATE TABLE items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    item_type ENUM('LOST', 'FOUND') NOT NULL,
    title VARCHAR(150) NOT NULL,
    category_id INT NOT NULL,
    location_id INT NOT NULL,
    specific_spot VARCHAR(150),
    incident_date DATE NOT NULL,
    status ENUM('OPEN', 'CLAIM_PENDING', 'RESOLVED', 'CLOSED') NOT NULL DEFAULT 'OPEN',
    description TEXT NOT NULL,
    image_url VARCHAR(500),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(120),
    contact_preference ENUM('CAMPUS_DESK', 'EMAIL', 'PHONE') NOT NULL DEFAULT 'CAMPUS_DESK',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Relational Integrity Constraints
    CONSTRAINT fk_items_user 
        FOREIGN KEY (user_id) REFERENCES users(user_id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
        
    CONSTRAINT fk_items_category 
        FOREIGN KEY (category_id) REFERENCES categories(category_id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
        
    CONSTRAINT fk_items_location 
        FOREIGN KEY (location_id) REFERENCES locations(location_id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,

    -- Performance Indexes for search & filtering
    INDEX idx_item_type_status (item_type, status),
    INDEX idx_item_category (category_id),
    INDEX idx_item_location (location_id),
    INDEX idx_item_incident_date (incident_date),
    INDEX idx_item_composite_search (item_type, status, category_id, location_id, incident_date),
    FULLTEXT INDEX idx_ft_item_search (title, description)
) ENGINE=InnoDB COMMENT='All lost and found listings';

-- ============================================================================
-- TABLE 5: claims
-- Purpose: Tracks ownership verification submissions and administrative review.
-- ============================================================================
CREATE TABLE claims (
    claim_id INT AUTO_INCREMENT PRIMARY KEY,
    item_id INT NOT NULL,
    claimant_id INT NOT NULL,
    proof_description TEXT NOT NULL,
    proof_attachment_url VARCHAR(500),
    claim_status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    admin_remarks TEXT,
    reviewed_by INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Relational Integrity Constraints
    CONSTRAINT fk_claims_item 
        FOREIGN KEY (item_id) REFERENCES items(item_id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
        
    CONSTRAINT fk_claims_claimant 
        FOREIGN KEY (claimant_id) REFERENCES users(user_id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
        
    CONSTRAINT fk_claims_reviewer 
        FOREIGN KEY (reviewed_by) REFERENCES users(user_id) 
        ON DELETE SET NULL ON UPDATE CASCADE,

    -- Indexes for user dashboard & admin review queue
    INDEX idx_claim_status (claim_status),
    INDEX idx_claimant_history (claimant_id, claim_status),
    INDEX idx_claim_item (item_id)
) ENGINE=InnoDB COMMENT='Ownership verification claims and decisions';


-- Default Categories (for system internal mapping)
INSERT INTO categories (category_id, category_name, description, icon_name) VALUES
(1, 'General Belongings', 'Default category for all campus items', 'box');

-- Default Campus Location (for system internal mapping)
INSERT INTO locations (location_id, building_name, campus_zone, description) VALUES
(1, 'Rajalakshmi Engineering College Campus', 'CENTRAL', 'REC Thandalam Campus');

-- Database is clean: no dummy users, items, or claims.

