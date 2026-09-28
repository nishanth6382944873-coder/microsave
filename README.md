# MicroSave – Self-Help Group Savings Tracker

A complete, clean, and robust Full-Stack Web Application built for college semester examinations. It tracks women's Self-Help Group (SHG) savings contributions, internal micro-credit loans, repayments, and pooled group capital according to real-world SHG microfinance operational rules.

---

## 📌 Table of Contents
1. [Project Overview](#-project-overview)
2. [Key Features](#-key-features)
3. [Technology Stack](#-technology-stack)
4. [SHG Business Rules](#-shg-business-rules)
5. [Database Design & Entity Relationships](#-database-design--entity-relationships)
6. [Project Structure](#-project-structure)
7. [Step-by-Step Setup & How to Run](#-step-by-step-setup--how-to-run)
   - [Database Setup (MySQL)](#1-database-setup-mysql)
   - [Running the Spring Boot Backend](#2-running-the-spring-boot-backend)
   - [Running the Frontend](#3-running-the-frontend)
   - [Zero-Config H2 Mode (Alternative for Offline Viva)](#4-zero-config-h2-mode-alternative-for-offline-viva)
8. [Swagger / OpenAPI Documentation](#-swagger--openapi-documentation)
9. [REST API Endpoints Reference](#-rest-api-endpoints-reference)
10. [Sample API Requests & Payloads](#-sample-api-requests--payloads)
11. [Automated Test Cases](#-automated-test-cases)
12. [College Viva Q&A Guide](#-college-viva-qa-guide)

---

## 📖 Project Overview

Women's Self-Help Groups (SHGs) are grassroots financial associations where 10–20 women pool regular weekly or monthly savings into a common fund. This pooled capital is used to disburse small, low-interest collateral-free internal loans to group members for emergencies, healthcare, education, or micro-enterprises.

**MicroSave** solves the record-keeping challenge by providing:
- Member registration and profile management.
- Weekly/monthly savings contribution tracking.
- Internal loan disbursement strictly governed by available pooled funds.
- Partial and complete loan repayment management.
- Real-time group financial dashboard and personal member savings statements.

---

## ✨ Key Features

1. **Group Financial Dashboard:** Live display of Total Members, Total Savings Deposited, Total Loans Disbursed, Outstanding Debt, and Group Available Pool.
2. **Member Management:** Register, update, and remove members, with quick access to personal financial summaries.
3. **Savings Contributions:** Record member deposits that directly increment group pooled capital.
4. **Internal Micro-Credit Disbursal:** Disburse loans with real-time checks against group liquidity and active loan constraints.
5. **Loan Repayments:** Flexible installment repayments with automatic debt reduction and status transition to `CLOSED` when zero balance is reached.
6. **Built-in Interactive API Docs:** Interactive Swagger UI for live endpoint testing and demonstration.
7. **Clean College-Friendly Architecture:** Clear Controller $\rightarrow$ Service $\rightarrow$ Repository $\rightarrow$ Database layered architecture without complex frameworks or confusing boilerplates.

---

## 🛠 Technology Stack

### Backend
- **Language:** Java 17
- **Framework:** Spring Boot 3.2.4
- **Modules:** Spring Web, Spring Data JPA, Hibernate, Bean Validation
- **Database:** MySQL 8.0+ (with optional H2 in-memory profile)
- **Documentation:** Swagger UI / OpenAPI 3.0 (`springdoc-openapi`)
- **Build Tool:** Apache Maven 3.9+
- **Port:** `8080`

### Frontend
- **Structure:** Semantic HTML5
- **Styling:** Custom Vanilla CSS3 (Responsive grid, status badges, modern clean cards)
- **Scripting:** Vanilla JavaScript (ES6+), Fetch API (Async/Await)
- **Design:** Zero dependencies (No React, Angular, Vue, or Tailwind)

---

## ⚖️ SHG Business Rules

These rules are enforced in the **Service Layer** (`LoanService.java` and `RepaymentService.java`) and validated on the backend:

| Rule | Name | Description |
| :--- | :--- | :--- |
| **RULE 1** | **Available Pool Calculation** | $\text{Available Pool} = \text{Total Contributions} - \text{Total Outstanding Loans}$ |
| **RULE 2** | **Single Active Loan Policy** | A member with an active unpaid loan cannot take another loan. (*Reject: "Member already has an active unpaid loan."*) |
| **RULE 3** | **Pool Limit Enforcement** | A loan amount cannot exceed the group's current available pool. (*Reject: "Loan amount exceeds the available group pool."*) |
| **RULE 4** | **Positive Loan Amount** | Loan amount must be strictly greater than zero. |
| **RULE 5** | **Positive Repayment Amount** | Repayment amount must be strictly greater than zero. |
| **RULE 6** | **Repayment Cap** | Repayment cannot exceed the outstanding loan balance. (*Reject: "Repayment amount cannot exceed outstanding loan."*) |
| **RULE 7** | **Automatic Loan Closure** | When outstanding balance reaches $0$, status automatically transitions from `ACTIVE` to `CLOSED`. |
| **RULE 8** | **Active Status Check** | Only `ACTIVE` loans can receive repayments. (*Reject: "Loan is already fully repaid."*) |

---

## 🗄 Database Design & Entity Relationships

### Conceptual Hierarchy
```
GROUP (groups)
  │
  ├── MEMBERS (members)
  │     │
  │     └── CONTRIBUTIONS (contributions)
  │
  └── LOANS (loans)
        │
        └── REPAYMENTS (repayments)
```

### Table Schemas

#### 1. `groups` Table
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `name` (VARCHAR, NOT NULL)
- `description` (TEXT)
- `created_date` (DATE, NOT NULL)

#### 2. `members` Table
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `name` (VARCHAR, NOT NULL)
- `phone` (VARCHAR, NOT NULL)
- `email` (VARCHAR)
- `address` (TEXT)
- `group_id` (BIGINT, FK $\rightarrow$ `groups.id`)

#### 3. `contributions` Table
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `member_id` (BIGINT, FK $\rightarrow$ `members.id`)
- `group_id` (BIGINT, FK $\rightarrow$ `groups.id`)
- `amount` (DOUBLE, NOT NULL)
- `contribution_date` (DATE, NOT NULL)
- `description` (VARCHAR)

#### 4. `loans` Table
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `member_id` (BIGINT, FK $\rightarrow$ `members.id`)
- `group_id` (BIGINT, FK $\rightarrow$ `groups.id`)
- `amount` (DOUBLE, NOT NULL)
- `outstanding_amount` (DOUBLE, NOT NULL)
- `loan_date` (DATE, NOT NULL)
- `status` (VARCHAR: `ACTIVE` or `CLOSED`)
- `description` (VARCHAR)

#### 5. `repayments` Table
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `loan_id` (BIGINT, FK $\rightarrow$ `loans.id`)
- `amount` (DOUBLE, NOT NULL)
- `repayment_date` (DATE, NOT NULL)
- `description` (VARCHAR)

---

## 📁 Project Structure

```
Microsaveproject/
│
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/microsave/
│       │   │   ├── MicroSaveApplication.java
│       │   │   ├── entity/
│       │   │   │   ├── Group.java
│       │   │   │   ├── Member.java
│       │   │   │   ├── Contribution.java
│       │   │   │   ├── Loan.java
│       │   │   │   └── Repayment.java
│       │   │   ├── repository/
│       │   │   │   ├── GroupRepository.java
│       │   │   │   ├── MemberRepository.java
│       │   │   │   ├── ContributionRepository.java
│       │   │   │   ├── LoanRepository.java
│       │   │   │   └── RepaymentRepository.java
│       │   │   ├── service/
│       │   │   │   ├── GroupService.java
│       │   │   │   ├── MemberService.java
│       │   │   │   ├── ContributionService.java
│       │   │   │   ├── LoanService.java
│       │   │   │   ├── RepaymentService.java
│       │   │   │   └── DashboardService.java
│       │   │   ├── controller/
│       │   │   │   ├── GroupController.java
│       │   │   │   ├── MemberController.java
│       │   │   │   ├── ContributionController.java
│       │   │   │   ├── LoanController.java
│       │   │   │   ├── RepaymentController.java
│       │   │   │   └── DashboardController.java
│       │   │   ├── dto/
│       │   │   │   ├── GroupRequest.java
│       │   │   │   ├── MemberRequest.java
│       │   │   │   ├── MemberResponse.java
│       │   │   │   ├── ContributionRequest.java
│       │   │   │   ├── ContributionResponse.java
│       │   │   │   ├── LoanRequest.java
│       │   │   │   ├── LoanResponse.java
│       │   │   │   ├── RepaymentRequest.java
│       │   │   │   ├── RepaymentResponse.java
│       │   │   │   ├── DashboardResponse.java
│       │   │   │   └── MemberSummaryResponse.java
│       │   │   ├── exception/
│       │   │   │   ├── ResourceNotFoundException.java
│       │   │   │   ├── BusinessRuleException.java
│       │   │   │   ├── ErrorResponse.java
│       │   │   │   └── GlobalExceptionHandler.java
│       │   │   └── config/
│       │   │       ├── CorsConfig.java
│       │   │       ├── OpenApiConfig.java
│       │   │       └── DataInitializer.java
│       │   └── resources/
│       │       ├── application.properties
│       │       └── application-h2.properties
│       └── test/
│           ├── java/com/microsave/
│           │   └── MicroSaveBusinessRulesTest.java
│           └── resources/
│               └── application-test.properties
│
├── frontend/
│   ├── index.html            (Dashboard)
│   ├── members.html          (Members Management & Savings Summary)
│   ├── contributions.html    (Savings Deposit & History)
│   ├── loans.html            (Loan Disbursal with Live Pool Check)
│   ├── repayments.html       (Installment Repayment & Transactions)
│   ├── css/
│   │   └── style.css         (Custom stylesheet)
│   └── js/
│       ├── dashboard.js
│       ├── members.js
│       ├── contributions.js
│       ├── loans.js
│       └── repayments.js
│
├── database.sql              (MySQL DB & Sample Data Script)
└── README.md
```

---

## 🚀 Step-by-Step Setup & How to Run

### 1. Database Setup (MySQL)

1. Open your terminal or MySQL Workbench.
2. Login to MySQL:
   ```bash
   mysql -u root -p
   ```
3. Run the provided SQL script:
   ```sql
   source /path/to/Microsaveproject/database.sql;
   ```
   *Or execute:*
   ```sql
   CREATE DATABASE IF NOT EXISTS microsave;
   ```

4. Configure your MySQL password in `backend/src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/microsave?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   spring.datasource.username=root
   spring.datasource.password=YOUR_MYSQL_PASSWORD
   ```
   *(Or pass it as environment variable `DB_PASSWORD` or command-line parameter without editing the file)*.

---

### 2. Running the Spring Boot Backend

Open a terminal inside the `backend` folder:

```bash
cd backend
mvn spring-boot:run
```

With custom MySQL password:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.datasource.password=your_password"
```

The backend server will start at:
👉 **`http://localhost:8080`**

On startup, `DataInitializer.java` will automatically insert sample data if tables are empty:
- Group: **Women Empowerment SHG**
- Members: **Priya Sharma**, **Anita Verma**, **Sunita Rao**
- Total Contributions: **₹15,000**
- Sample Loan: Anita Verma **₹5,000**
- Sample Repayment: Anita Verma **₹2,000** (Remaining Outstanding: **₹3,000**)
- Available Pool: **₹12,000**

---

### 3. Running the Frontend

The frontend consists of standalone static HTML, CSS, and JS files.

#### Option A: VS Code Live Server (Recommended)
1. Open VS Code.
2. Install the **Live Server** extension (Ritwick Dey).
3. Right-click on `frontend/index.html` $\rightarrow$ **Open with Live Server**.
4. The frontend will launch at `http://localhost:5500` or `http://127.0.0.1:5500`.

#### Option B: Python Simple HTTP Server
Open a terminal in the `frontend` folder:
```bash
cd frontend
python -m http.server 5500
```
Open `http://localhost:5500` in any web browser.

#### Option C: Direct Browser Open
Double-click `frontend/index.html` to open directly in Google Chrome, Microsoft Edge, or Mozilla Firefox.

---

### 4. Zero-Config H2 Mode (Alternative for Offline Viva)

If you are presenting your project on a college lab PC where MySQL is not installed or permissions are restricted, run:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

The backend will automatically start using an in-memory H2 database with the same schema and sample data!

---

## ⚡ Swagger / OpenAPI Documentation

Swagger UI is configured and enabled by default. Once the backend is running, open:

👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

Swagger tags include:
- **Groups:** Create, view, update, delete SHG groups.
- **Members:** Member onboarding, directory, and personal financial summaries.
- **Contributions:** Savings contribution logging and history.
- **Loans:** Loan disbursal, directory, and live pool queries.
- **Repayments:** Repayment transactions and loan status updates.
- **Dashboard:** Group summary metrics and available pool calculation.

---

## 📡 REST API Endpoints Reference

### Groups
- `POST /api/groups` — Create a new SHG
- `GET /api/groups` — List all groups
- `GET /api/groups/{id}` — Get group details
- `PUT /api/groups/{id}` — Update group
- `DELETE /api/groups/{id}` — Delete group

### Members
- `POST /api/members` — Register new member
- `GET /api/members` — List all members (supports `?groupId={id}`)
- `GET /api/members/{id}` — Get member by ID
- `PUT /api/members/{id}` — Update member
- `DELETE /api/members/{id}` — Delete member
- `GET /api/members/{memberId}/summary` — Member savings & loan balance summary

### Contributions
- `POST /api/contributions` — Record savings contribution
- `GET /api/contributions` — List all contributions (supports `?groupId={id}&memberId={id}`)
- `GET /api/contributions/{id}` — Get contribution by ID
- `PUT /api/contributions/{id}` — Update contribution
- `DELETE /api/contributions/{id}` — Delete contribution

### Loans
- `POST /api/loans` — Disburse internal loan (enforces pool & active loan checks)
- `GET /api/loans` — List all loans
- `GET /api/loans/{id}` — Get loan by ID
- `GET /api/loans/pool/{groupId}` — Get available lending pool for group
- `PUT /api/loans/{id}` — Update loan
- `DELETE /api/loans/{id}` — Delete loan

### Repayments
- `POST /api/repayments` — Record repayment (reduces outstanding, auto-closes if zero)
- `GET /api/repayments` — List all repayments (supports `?loanId={id}`)
- `GET /api/repayments/{id}` — Get repayment by ID
- `PUT /api/repayments/{id}` — Update repayment
- `DELETE /api/repayments/{id}` — Delete repayment (reverts loan balance)

### Dashboard
- `GET /api/dashboard/{groupId}` — Group financial metrics

---

## 📝 Sample API Requests & Payloads

### 1. Register Member (`POST /api/members`)
```json
{
  "name": "Kavita Devi",
  "phone": "9876543299",
  "email": "kavita.devi@example.com",
  "address": "15 Station Road",
  "groupId": 1
}
```

### 2. Record Savings Contribution (`POST /api/contributions`)
```json
{
  "memberId": 1,
  "groupId": 1,
  "amount": 2000.0,
  "contributionDate": "2026-09-28",
  "description": "Weekly savings deposit"
}
```

### 3. Disburse Loan (`POST /api/loans`)
```json
{
  "memberId": 1,
  "groupId": 1,
  "amount": 5000.0,
  "loanDate": "2026-09-28",
  "description": "Purchase of tailoring materials"
}
```

### 4. Record Repayment (`POST /api/repayments`)
```json
{
  "loanId": 1,
  "amount": 1000.0,
  "repaymentDate": "2026-09-28",
  "description": "Monthly loan installment"
}
```

### 5. Group Dashboard Response (`GET /api/dashboard/1`)
```json
{
  "groupId": 1,
  "groupName": "Women Empowerment SHG",
  "totalMembers": 3,
  "totalContributions": 15000.0,
  "totalLoans": 5000.0,
  "totalOutstandingLoans": 3000.0,
  "availablePool": 12000.0
}
```

### 6. Member Summary Response (`GET /api/members/1/summary`)
```json
{
  "memberId": 1,
  "memberName": "Priya Sharma",
  "totalSavings": 5000.0,
  "activeLoanAmount": 0.0,
  "outstandingLoan": 0.0
}
```

---

## 🧪 Automated Test Cases

The project includes automated integration tests in `backend/src/test/java/com/microsave/MicroSaveBusinessRulesTest.java` verifying all 11 requirements:

To run all tests:
```bash
cd backend
mvn test
```

### Test Coverage Checklist:
1. `testGroupAndMemberCreation`: Tests group and member persistence.
2. `testRecordContribution`: Tests savings contribution recording and pool increment.
3. `testDisburseValidLoan`: Tests valid loan disbursal within available liquidity.
4. `testRejectLoanExceedingAvailablePool`: Rejects loan $> \text{Available Pool}$.
5. `testRejectSecondActiveLoanForSameMember`: Rejects second loan for member with active unpaid debt.
6. `testRecordValidRepayment`: Verifies balance deduction on partial repayment.
7. `testRejectRepaymentExceedingOutstanding`: Rejects repayment $> \text{Outstanding Amount}$.
8. `testAutomaticallyCloseFullyRepaidLoan`: Verifies loan status changes to `CLOSED` when balance reaches $0$, and ensures member can borrow again.
9. `testMemberSummary`: Validates personal savings and debt metrics aggregation.
10. `testGroupDashboardSummary`: Validates group-level metrics and available pool equation.

---

## 🎓 College Viva Q&A Guide

### Q1: What is the core business objective of this project?
**Answer:** The project digitizes financial accounting for a women's Self-Help Group (SHG). It maintains members' weekly savings, tracks internal peer-to-peer micro-credit loans, records installment repayments, and calculates the available group capital pool.

### Q2: What is the formula for the Group Available Pool?
**Answer:** 
$$\text{Available Pool} = \text{Total Member Savings Contributions} - \text{Total Active Outstanding Loans}$$
This ensures the group never disburses more money than what is physically available in the shared treasury.

### Q3: How is the "Single Active Loan" rule enforced?
**Answer:** In `LoanService.java`, before saving a new loan record, we call `loanRepository.existsByMemberIdAndStatus(memberId, "ACTIVE")`. If `true`, the application throws a custom `BusinessRuleException` with the message: *"Member already has an active unpaid loan."*

### Q4: What happens when a loan is fully repaid?
**Answer:** In `RepaymentService.java`, every repayment reduces the loan's `outstandingAmount`. When the balance reaches $0$, the loan's status is automatically updated from `ACTIVE` to `CLOSED`. Once closed, the member becomes eligible to apply for a new loan.

### Q5: How is layered architecture maintained in this project?
**Answer:**
- **Controller Layer:** Handles HTTP requests, input validation (`@Valid`), and HTTP response status codes.
- **Service Layer:** Houses all business rules, calculations (available pool), and transaction boundaries (`@Transactional`).
- **Repository Layer:** Extends Spring Data JPA's `JpaRepository` and executes custom `@Query` aggregations.
- **Database:** Relational MySQL schema with foreign keys and referential integrity.

### Q6: Why did you not use complex frontend frameworks like React or Angular?
**Answer:** For semester examination purposes, Vanilla HTML, CSS, and JavaScript with the modern Fetch API provide complete transparency. It allows the examiner to inspect the pure DOM manipulation, HTTP request-response cycle, and CORS integration without hidden abstraction layers or heavy build tools.
#   m i c r o s a v e  
 