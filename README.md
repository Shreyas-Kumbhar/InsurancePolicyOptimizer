# Suraksha Shield - Insurance Policy Optimizer

Suraksha Shield is a Full-Stack Spring Boot application designed to help users find the optimal combination of insurance policies based on their specific constraints (e.g., maximum premium they can pay and minimum coverage they need). It leverages a Backtracking Algorithm to evaluate combinations of policies to minimize the overall premium cost while meeting coverage requirements.

## Table of Contents
- [Technologies Used](#technologies-used)
- [Project Structure and Java Code Explanation](#project-structure-and-java-code-explanation)
  - [1. Entities (`com.suraksha.shield.entity`)](#1-entities)
  - [2. Services (`com.suraksha.shield.service`)](#2-services)
  - [3. Controllers (`com.suraksha.shield.controller`)](#3-controllers)
  - [4. Security & Configuration (`com.suraksha.shield.config`)](#4-security--configuration)
  - [5. Repositories (`com.suraksha.shield.repository`)](#5-repositories)
  - [6. DTOs (Data Transfer Objects) (`com.suraksha.shield.dto`)](#6-dtos)
- [How the Optimization Engine Works](#how-the-optimization-engine-works)
- [How to Run](#how-to-run)

## Technologies Used
- **Java 17**
- **Spring Boot 3.2.5** (Web, Data JPA, Security, Validation)
- **MySQL Database**
- **JSON Web Tokens (JWT)** for authentication
- **Maven** for build and dependency management

---

## Project Structure and Java Code Explanation

The project follows a standard layered architecture typical for Spring Boot applications:

### 1. Entities (`com.suraksha.shield.entity`)
Entities map directly to database tables using JPA (Hibernate).
*   **`Policy.java`**: Represents an insurance policy. It contains details such as `name`, `type`, `premium` (cost), `coverage` (benefit amount), `riskLevel`, and `provider`. It also has a Many-to-One relationship with the `Admin` who created it.
*   **`User.java`**: Represents a standard user in the system. Users have attributes like `email`, `password`, and a list of `allocatedPolicies` (policies they have chosen).
*   **`Admin.java`**: Represents an administrator user. Admins can create and manage policies in the system.

### 2. Services (`com.suraksha.shield.service`)
Services contain the core business logic of the application.
*   **`PolicyService.java`**: The core component of this application. It provides CRUD operations (Create, Read, Update, Delete) for `Policy` entities. Crucially, it houses the `optimize()` method which implements the **Backtracking Algorithm** to find the optimal combination of policies.
*   **`CustomUserDetailsService.java`**: Implements Spring Security's `UserDetailsService`. It loads user-specific data from the database (checking both `User` and `Admin` repositories) to facilitate the authentication process.

### 3. Controllers (`com.suraksha.shield.controller`)
Controllers handle incoming HTTP requests and return HTTP responses, acting as the bridge between the frontend and backend.
*   **`PolicyController.java`**: Exposes endpoints for users to search for policies (`/api/policies/results`), view specific policy details (`/api/policies/{id}`), and allocate a policy to their profile (`/api/policies/{id}/allocate`).
*   **`AuthController.java`**: Manages user authentication and registration. It provides endpoints like `/api/auth/login` (which returns a JWT) and `/api/auth/register`.
*   **`AdminController.java`**: Secured endpoints intended only for Admins to add, edit, and delete policies.
*   **`UserController.java`**: Endpoints for standard users to fetch their profile details and view the policies they have been allocated.
*   **`PageController.java`**: Responsible for routing and serving static HTML views.

### 4. Security & Configuration (`com.suraksha.shield.config`)
Manages application security, JWT validation, and general setup.
*   **`SecurityConfig.java`**: Configures Spring Security. It disables CSRF, sets session management to stateless (since we use JWTs), and defines which endpoints are public (like login/register) and which require authentication or specific roles (like Admin endpoints).
*   **`JwtTokenProvider.java`**: Utility class used to generate, parse, and validate JSON Web Tokens.
*   **`JwtAuthenticationFilter.java`**: A filter that runs once per request. It intercepts HTTP requests, extracts the JWT from the `Authorization` header, validates it, and sets the authenticated user in the `SecurityContext`.
*   **`DatabaseSeeder.java`**: An initialization component that seeds the database with initial Admin accounts or default policies when the application starts.

### 5. Repositories (`com.suraksha.shield.repository`)
Interfaces extending Spring Data JPA's `JpaRepository`. They provide out-of-the-box methods for database operations (save, find, delete) without needing to write boilerplate SQL queries.
*   **`PolicyRepository.java`**: Handles data access for policies. It includes custom methods to filter policies by type, risk level, or name.
*   **`UserRepository.java`** & **`AdminRepository.java`**: Handle data access for users and admins respectively (e.g., finding a user by their email).

### 6. DTOs (Data Transfer Objects) (`com.suraksha.shield.dto`)
Simple Java classes used to transfer data between layers (usually between Controller and Service, or as API requests/responses) without exposing database entities directly. Examples include `LoginRequest`, `RegisterRequest`, and `PolicyOptimizationResult`.

---

## How the Optimization Engine Works

The standout feature of this application is the policy optimizer located in `PolicyService.java`. It solves a variation of the Knapsack Problem.

**The Goal**: Find a combination of policies where the total coverage is $\ge$ `coverageMin`, and the total premium is $\le$ `maxPremium`, such that the overall premium paid is strictly minimized.

**The Algorithm (Backtracking)**:
1.  **Filtering**: First, the system filters all available policies in the database based on the user's selected `type` and `riskLevel`.
2.  **Backtracking (`backtrack` method)**: The algorithm recursively explores all possible combinations of the filtered policies.
    *   For every policy in the list, the algorithm makes a choice: **Include** it in the current combination or **Exclude** it.
    *   It keeps a running total of `currentPremium` and `currentCoverage`.
    *   **Pruning**: If adding a policy exceeds the `maxPremium`, that path is immediately abandoned (pruned).
    *   **Evaluating**: Whenever a valid combination is found (Coverage $\ge$ Minimum Required && Premium $\le$ Maximum Allowed), it checks if this combination's premium is lower than the best premium found so far. If it is, this becomes the new `bestCombination`.
3.  **Result**: After exploring the possibilities, it returns a `PolicyOptimizationResult` containing the optimal list of policies, the total premium cost, and the total coverage achieved.

---

## How to Run
1.  Ensure you have **Java 17** and **MySQL** installed.
2.  Create a MySQL database (check `application.properties` for the exact database name, username, and password required).
3.  Navigate to the project root directory in your terminal.
4.  Run the application using the Maven wrapper:
    ```bash
    ./mvnw spring-boot:run
    ```
5.  The application will start, usually on `http://localhost:8080`.


Copyright © 2026 Shreyas Kumbhar.
All rights reserved.

This repository is shared for educational and portfolio purposes only.
Unauthorized copying, redistribution, or commercial use is prohibited without written permission.