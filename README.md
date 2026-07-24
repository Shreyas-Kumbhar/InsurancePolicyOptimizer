<div align="center">

# 🛡️ Suraksha Shield
### Insurance Policy Optimizer

**A full-stack Spring Boot application that helps users discover the most cost-effective insurance coverage using a backtracking optimization algorithm.**

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?style=flat-square&logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)
![JWT](https://img.shields.io/badge/Auth-JWT-purple?style=flat-square&logo=jsonwebtokens)
![Maven](https://img.shields.io/badge/Build-Maven-red?style=flat-square&logo=apachemaven)
![License](https://img.shields.io/badge/License-Educational-lightgrey?style=flat-square)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Architecture](#-architecture)
- [How the Optimization Engine Works](#-how-the-optimization-engine-works)
- [API Reference](#-api-reference)
- [Database Schema](#-database-schema)
- [Getting Started](#-getting-started)
- [Default Credentials](#-default-credentials)
- [Screenshots](#-screenshots)
- [License](#-license)

---

## 🔍 Overview

Suraksha Shield is a production-ready insurance policy optimization platform built with Java and Spring Boot. Users define their financial constraints — maximum annual premium and minimum coverage required — and the engine computes the optimal combination of policies that minimizes cost while satisfying coverage requirements.

The core algorithm solves a variant of the **0/1 Knapsack Problem** using **recursive backtracking with pruning**, ensuring the most cost-efficient policy portfolio is returned.

---

## ✨ Features

### User
- Register and log in securely with JWT-based authentication
- Configure optimization parameters — coverage range, max premium, policy type, and risk level
- Dynamic risk calculation based on age, health, driving history, and lifestyle factors
- Browse and view detailed information on all available policies
- Allocate policies to a personal profile dashboard
- View all allocated policies from the user dashboard

### Admin
- Secure admin login (separate from user login)
- Full CRUD management of insurance policies
- View all policies from a dedicated admin dashboard
- Create and edit policies with provider, type, risk, premium, and coverage details

### System
- Auto-seeds admin account and 35 sample policies on first startup
- Stateless JWT authentication — no sessions
- Role-based access control (`ROLE_USER` / `ROLE_ADMIN`)
- BCrypt password hashing
- Input validation on all API endpoints
- Global exception handling with meaningful error responses

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Security | Spring Security 6 + JWT (JJWT 0.11.5) |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL 8.0 |
| Build Tool | Maven |
| Frontend | HTML5, Bootstrap 5.3, Vanilla JS |
| Server | Embedded Apache Tomcat |

---

## 📁 Project Structure

```
src/
└── main/
    ├── java/com/suraksha/shield/
    │   ├── config/
    │   │   ├── DatabaseSeeder.java          # Seeds admin + 35 sample policies on startup
    │   │   ├── JwtAuthenticationFilter.java # Validates JWT on every request
    │   │   ├── JwtTokenProvider.java        # JWT generation, parsing, validation
    │   │   ├── SecurityConfig.java          # Spring Security rules & filter chain
    │   │   └── WebMvcConfig.java            # Static resource & URL routing config
    │   ├── controller/
    │   │   ├── AdminController.java         # Admin CRUD API (/api/admin/**)
    │   │   ├── AuthController.java          # Login & register API (/api/auth/**)
    │   │   ├── PageController.java          # Serves static HTML pages
    │   │   ├── PolicyController.java        # Policy search & allocation API
    │   │   └── UserController.java          # User profile API (/api/users/**)
    │   ├── dto/
    │   │   ├── JwtResponse.java             # Auth response payload
    │   │   ├── LoginRequest.java            # Login request body
    │   │   ├── PolicyDto.java               # Policy create/update payload
    │   │   ├── PolicyOptimizationResult.java# Optimization engine response
    │   │   └── RegisterRequest.java         # Registration request body
    │   ├── entity/
    │   │   ├── Admin.java                   # Admin JPA entity
    │   │   ├── Policy.java                  # Policy JPA entity
    │   │   └── User.java                    # User JPA entity (with allocated policies)
    │   ├── exception/
    │   │   ├── GlobalExceptionHandler.java  # Centralized error handling
    │   │   └── ResourceNotFoundException.java
    │   ├── repository/
    │   │   ├── AdminRepository.java
    │   │   ├── PolicyRepository.java        # Custom queries: filter by type, risk, name
    │   │   └── UserRepository.java
    │   ├── service/
    │   │   ├── CustomUserDetailsService.java# Loads user/admin for Spring Security
    │   │   └── PolicyService.java           # Core business logic + optimization engine
    │   └── ShieldApplication.java           # Application entry point
    └── resources/
        ├── application.properties           # App configuration
        ├── schema.sql                        # Database schema reference
        └── static/
            ├── index.html                    # Landing page
            ├── login.html                    # User login
            ├── register.html                 # User registration
            ├── profile.html                  # User dashboard
            ├── admin/                        # Admin pages
            │   ├── login.html
            │   ├── dashboard.html
            │   ├── newPolicy.html
            │   └── editPolicy.html
            ├── policies/                     # Policy pages
            │   ├── config.html               # Optimization parameter form
            │   ├── results.html              # Search results
            │   └── detail.html               # Policy detail view
            ├── css/style.css
            └── js/
                ├── auth.js                   # Auth utilities
                └── main.js                   # Navbar, footer, fetch helpers
```

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────────┐
│                      Browser (Client)                    │
│          HTML + Bootstrap 5 + Vanilla JS + JWT           │
└─────────────────────┬───────────────────────────────────┘
                      │  HTTP Requests (JWT in header)
┌─────────────────────▼───────────────────────────────────┐
│                  Spring Boot Application                  │
│                                                           │
│  ┌─────────────────────────────────────────────────┐     │
│  │           Spring Security Filter Chain           │     │
│  │  JwtAuthenticationFilter → AuthorizationFilter  │     │
│  └──────────────────────┬──────────────────────────┘     │
│                         │                                 │
│  ┌──────────────────────▼──────────────────────────┐     │
│  │                  Controllers                     │     │
│  │  AuthController │ PolicyController │ AdminCtrl  │     │
│  └──────────────────────┬──────────────────────────┘     │
│                         │                                 │
│  ┌──────────────────────▼──────────────────────────┐     │
│  │                   Services                       │     │
│  │    PolicyService (Optimization Engine)           │     │
│  │    CustomUserDetailsService                      │     │
│  └──────────────────────┬──────────────────────────┘     │
│                         │                                 │
│  ┌──────────────────────▼──────────────────────────┐     │
│  │              Repositories (JPA)                  │     │
│  │  PolicyRepository │ UserRepository │ AdminRepo  │     │
│  └──────────────────────┬──────────────────────────┘     │
└─────────────────────────┼───────────────────────────────┘
                          │  JDBC
┌─────────────────────────▼───────────────────────────────┐
│                     MySQL Database                        │
│          admins │ users │ policies │ user_policies        │
└─────────────────────────────────────────────────────────┘
```

---

## ⚙️ How the Optimization Engine Works

The engine is implemented in `PolicyService.java` and solves a variant of the **0/1 Knapsack Problem** using **backtracking with pruning**.

### Goal
Find a combination of policies where:
- **Total coverage** ≥ `coverageMin`
- **Total premium** ≤ `maxPremium`
- **Total premium is minimized** across all valid combinations

### Algorithm Steps

```
1. FILTER
   └── Query policies by type and/or riskLevel from DB

2. BACKTRACK (recursive)
   └── For each policy at index i:
       ├── EXCLUDE: skip to index i+1
       └── INCLUDE: add to current combination
           ├── PRUNE: if currentPremium > maxPremium → stop this branch
           └── EVALUATE: if coverage met → update bestCombination if cheaper

3. RETURN
   └── PolicyOptimizationResult {
         policies: bestCombination,
         combinationMode: true,
         totalPremium: minimizedPremium,
         totalCoverage: achievedCoverage
       }
```

### Complexity
- **Time**: O(2ⁿ) worst case, significantly reduced by premium-based pruning
- **Space**: O(n) recursion depth

### Special Cases
| Input | Behaviour |
|---|---|
| Name search | Bypasses backtracking, returns name-matched results sorted by premium |
| `show_all=true` | Returns all policies sorted by premium, no optimization |
| No filters | Runs backtracking on the full policy catalog |

---

## 📡 API Reference

### Authentication

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register a new user |
| `POST` | `/api/auth/login` | Public | User login → returns JWT |
| `POST` | `/api/auth/admin/login` | Public | Admin login → returns JWT |

**Register Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "yourpassword"
}
```

**Login Request Body:**
```json
{
  "email": "john@example.com",
  "password": "yourpassword"
}
```

**Login Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "id": 1,
  "email": "john@example.com",
  "role": "USER",
  "firstName": "John",
  "lastName": "Doe"
}
```

---

### Policies

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/policies/results` | Public | Search & optimize policies |
| `GET` | `/api/policies/{id}` | Public | Get policy by ID |
| `POST` | `/api/policies/{id}/allocate` | `ROLE_USER` | Allocate policy to profile |

**Query Parameters for `/api/policies/results`:**

| Parameter | Type | Description |
|---|---|---|
| `type` | string | `life`, `health`, `car`, `home`, `travel` |
| `riskLevel` | string | `low`, `medium`, `high` |
| `max_premium` | integer | Maximum annual premium (₹) |
| `coverage_min` | integer | Minimum coverage required (₹) |
| `coverage_max` | integer | Maximum coverage cap (₹) |
| `name` | string | Search by policy name |
| `show_all` | boolean | Return all policies sorted by premium |

---

### User

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/users/profile` | `ROLE_USER` | Get profile + allocated policies |

---

### Admin

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/admin/dashboard` | `ROLE_ADMIN` | List all policies |
| `POST` | `/api/admin/policies` | `ROLE_ADMIN` | Create a new policy |
| `PUT` | `/api/admin/policies/{id}` | `ROLE_ADMIN` | Update a policy |
| `DELETE` | `/api/admin/policies/{id}` | `ROLE_ADMIN` | Delete a policy |

**Policy Request Body (Create/Update):**
```json
{
  "name": "LIC Jeevan Suraksha",
  "type": "life",
  "premium": 5000,
  "coverage": 300000,
  "riskLevel": "low",
  "provider": "LIC"
}
```

> All protected endpoints require the header:
> `Authorization: Bearer <your_jwt_token>`

---

## 🗄 Database Schema

```sql
admins
  id          BIGINT PK AUTO_INCREMENT
  email       VARCHAR(255) UNIQUE NOT NULL
  password    VARCHAR(255) NOT NULL          -- BCrypt hashed

users
  id          BIGINT PK AUTO_INCREMENT
  first_name  VARCHAR(255) NOT NULL
  last_name   VARCHAR(255) NOT NULL
  email       VARCHAR(255) UNIQUE NOT NULL
  password    VARCHAR(255) NOT NULL          -- BCrypt hashed

policies
  id            BIGINT PK AUTO_INCREMENT
  name          VARCHAR(255) NOT NULL
  type          VARCHAR(255) NOT NULL        -- life | health | car | home | travel
  premium       INT NOT NULL
  coverage      INT NOT NULL
  risk_level    VARCHAR(50) NOT NULL         -- low | medium | high
  provider      VARCHAR(255) NOT NULL
  created_by_id BIGINT FK → admins(id)

user_policies                               -- Many-to-Many join table
  user_id     BIGINT FK → users(id)
  policy_id   BIGINT FK → policies(id)
  PRIMARY KEY (user_id, policy_id)
```

> Tables are auto-created by Hibernate on first startup (`ddl-auto=update`).

---

## 🚀 Getting Started

### Prerequisites

- **Java 17+** — [Download](https://adoptium.net/)
- **MySQL 8.0+** — [Download](https://dev.mysql.com/downloads/)
- **Maven 3.8+** — [Download](https://maven.apache.org/download.cgi)

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/InsurancePolicyOptimizer.git
cd InsurancePolicyOptimizer
```

### 2. Configure the Database

Make sure MySQL is running, then update `src/main/resources/application.properties` if your credentials differ:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/policies_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=root
```

The database `policies_db` is created automatically on first run.

### 3. Build & Run

```bash
mvn spring-boot:run
```

Or build a JAR and run it:

```bash
mvn clean package
java -jar target/shield-1.0.0.jar
```

### 4. Open in Browser

```
http://localhost:3647
```

The app seeds an admin account and 35 sample policies automatically on first startup.

---

## 🔑 Default Credentials

| Role | Email | Password |
|---|---|---|
| Admin | `admin@gmail.com` | `admin123` |
| User | *(register via `/register`)* | *(your choice)* |

> Change the admin password after first login in a production environment.

---

## 🗺 Page Routes

| URL | Description |
|---|---|
| `/` | Landing page |
| `/login` | User login |
| `/register` | User registration |
| `/profile` | User dashboard (protected) |
| `/policies` | Policy configuration & optimization form |
| `/policies/results` | Optimization results |
| `/policies/{id}` | Policy detail view |
| `/admin/login` | Admin login |
| `/admin/dashboard` | Admin policy management |
| `/admin/policies/new` | Create new policy |
| `/admin/policies/{id}/edit` | Edit existing policy |

---

## 📄 License

```
Copyright © 2026 Shreyas Kumbhar. All rights reserved.

This repository is shared for educational and portfolio purposes only.
Unauthorized copying, redistribution, or commercial use is strictly
prohibited without prior written permission from the author.
```

---

<div align="center">
  Built with ❤️ by <strong>Shreyas Kumbhar</strong>
</div>
