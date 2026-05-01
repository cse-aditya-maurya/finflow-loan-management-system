# FinFlow – Loan Management System

## Introduction

FinFlow is a web-based loan onboarding and approval system designed to digitize the complete loan application process.
The system allows users to apply for loans online, upload required documents, and track the progress of their applications in real time.
Administrative users can review applications, verify documents, make approval decisions, and generate reports.

This project is built using a microservices architecture with Spring Boot and secured using JWT authentication.

---

## Project Objective

The main objective of this system is to provide an end-to-end digital loan processing platform that reduces manual effort and improves operational efficiency.
It focuses on secure authentication, scalable backend design, and workflow-based loan processing.

---

## System Architecture

The system follows a microservices architecture where the frontend communicates with backend services through an API Gateway.

**Application Flow**

Angular Client → Spring Cloud Gateway → Microservices → Database

Each service is independently deployable and manages its own business logic and data.

---

## Microservices Overview

### 1. Auth Service

Handles user registration, login, and JWT token generation.
It is responsible for authentication and role-based access control.

### 2. Application Service

Manages the loan application lifecycle.
Users can create draft applications, update details, submit applications, and track status.

### 3. Document Service

Handles document upload and management.
Applicants upload KYC and income documents which can later be verified by administrators.

### 4. Admin Service

Used by administrative users to review applications, verify documents, approve or reject loans, and generate reports.

---

## User Roles

### Applicant

* Register and login
* Create or update loan application drafts
* Submit loan applications
* Upload and manage documents
* Track application status

### Admin

* View application queue
* Verify submitted documents
* Approve or reject applications
* Generate operational reports
* Manage system users

---

## Security Implementation

Security is implemented using Spring Security and JWT authentication.

* Users receive a JWT token after successful login
* The token must be included in every protected API request
* Role-based authorization is implemented using method-level security
* All requests are routed through Spring Cloud Gateway

---

## Loan Application Status Lifecycle

The application moves through the following stages:

Draft → Submitted → Documents Pending → Documents Verified → Under Review → Approved / Rejected → Closed

---

## Key API Endpoints

### Authentication

* POST /gateway/auth/signup
* POST /gateway/auth/login

### Applicant

* GET /gateway/applications/my
* POST /gateway/applications
* PUT /gateway/applications/{id}
* POST /gateway/applications/{id}/submit
* GET /gateway/applications/{id}/status

### Documents

* POST /gateway/documents/upload

### Admin

* GET /gateway/admin/applications
* PUT /gateway/admin/documents/{id}/verify
* POST /gateway/admin/applications/{id}/decision
* GET /gateway/admin/reports
* GET /gateway/admin/users
* PUT /gateway/admin/users/{id}

---

## Database Design

Each microservice maintains its own database or schema to ensure loose coupling and scalability.

Main entities used in the system:

* User
* LoanApplication
* Document
* Decision
* Report

JPA and Hibernate are used for ORM and database interaction.

---

## Technology Stack

**Backend**

* Java 17
* Spring Boot
* Spring Security
* JWT
* Spring Cloud Gateway
* Eureka Service Discovery
* Spring Data JPA / Hibernate

**Frontend**

* React

**Database**

* MySQL or PostgreSQL

**Testing**

* JUnit
* Mockito
* Jasmine / Karma

---

## Purpose of the Project

This project is designed to simulate a real-world banking loan processing system.
It demonstrates practical implementation of microservices architecture, secure authentication, workflow-driven business logic, and scalable backend design.

It can be used for learning, portfolio building, and interview preparation.

---
