# Learning Management System (LMS)

## Overview

This Learning Management System (LMS) is a comprehensive web application designed to facilitate online education and training. It supports various user roles (Admin, Instructor, Student), course management, content delivery, assessment tools, and progress tracking.

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Setup and Installation](#setup-and-installation)
- [API Documentation](#api-documentation)
- [Security](#security)
- [Database Schema](#database-schema)
- [Contributing](#contributing)
- [License](#license)

## Features

### User Management
- Registration/login with JWT authentication
- Role-based access control (Admin, Instructor, Student)
- User profiles with customizable settings
- Batch user import functionality

### Course Management
- Create, update, delete, and list courses
- Assign instructors and department classification
- Course prerequisites and capacity management
- Course statistics and analytics

### Enrollment System
- Students can enroll/unenroll in courses
- Track student progress and course completion
- Enrollment validation (prerequisites, capacity)

### Content Management
- Upload and organize lectures (PDFs, videos, quizzes)
- Module-based content organization
- Content tagging and search functionality
- Content access tracking

### Assessment Module
- Multiple types of quizzes and questions
- Auto-grading for objective questions
- Manual grading for subjective questions
- Quiz analytics and performance tracking

### Certification
- Automatic certificate generation upon course completion
- Certificate verification system
- LinkedIn integration for sharing certificates

### Notification System
- Email and in-app notifications
- Configurable notification preferences
- Event-based alerts (deadlines, grading, etc.)

### Reporting and Analytics
- Student progress reports
- Course completion statistics
- Performance analytics

## Architecture

The project is initially built as a monolithic application with a structured layered architecture:

- **Presentation Layer**: RESTful controllers
- **Service Layer**: Business logic and service implementations
- **Data Access Layer**: Repositories and data models
- **Security Layer**: Authentication, authorization, and security filters

Each module follows a similar pattern with controllers, services, repositories, and models organized by domain.

## Technology Stack

### Backend
- **Framework**: Spring Boot
- **Language**: Java
- **Security**: Spring Security, JWT
- **Database**: PostgreSQL/MySQL
- **ORM**: Hibernate (JPA)
- **Build Tool**: Maven

### Frontend (Planned for Step 2)
- **Framework**: React.js
- **State Management**: React Context API or Redux
- **UI Components**: Material UI, Bootstrap, or Tailwind CSS

## Project Structure

The project is organized by functional domains:

```
src/main/java/com/example/lms/
├── assessment/           # Quiz and assessment functionality
├── certificate/          # Certificate generation and management
├── common/               # Shared utilities and base classes
├── content/              # Course content management
├── course/               # Course management
├── Department/           # Department management
├── enrollment/           # Student enrollment
├── logging/              # Activity logging
├── notification/         # Notification system
├── progress/             # Student progress tracking
├── report/               # Reporting functionality
├── security/             # Authentication and authorization
└── user/                 # User management
```

Each module typically contains:
- `controller/`: REST API endpoints
- `service/`: Business logic implementations
- `repository/`: Data access interfaces
- `model/`: Entity classes
- `dto/`: Data transfer objects
- `mapper/`: Entity-DTO conversion

## Setup and Installation

### Prerequisites
- JDK 17 or higher
- Maven 3.6+
- PostgreSQL or MySQL database
- IDE (IntelliJ IDEA, Eclipse, or VS Code with Java extensions)

### Database Configuration

Configure your database connection in `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/lms_db
spring.datasource.username=postgres
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### Build and Run

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/lms-project.git
   cd lms-project
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

4. Access the API at: http://localhost:8080/api

### Initial Setup

The system automatically creates default roles and an admin user at startup:
- Default admin credentials:
  - Email: admin@example.com
  - Password: admin123 (change immediately after first login)

## API Documentation

The API documentation is available through Swagger UI:
- URL: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs

### Key API Endpoints

- Authentication: `/api/auth/*`
- Users: `/api/v1/users/*`
- Admin: `/api/admin/*`
- Courses: `/api/courses/*`
- Content: `/api/contents/*`
- Quizzes: `/api/quizzes/*`
- Enrollments: `/api/enrollments/*`
- Certificates: `/api/certificates/*`
- Notifications: `/api/notifications/*`

## Security

The application implements several security features:

- JWT-based authentication
- Role-based access control
- Password encryption
- CSRF protection
- Input validation
- Secure JWT token storage

## Database Schema

The main entities in the database schema include:

- Users
- Roles and Permissions
- Courses
- Departments
- Content
- Quizzes and Questions
- Enrollments
- Progress Records
- Certificates
- Notifications

Detailed ERD diagrams are available in the `docs/database` directory.

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/new-feature`
3. Commit your changes: `git commit -am 'Add new feature'`
4. Push to the branch: `git push origin feature/new-feature`
5. Submit a pull request

Please follow the existing code style and include appropriate tests.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## Project Milestones

### Step 1: Monolithic Backend Development (Completed)
- Full backend functionality with REST APIs
- Monolithic architecture
- JWT authentication
- Database integration

### Step 2: Frontend Development (Completed)
- React-based frontend
- Integration with backend APIs
- Responsive UI design

### Step 3: Microservices Conversion (Partially Completed)
- Refactor into microservices
- Service discovery
- API gateway
- Message queuing
