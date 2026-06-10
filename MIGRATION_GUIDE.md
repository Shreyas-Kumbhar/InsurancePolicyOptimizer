# Migration Guide: MERN Stack to Java Spring Boot Full Stack

This guide details the complete migration of the **Suraksha Shield (Insurance Policy Optimizer)** project from a **MERN Stack** (MongoDB, Express, React/EJS, Node.js) to a **Java Spring Boot Full Stack** stack (Java 17, Spring Boot, MySQL, Spring Data JPA, Hibernate, and JWT Spring Security).

---

## 1. Directory Structure Comparison

### Old MERN Stack Directory Structure
```text
InsurancePolicyOptimizer/
├── middleware/
│   └── auth.js
├── models/
│   ├── admin.js
│   ├── policy.js
│   └── user.js
├── public/
│   └── css/
│       └── style.css
├── routes/
│   ├── admin.js
│   ├── auth.js
│   ├── policies.js
│   └── user.js
├── views/
│   ├── admin/
│   │   ├── dashboard.ejs
│   │   ├── editPolicy.ejs
│   │   ├── login.ejs
│   │   └── newPolicy.ejs
│   ├── partials/
│   │   ├── flash.ejs
│   │   ├── footer.ejs
│   │   └── header.ejs
│   ├── policies/
│   │   ├── config.ejs
│   │   ├── detail.ejs
│   │   └── results.ejs
│   ├── landing.ejs
│   ├── login.ejs
│   ├── profile.ejs
│   └── register.ejs
├── app.js
├── package.json
└── seedAdmin.js
```

### New Java Full Stack Directory Structure
```text
InsurancePolicyOptimizer/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── suraksha/
│       │           └── shield/
│       │               ├── config/
│       │               │   ├── DatabaseSeeder.java         <-- [NEW] Populates MySQL with admins/policies
│       │               │   ├── JwtAuthenticationFilter.java
│       │               │   ├── JwtTokenProvider.java
│       │               │   ├── SecurityConfig.java         <-- [UPDATED] Routes and CORS security
│       │               │   └── WebMvcConfig.java           <-- Forward static files to controllers
│       │               ├── controller/
│       │               │   ├── AdminController.java
│       │               │   ├── AuthController.java
│       │               │   ├── PolicyController.java
│       │               │   └── UserController.java
│       │               ├── dto/
│       │               │   ├── JwtResponse.java
│       │               │   ├── LoginRequest.java
│       │               │   ├── PolicyDto.java
│       │               │   ├── PolicyOptimizationResult.java
│       │               │   └── RegisterRequest.java
│       │               ├── entity/
│       │               │   ├── Admin.java
│       │               │   ├── Policy.java
│       │               │   └── User.java
│       │               ├── exception/
│       │               │   ├── GlobalExceptionHandler.java
│       │               │   └── ResourceNotFoundException.java
│       │               ├── repository/
│       │               │   ├── AdminRepository.java
│       │               │   ├── PolicyRepository.java
│       │               │   └── UserRepository.java
│       │               ├── service/
│       │               │   ├── CustomUserDetailsService.java
│       │               │   └── PolicyService.java
│       │               └── ShieldApplication.java
│       └── resources/
│           ├── static/                                     <-- [NEW] Frontend assets
│           │   ├── css/
│           │   │   └── style.css
│           │   ├── js/
│           │   │   └── main.js
│           │   ├── admin/
│           │   │   ├── dashboard.html
│           │   │   ├── editPolicy.html
│           │   │   ├── login.html
│           │   │   └── newPolicy.html
│           │   ├── policies/
│           │   │   ├── config.html
│           │   │   ├── detail.html
│           │   │   └── results.html
│           │   ├── index.html
│           │   ├── login.html
│           │   ├── profile.html
│           │   └── register.html
│           ├── application.properties
│           └── schema.sql                                  <-- [NEW] SQL definition
├── pom.xml
└── MIGRATION_GUIDE.md                                      <-- This document
```

---

## 2. Database Schema Translation (NoSQL to SQL)

MongoDB's unstructured documents are mapped to strict MySQL tables with correct constraints and links:

### 1. `admins` Table (MongoDB `Admin` collection)
```sql
CREATE TABLE admins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);
```

### 2. `users` Table (MongoDB `User` collection)
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);
```

### 3. `policies` Table (MongoDB `Policy` collection)
MongoDB's `createdBy` property referencing `Admin` is converted into a physical foreign key constraint `created_by_id`.
```sql
CREATE TABLE policies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    premium INT NOT NULL CHECK (premium >= 0),
    coverage INT NOT NULL CHECK (coverage >= 0),
    risk_level VARCHAR(50) NOT NULL,
    provider VARCHAR(255) NOT NULL,
    created_by_id BIGINT NULL,
    CONSTRAINT fk_policy_created_by FOREIGN KEY (created_by_id) REFERENCES admins(id) ON DELETE SET NULL
);
```

### 4. `user_policies` Join Table (MongoDB `User.allocatedPolicies` array field)
The array list of ObjectIds reference inside user schema is converted to a relational junction table with cascading keys.
```sql
CREATE TABLE user_policies (
    user_id BIGINT NOT NULL,
    policy_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, policy_id),
    CONSTRAINT fk_user_policies_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_policies_policy FOREIGN KEY (policy_id) REFERENCES policies(id) ON DELETE CASCADE
);
```

---

## 3. API Documentation

### Public Authentication Routes (`/api/auth/**`)
* **`POST /api/auth/register`**: Registers a new customer user.
  - Body: `{"firstName": "...", "lastName": "...", "email": "...", "password": "..."}`
* **`POST /api/auth/login`**: Authenticates a customer user. Returns JWT payload.
  - Body: `{"email": "...", "password": "..."}`
  - Response: `{"token": "...", "id": 1, "email": "...", "role": "USER", "firstName": "...", "lastName": "..."}`
* **`POST /api/auth/admin/login`**: Authenticates an administrative user.
  - Body: `{"email": "...", "password": "..."}`
  - Response: `{"token": "...", "id": 1, "email": "...", "role": "ADMIN"}`

### User Profile Endpoints (`/api/users/**` - Requires `ROLE_USER`)
* **`GET /api/users/profile`**: Returns the current user's profile and allocated policies.
  - Request Headers: `Authorization: Bearer <JWT_TOKEN>`

### Policies Endpoints (`/api/policies/**`)
* **`GET /api/policies/{id}`** *(Public)*: Returns policy attributes.
* **`GET /api/policies/results`** *(Requires `ROLE_USER`)*: Runs search and optimization.
  - Request Headers: `Authorization: Bearer <JWT_TOKEN>`
  - Request Parameters: `max_premium`, `coverage_min`, `coverage_max`, `type`, `riskLevel`, `name`
* **`POST /api/policies/{id}/allocate`** *(Requires `ROLE_USER`)*: Adds a policy to profile list.
  - Request Headers: `Authorization: Bearer <JWT_TOKEN>`

### Admin Endpoints (`/api/admin/**` - Requires `ROLE_ADMIN`)
* **`GET /api/admin/dashboard`**: Lists all active database policies.
  - Request Headers: `Authorization: Bearer <JWT_TOKEN>`
* **`POST /api/admin/policies`**: Saves a new policy.
  - Request Headers: `Authorization: Bearer <JWT_TOKEN>`
  - Body: `{"name": "...", "type": "...", "premium": 0, "coverage": 0, "riskLevel": "...", "provider": "..."}`
* **`PUT /api/admin/policies/{id}`**: Modifies a policy.
  - Request Headers: `Authorization: Bearer <JWT_TOKEN>`
* **`DELETE /api/admin/policies/{id}`**: Deletes a policy.
  - Request Headers: `Authorization: Bearer <JWT_TOKEN>`

---

## 4. Setup and Execution Steps

### Prerequisites
1. **Java Development Kit (JDK) 17** or higher.
2. **Maven 3.8+** (or use IntelliJ/Eclipse built-in compilation).
3. **MySQL Server 8.0+** running locally.

### Step 1: Configure MySQL Database Connection
Ensure MySQL is running. The application is configured to connect to database `policies_db` using username `root` and password `root`. 
You can modify this in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/policies_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### Step 2: Build the Application
Compile the code and retrieve dependencies specified in `pom.xml`:
```bash
mvn clean compile
```

### Step 3: Run the Application
Start the Spring Boot server (running on port `3647`):
```bash
mvn spring-boot:run
```

During startup, `DatabaseSeeder.java` will run automatically. It will:
- Check if any Admin exists. If not, it creates a default administrator: **`admin@gmail.com`** (Password: **`admin123`**).
- Check if any policies exist in the database. If empty, it automatically seeds 30+ sample Life, Health, and Car policies.

### Step 4: Verify in Browser
Open your browser and navigate to:
```text
http://localhost:3647/
```
From here you can search, register as a customer, log in, configure coverage budgets, and compute optimized insurance combinations.
To test admin controls, visit `http://localhost:3647/admin/login` and log in with `admin@gmail.com` / `admin123`..
