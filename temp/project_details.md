# Insurance Policy Optimizer (Suraksha Shield)

## 1. Project Overview
The **Insurance Policy Optimizer** (also known as Suraksha Shield) is a web application designed to help users navigate the complexities of insurance. Rather than manually comparing individual policies, the application acts as an optimization engine. It cross-references thousands of policy permutations to construct a bespoke portfolio that meets the user's coverage needs while minimizing their annual premium.

## 2. Technology Stack
The project is built using a lightweight Node.js architecture inspired by the MERN stack:
* **Backend**: Node.js and Express.js
* **Database**: MongoDB (accessed via Mongoose)
* **Frontend**: HTML, CSS, Bootstrap, and EJS (Embedded JavaScript templates)
* **Authentication**: `bcryptjs` for secure password hashing and `express-session` for stateful user sessions.

## 3. Core Functionalities

### A. User Authentication & Authorization
* **Registration & Login**: Users can create an account and log in securely. Passwords are encrypted before being stored in the database.
* **Session Management**: Express sessions keep track of the user's login state (`currentUser`). Certain features, such as viewing detailed policy documents or running the optimization engine, are strictly protected and require the user to be authenticated.

### B. Dynamic Landing Page
* The landing page acts as the entry point, showcasing the application's value proposition.
* **Context-Aware UI**: When a user is logged out, the page displays a static "Projected Savings" dashboard. Once logged in, this section dynamically transforms into a direct Search Bar, allowing the user to search for specific policies by name instantly.

### C. Policy Configuration Interface
* Users input their financial constraints into a Configuration Vault.
* Parameters include:
  * **Minimum Coverage Required**: The total coverage amount the user needs.
  * **Maximum Premium Constraint**: The absolute maximum amount the user is willing to pay annually.
  * **Policy Type**: Specific filters like Life Insurance or Health Insurance.
  * **Risk Profile**: Categorization of policies based on risk levels (Low, Medium, High).

### D. The Optimization Engine (Backtracking Algorithm)
* This is the core algorithmic feature of Suraksha Shield. When a user runs the optimization engine, the system does not just filter individual policies; it calculates the optimal **combination** of policies.
* **Logic Breakdown**:
  1. The engine retrieves a pool of base policies matching the Type and Risk Level.
  2. It utilizes a recursive **Backtracking Algorithm** to explore different subsets (combinations) of these policies.
  3. **Pruning**: If adding a policy causes the cumulative premium to exceed the user's budget, that algorithmic branch is immediately pruned (abandoned).
  4. **Validation**: When a combination meets the *Minimum Coverage* requirement, it is marked as a valid portfolio.
  5. **Optimization**: The algorithm tracks all valid portfolios and ultimately selects the one with the lowest possible combined premium.
* **Result**: The user is presented with an "Optimal Portfolio" containing a mix of policies that, together, provide the exact coverage needed at the cheapest possible price.

### E. Policy Search and Details
* **Direct Search**: Users can bypass the optimization engine to search for policies via string matching (`$regex`).
* **Detailed View**: Users can click "View Details" on any policy card to see an in-depth breakdown of the specific policy, including its provider, risk metrics, and precise coverage clauses.

## 4. Coupling and Dependencies

### A. NPM Dependencies (Tight Coupling)
The backend logic is tightly coupled to these critical open-source modules:
* `express` (v5.2.1): The core routing and server framework. All API routes and middleware rely entirely on Express.
* `mongoose` (v9.4.1): The ODM (Object Document Mapper) for MongoDB. The data models (`Policy`, `User`) are tightly coupled to Mongoose schema syntax and querying logic.
* `ejs` (v5.0.2): The view engine. The backend must pass data directly to EJS templates to render the HTML. This creates a tight coupling between the backend response structure and the frontend views.
* `bcryptjs` (v3.0.3): Used exclusively in the User model logic for synchronous/asynchronous password hashing.
* `express-session` (v1.19.0): Manages authentication state tightly bound to the Express request/response cycle.

### B. Architectural Coupling
* **Database Coupling**: The application is tightly coupled to a locally hosted MongoDB instance (`mongodb://127.0.0.1:27017/policiesDB`). The logic expects specific document structures.
* **Loose Coupling (Frontend)**: The CSS uses standard Bootstrap classes with a custom `index.css` file. The frontend logic (HTML/CSS) is loosely coupled to the backend, meaning the UI styling can be easily migrated or updated without altering the Node.js backend, provided the EJS variables (`<%= %>`) remain intact.
