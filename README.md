# Campus Lost & Found System

A production-ready, enterprise-grade web application for university campuses to streamline the reporting, intelligent matching, and administrative verification of lost and found belongings.

Designed with a clean, high-contrast, white-based user interface and powered by a robust **Core Java, Java Servlets, JDBC, MySQL, and Apache Tomcat** backend without heavyweight frameworks.

---

## Table of Contents
1. [Project Overview & Problem Statement](#project-overview--problem-statement)
2. [Technology Stack & Architecture](#technology-stack--architecture)
3. [System Architecture Diagram](#system-architecture-diagram)
4. [Prerequisites](#prerequisites)
5. [MySQL Database Setup](#mysql-database-setup)
6. [Configuration & Secret Management](#configuration--secret-management)
7. [Build & Run Instructions](#build--run-instructions)
8. [Comprehensive Testing & Verification](#comprehensive-testing--verification)
9. [Main Features & Servlet Endpoints](#main-features--servlet-endpoints)
10. [Security Review & Hardening](#security-review--hardening)
11. [Known Limitations & Future Improvements](#known-limitations--future-improvements)

---

## Project Overview & Problem Statement

### Problem Statement
Traditional university lost and found systems rely on scattered bulletin boards, fragmented social media groups, or unorganized physical desks. This results in:
- Low item recovery rates (< 20%).
- Manual, time-consuming cross-referencing for campus security staff.
- Vulnerability to fraudulent ownership claims for high-value items (laptops, wallets, IDs).
- Lack of audit trails and real-time visibility for students and faculty.

### The Solution
The **Campus Lost & Found System** provides a centralized, secure digital portal featuring:
- **Instant Reporting:** Guided forms with live sticky preview cards for Lost and Found items.
- **Smart Multi-Factor Matching Engine:** Automatically calculates similarity scores between opposite report types using category, location, date proximity, and keyword tokenization.
- **Strict Verification Workflow:** Multi-step ownership claims requiring verifiable proof, reviewed exclusively by authorized campus security personnel.
- **Role-Based Workspaces:** Student dashboards for tracking personal reports/claims and an Admin Console for security desk officers.

---

## Technology Stack & Architecture

Strictly adheres to foundational enterprise Java standards without Spring Boot, Hibernate, JPA, or ORM frameworks:

- **Frontend:**
  - Semantic HTML5 with modern layout architecture.
  - Pure Vanilla CSS3 design system (accessible, responsive, crisp white-based design).
  - Vanilla JavaScript (ES6+) with modular client API integration and contextual XSS sanitization.
- **Backend:**
  - Core Java 17+.
  - Java Servlets 3.1+ (`javax.servlet`).
  - Apache Tomcat 9.0+ web container.
- **Data Access & Persistence:**
  - Pure Java Database Connectivity (JDBC) with parameterized `PreparedStatement`.
  - HikariCP connection pooling with transparent fallback to `DriverManager`.
  - MySQL 8.0+ (InnoDB engine with ACID transactions).
- **Build System:**
  - Apache Maven (`pom.xml`) generating a deployable `target/campus-lost-and-found.war`.

---

## System Architecture Diagram

```mermaid
graph TD
    Client["Browser (HTML5, Vanilla CSS, JS)"]
    
    subgraph TomcatServer["Apache Tomcat Web Container"]
        AuthFilter["AuthFilter (Encoding, Security Headers, Admin Auth)"]
        
        subgraph Servlets["Java Servlets (/api/*)"]
            AuthServlet["AuthServlet (/api/auth/*)"]
            ItemServlet["ItemServlet (/api/items/*)"]
            ClaimServlet["ClaimServlet (/api/claims/*)"]
            MatchServlet["MatchServlet (/api/matches)"]
            AdminServlet["AdminServlet (/api/admin/*)"]
            MetaServlet["MetadataServlet (/api/metadata/*)"]
        end
        
        subgraph Services["Business Logic Layer"]
            AuthService["AuthService (Password hashing & sessions)"]
            ItemService["ItemService (Ownership rules & reporting)"]
            ClaimService["ClaimService (Verification rules & role checks)"]
            MatchingEngine["MatchingEngine (Multi-factor scoring algorithm)"]
        end
        
        subgraph DAOs["Data Access Layer (JDBC Direct)"]
            JdbcUserDAO["JdbcUserDAO"]
            JdbcItemDAO["JdbcItemDAO"]
            JdbcClaimDAO["JdbcClaimDAO (ACID Transactions)"]
            JdbcCatDAO["JdbcCategoryDAO"]
            JdbcLocDAO["JdbcLocationDAO"]
            DBConnection["DBConnection (HikariCP / Env Overrides)"]
        end
    end
    
    subgraph Database["MySQL 8.0 Database (campus_lost_found_db)"]
        UsersTbl[("users")]
        CategoriesTbl[("categories")]
        LocationsTbl[("locations")]
        ItemsTbl[("items")]
        ClaimsTbl[("claims")]
    end
    
    Client -->|HTTP / JSON REST| AuthFilter
    AuthFilter --> Servlets
    Servlets --> Services
    Services --> DAOs
    DAOs --> DBConnection
    DBConnection -->|JDBC (Port 3306)| Database
```

---

## Prerequisites

Before running the project, ensure the following software is installed on your workstation:
1. **Java Development Kit (JDK):** Version 17 or higher (`javac -version`).
2. **Apache Maven:** Version 3.8 or higher (`mvn -version`).
3. **MySQL Server:** Version 8.0 or higher (`mysql --version`).
4. **Apache Tomcat:** Version 9.0.x (or Tomcat 10 with Jakarta migration).
5. **Web Browser:** Modern browser (Chrome, Edge, Firefox, Safari).

---

## MySQL Database Setup

1. **Start your local MySQL service:**
   ```bash
   # Windows (via Services or PowerShell)
   net start MySQL80
   
   # Linux / macOS
   sudo systemctl start mysql
   ```

2. **Execute Schema and Seed scripts:**
   The SQL scripts are located in `src/main/resources/db/`:
   ```bash
   # Connect to MySQL and run schema
   mysql -u root -p < src/main/resources/db/schema.sql
   
   # Populate realistic campus seed data
   mysql -u root -p < src/main/resources/db/seed.sql
   ```

3. **Pre-seeded Accounts for Testing:**
   | Role | Full Name | University Email | Roll Number | Password |
   | :--- | :--- | :--- | :--- | :--- |
   | **Student** | Aarav Sharma | `aarav.sharma@campus.edu` | `2023CS1042` | `password123` |
   | **Student** | Priya Patel | `priya.patel@campus.edu` | `2022EC058` | `password123` |
   | **Faculty / Staff** | Dr. Ramesh Nair | `r.nair@campus.edu` | `FAC-901` | `password123` |
   | **Security Officer** | Campus Security Officer | `security.admin@campus.edu` | `SEC-001` | `password123` |

---

## Configuration & Secret Management

The database connection parameters are fully externalized outside compiled Java source code.

### Option A: Configuration File (`db.properties`)
Copy the provided template file `src/main/resources/db.properties.example` to `src/main/resources/db.properties`:
```properties
# Database Connection Properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/campus_lost_found_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.user=root
db.password=YOUR_LOCAL_PASSWORD

# HikariCP Connection Pool Configuration
pool.maxSize=10
pool.minIdle=2
pool.connectionTimeout=30000
pool.idleTimeout=600000
```

### Option B: Environment Variables (Recommended for Production & CI/CD)
The application automatically checks for environment variables before consulting `db.properties`:
- `DB_URL`: JDBC connection URL.
- `DB_USER`: Database username.
- `DB_PASSWORD`: Database password.
- `DB_DRIVER`: Driver class name (default: `com.mysql.cj.jdbc.Driver`).

```bash
# Windows PowerShell
$env:DB_USER="root"
$env:DB_PASSWORD="YourPasswordHere"

# Linux / macOS
export DB_USER="root"
export DB_PASSWORD="YourPasswordHere"
```

---

## Build & Run Instructions

### 1. Compile and Package the Application
Use Maven to build the deployable `.war` archive:
```bash
mvn clean package -DskipTests
```
This generates the packaged artifact:
```
target/campus-lost-and-found.war
```

### 2. Deploy to Apache Tomcat
1. Copy `target/campus-lost-and-found.war` to your Apache Tomcat `webapps/` directory:
   ```bash
   cp target/campus-lost-and-found.war /path/to/tomcat/webapps/
   ```
2. Start the Tomcat server:
   ```bash
   # Windows
   C:\path\to\tomcat\bin\startup.bat
   
   # Linux / macOS
   /path/to/tomcat/bin/startup.sh
   ```
3. Open your browser and navigate to:
   ```
   http://localhost:8080/campus-lost-and-found/
   ```
   *(Note: The frontend client automatically detects the context path `/campus-lost-and-found/` or `/` dynamically).*

---

## Comprehensive Testing & Verification

The project includes an end-to-end integration test suite (`FullWorkflowIntegrationTest.java`) that verifies all 10 core functional workflows directly against live MySQL:

```bash
mvn test-compile "-Dexec.classpathScope=test" "-Dexec.mainClass=com.campus.lostfound.FullWorkflowIntegrationTest" exec:java
```

### Verified Test Results (10/10 Passed)
- **[TEST 1] Database Connectivity:** HikariCP connection pool successfully establishes live connection to `campus_lost_found_db`.
- **[TEST 2] User Registration & Password Security:** Validates required fields and confirms SHA-256 password hashing.
- **[TEST 3] User Authentication & Sessions:** Verifies correct credential validation and rejection of incorrect passwords.
- **[TEST 4] Reporting Lost Belonging:** Inserts lost item with valid foreign keys and status `OPEN`.
- **[TEST 5] Reporting Found Belonging:** Inserts found item with valid foreign keys and status `OPEN`.
- **[TEST 6] Multi-Factor Smart Matching Engine:** Validates that opposite reports with matching category, location, and date calculate similarity scores up to 100%.
- **[TEST 7] Recovery Claim Business Rules:** Submits ownership claim, enforces that owners cannot claim their own items, and atomically updates target item status to `CLAIM_PENDING`.
- **[TEST 8] Administrative Verification & Resolution:** Verifies admin authorization, approves claim, and marks item status as `RESOLVED`.
- **[TEST 9] SQL Injection Neutralization:** Attempts SQL injection payloads (`' OR '1'='1`) and verifies parameterization safely neutralizes them.
- **[TEST 10] Audit Trail & Data Cleanup:** Cleans up temporary test records while preserving relational integrity.

---

## Main Features & Servlet Endpoints

### Application Views
- **Home (`index.html`):** Hero section, live stats, latest listings, category grid, and process guide.
- **Catalog Browse (`browse.html`):** Real-time multi-facet filtering (type, status, category, location), debounced search, sorting, and active filter chips.
- **Report Item (`report.html`):** Dual-mode (Lost / Found) submission form with live synchronized preview card and immediate match popup.
- **Item Details (`item-details.html`):** Complete incident specifications, proof requirements, security guidelines, and dynamic smart match suggestions.
- **My Dashboard (`my-items.html`):** Personal report management, status updates (Mark Reunited), and claim progression stepper.
- **Admin Console (`admin.html`):** Administrative verification queue, proof inspection modal, claim approval/rejection with remarks, and inventory management.
- **Sign In (`login.html`):** University SSO interface with quick 1-click role switcher (Student vs. Admin).

### RESTful Servlet API Endpoints
| HTTP Method | URL Pattern | Description | Access Control |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/metadata/categories` | Controlled list of item categories | Public |
| `GET` | `/api/metadata/locations` | Controlled list of campus buildings/zones | Public |
| `GET` | `/api/auth` | Retrieves current session user info | Logged In |
| `POST` | `/api/auth/login` | Authenticates user credentials and starts session | Public |
| `POST` | `/api/auth/register` | Registers new university user account | Public |
| `POST` | `/api/auth/logout` | Invalidates current HTTP session | Logged In |
| `GET` | `/api/items` | Searches and filters catalog listings | Public |
| `GET` | `/api/items?id={id}` | Retrieves full item details by reference ID | Public |
| `POST` | `/api/items` | Submits a new lost or found report | Authenticated User |
| `PUT` | `/api/items` | Resolves item listing (Mark Reunited) | Reporter or Admin |
| `DELETE` | `/api/items?id={id}` | Deletes listing from catalog | Reporter or Admin |
| `GET` | `/api/claims` | Lists claims (filtered by status or claimant) | Authenticated User |
| `POST` | `/api/claims` | Submits ownership claim with proof description | Non-owner Claimant |
| `PUT` | `/api/claims` | Approves or rejects ownership claim | Admin / Staff Only |
| `GET` | `/api/matches?itemId={id}` | Calculates smart match candidates for an item | Public |
| `GET` | `/api/admin/metrics` | Returns campus inventory and recovery metrics | Admin / Staff Only |

---

## Security Review & Hardening

1. **SQL Injection Immunity:**
   - 100% of database interactions in `JdbcUserDAO`, `JdbcItemDAO`, `JdbcClaimDAO`, `JdbcCategoryDAO`, and `JdbcLocationDAO` utilize parameterized `PreparedStatement`. No string concatenation is used for dynamic queries.
2. **Password Protection:**
   - User passwords are encrypted using SHA-256 via `PasswordUtil.hashPassword()`.
   - The `passwordHash` field in `User.java` is declared `transient`, ensuring hashed credentials are never exposed or serialized in JSON responses.
3. **Cross-Site Scripting (XSS) Prevention:**
   - All user-supplied fields (`title`, `description`, `specificSpot`, `proofDescription`, `adminRemarks`, user names) are sanitized through `UI.escapeHTML(...)` before dynamic interpolation into HTML template literals.
4. **Access Control & Ownership Enforcement:**
   - Item resolution and deletion enforce that only the original reporting user or an administrative account can modify the record (`ItemService.java`).
   - Claim submission enforces that claimants cannot file claims against items they reported themselves (`ClaimService.java`).
   - Claim reviews are strictly restricted to users with `ADMIN` or `STAFF` roles; unauthorized attempts receive `403 Forbidden`.
   - Admin routes are guarded by `AuthFilter.java`.
5. **Database Transaction Consistency (ACID):**
   - Multi-table operations (such as claim submission updating item status to `CLAIM_PENDING`, or claim review updating item status to `RESOLVED` or reopening to `OPEN`) are executed in atomic transactions using `conn.setAutoCommit(false)`, `conn.commit()`, and `conn.rollback()`.
6. **Defense-in-Depth HTTP Headers:**
   - `AuthFilter` injects standard HTTP security headers on all responses:
     - `X-Content-Type-Options: nosniff`
     - `X-Frame-Options: SAMEORIGIN`
     - `X-XSS-Protection: 1; mode=block`
     - `Referrer-Policy: strict-origin-when-cross-origin`
7. **Session Management:**
   - `web.xml` configures secure session cookies with `<http-only>true</http-only>` and 60-minute session expiry.
8. **Information Disclosure Prevention:**
   - Internal server errors (500) log diagnostic stack traces securely to server logs (`System.err`) while returning clean, sanitized JSON error messages to clients without leaking database paths or SQL syntax.

---

## Known Limitations & Future Improvements

1. **Password Hashing Evolution:**
   - *Current:* SHA-256 hex digest.
   - *Future Work:* Upgrade to salted PBKDF2 or BCrypt with a configurable work factor for enhanced brute-force resistance.
2. **Cloud Binary Storage:**
   - *Current:* External image URLs (e.g., Unsplash campus imagery or hosted URLs).
   - *Future Work:* Implement `Servlet 3.0` multipart file upload directly to cloud object storage (e.g., AWS S3 or MinIO) with thumbnail generation and file type validation.
3. **Automated Notification Triggers:**
   - *Current:* Instant on-screen toast notifications and status badges.
   - *Future Work:* Integrate JavaMail API or campus SMTP server to send automatic email alerts when a matching item is registered or when an ownership claim is approved.
4. **Two-Factor Authentication (2FA):**
   - *Current:* Single sign-on role authentication.
   - *Future Work:* Add TOTP/authenticator app 2FA for the administrative security desk review actions.
