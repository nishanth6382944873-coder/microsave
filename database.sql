-- ==============================================================
-- MicroSave – Self-Help Group Savings Tracker
-- MySQL Database Setup & Initialization Script
-- ==============================================================

-- 1. Create Database if not exists
CREATE DATABASE IF NOT EXISTS microsave CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE microsave;

-- 2. Drop tables if recreating (order respects foreign key constraints)
DROP TABLE IF EXISTS repayments;
DROP TABLE IF EXISTS loans;
DROP TABLE IF EXISTS contributions;
DROP TABLE IF EXISTS members;
DROP TABLE IF EXISTS `groups`;

-- --------------------------------------------------------------
-- Table: groups
-- --------------------------------------------------------------
CREATE TABLE `groups` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_date DATE NOT NULL
) ENGINE=InnoDB;

-- --------------------------------------------------------------
-- Table: members
-- --------------------------------------------------------------
CREATE TABLE members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(255),
    address TEXT,
    group_id BIGINT NOT NULL,
    CONSTRAINT fk_members_group FOREIGN KEY (group_id) REFERENCES `groups` (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- --------------------------------------------------------------
-- Table: contributions
-- --------------------------------------------------------------
CREATE TABLE contributions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    amount DOUBLE NOT NULL,
    contribution_date DATE NOT NULL,
    description VARCHAR(255),
    CONSTRAINT fk_contributions_member FOREIGN KEY (member_id) REFERENCES members (id) ON DELETE CASCADE,
    CONSTRAINT fk_contributions_group FOREIGN KEY (group_id) REFERENCES `groups` (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- --------------------------------------------------------------
-- Table: loans
-- --------------------------------------------------------------
CREATE TABLE loans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    amount DOUBLE NOT NULL,
    outstanding_amount DOUBLE NOT NULL,
    loan_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT fk_loans_member FOREIGN KEY (member_id) REFERENCES members (id) ON DELETE CASCADE,
    CONSTRAINT fk_loans_group FOREIGN KEY (group_id) REFERENCES `groups` (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- --------------------------------------------------------------
-- Table: repayments
-- --------------------------------------------------------------
CREATE TABLE repayments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    amount DOUBLE NOT NULL,
    repayment_date DATE NOT NULL,
    description VARCHAR(255),
    CONSTRAINT fk_repayments_loan FOREIGN KEY (loan_id) REFERENCES loans (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ==============================================================
-- Sample Data Insertion
-- ==============================================================

-- 1. Insert Group
INSERT INTO `groups` (id, name, description, created_date) VALUES 
(1, 'Women Empowerment SHG', 'Local self-help group empowering women through collective savings and micro-credit.', CURDATE() - INTERVAL 90 DAY);

-- 2. Insert Members
INSERT INTO members (id, name, phone, email, address, group_id) VALUES 
(1, 'Priya Sharma', '9876543210', 'priya.sharma@example.com', '12 Gandhi Nagar, Sector 4', 1),
(2, 'Anita Verma', '9876543211', 'anita.verma@example.com', '45 Market Road, Near Temple', 1),
(3, 'Sunita Rao', '9876543212', 'sunita.rao@example.com', '78 Lake View Colony', 1);

-- 3. Insert Savings Contributions (Total = ₹15,000)
INSERT INTO contributions (id, member_id, group_id, amount, contribution_date, description) VALUES 
(1, 1, 1, 5000.0, CURDATE() - INTERVAL 30 DAY, 'Monthly savings deposit'),
(2, 2, 1, 4000.0, CURDATE() - INTERVAL 30 DAY, 'Monthly savings deposit'),
(3, 3, 1, 6000.0, CURDATE() - INTERVAL 30 DAY, 'Monthly savings deposit');

-- 4. Insert Sample Loan (Anita Verma borrows ₹5,000; Pool was ₹15,000)
INSERT INTO loans (id, member_id, group_id, amount, outstanding_amount, loan_date, status, description) VALUES 
(1, 2, 1, 5000.0, 3000.0, CURDATE() - INTERVAL 15 DAY, 'ACTIVE', 'Small business inventory purchase');

-- 5. Insert Sample Repayment (Anita Verma repays ₹2,000, leaving ₹3,000 outstanding)
INSERT INTO repayments (id, loan_id, amount, repayment_date, description) VALUES 
(1, 1, 2000.0, CURDATE() - INTERVAL 5 DAY, 'First installment repayment');

-- Reset Auto-Increment Sequences
ALTER TABLE `groups` AUTO_INCREMENT = 2;
ALTER TABLE members AUTO_INCREMENT = 4;
ALTER TABLE contributions AUTO_INCREMENT = 4;
ALTER TABLE loans AUTO_INCREMENT = 2;
ALTER TABLE repayments AUTO_INCREMENT = 2;
