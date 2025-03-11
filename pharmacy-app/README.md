# Pharmacy Management System

A comprehensive pharmacy management system built with Spring Boot, Thymeleaf, and MySQL.

## Features

- User authentication and authorization with role-based access control
- Medicine management (CRUD operations)
- Medicine booking system
- Pharmacist management
- Supplier management
- Stock transaction tracking
- Audit logging
- Admin dashboard with reporting

## User Roles

- **Regular User**: Can view medicines, create and manage bookings
- **Pharmacist**: Can manage medicines, approve/reject bookings, update stock
- **Admin**: Full system access, including user management and reporting

## Technologies Used

- Java 11
- Spring Boot 2.7.x
- Spring Security with JWT
- Spring Data JPA
- Thymeleaf
- MySQL 8.0
- Bootstrap 5
- Maven

## Prerequisites

- JDK 11 or later
- Maven 3.6 or later
- MySQL 8.0 or later

## Getting Started

### Database Setup

1. Create a MySQL database named `pharmacy_db`
2. Update the database credentials in `src/main/resources/application.properties` if needed

### Running the Application

1. Clone the repository

