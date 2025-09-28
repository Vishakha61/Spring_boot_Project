# Spring Boot Inventory & Billing System

A complete inventory and billing management system built with Spring Boot, featuring user authentication and authorization.

## Features

### Authentication & Authorization
- **User Registration**: New users can create accounts with email validation
- **User Login**: Secure login with username/password
- **Session Management**: Automatic session handling and logout functionality
- **Role-based Access**: Support for USER and ADMIN roles
- **Spring Security**: Comprehensive security configuration

### Business Features
- Item Management
- Bill Generation
- Sales Tracking
- Sales Reports

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Running the Application

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Spring_boot_Project
   ```

2. **Build the project**
   ```bash
   mvn clean compile
   ```

3. **Run the billing service**
   ```bash
   cd billing-service
   mvn spring-boot:run
   ```

4. **Access the application**
   Open your browser and navigate to: `http://localhost:8080`

## User Authentication

### Default Users
The application comes with two pre-created users:

#### Admin User
- **Username**: `admin`
- **Password**: `admin123`
- **Role**: ADMIN
- **Email**: admin@example.com

#### Regular User
- **Username**: `user`
- **Password**: `user123`
- **Role**: USER
- **Email**: user@example.com

### Registration Process

1. **Navigate to Registration**
   - Go to `http://localhost:8080/register`
   - Or click "Register here" on the login page

2. **Fill out the form**
   - First Name and Last Name
   - Username (must be unique)
   - Email (must be unique and valid format)
   - Password (minimum 6 characters)
   - Confirm Password (must match)

3. **Account Creation**
   - Upon successful registration, you'll be redirected to the login page
   - New accounts are created with USER role by default

### Login Process

1. **Navigate to Login**
   - The application will automatically redirect to login page if not authenticated
   - Direct URL: `http://localhost:8080/login`

2. **Enter Credentials**
   - Username: Your registered username
   - Password: Your password

3. **Access Dashboard**
   - Upon successful login, you'll be redirected to the main dashboard
   - The dashboard shows your username and provides access to all features

### Logout

- Click the "Logout" button in the top-right corner of any page
- You'll be redirected to the login page with a confirmation message

## Application Structure

### Authentication Components

- **User Entity**: `com.example.billing.model.User`
  - Stores user information including credentials and roles
  - Timestamps for creation and updates

- **UserRepository**: `com.example.billing.repository.UserRepository`
  - JPA repository for user data access
  - Custom methods for finding users by username/email

- **UserService**: `com.example.billing.service.UserService`
  - Implements UserDetailsService for Spring Security
  - Handles user registration and validation
  - Password encryption with BCrypt

- **AuthController**: `com.example.billing.controller.AuthController`
  - Handles login and registration endpoints
  - Form validation and error handling

- **SecurityConfig**: `com.example.billing.config.SecurityConfig`
  - Spring Security configuration
  - URL-based security rules
  - Login/logout configuration

### Templates

- **login.html**: Styled login form with error handling
- **register.html**: User registration form with client-side validation
- **index.html**: Main dashboard with user info and navigation
- **access-denied.html**: Error page for unauthorized access

## Security Features

### Password Security
- BCrypt encryption for password storage
- Minimum password length validation
- Password confirmation during registration

### Session Management
- Automatic session creation on login
- Session invalidation on logout
- Session timeout handling

### Input Validation
- Server-side validation for all forms
- Client-side validation for improved UX
- Unique constraints for username and email

### CSRF Protection
- CSRF tokens for all forms
- Exception for H2 console (development only)

## Database

The application uses H2 in-memory database for development:
- **Console**: `http://localhost:8080/h2-console`
- **URL**: `jdbc:h2:mem:inventorydb`
- **Username**: `SA`
- **Password**: (empty)

### User Table Schema
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    role VARCHAR(255) CHECK (role IN ('USER','ADMIN')),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

## Error Handling

- **Login Errors**: Invalid credentials show user-friendly messages
- **Registration Errors**: Validation errors displayed inline
- **Access Denied**: Custom error page for unauthorized access
- **Session Expiry**: Automatic redirect to login page

## Customization

### Adding New Roles
1. Update the `User.Role` enum
2. Modify security configuration in `SecurityConfig`
3. Add role-based UI elements as needed

### Password Policy
Modify validation in:
- `AuthController.registerUser()` method
- Client-side validation in `register.html`

### UI Styling
All templates include embedded CSS and can be customized by modifying the `<style>` sections.

## Troubleshooting

### Common Issues

1. **Port already in use**
   - Stop any existing instances running on port 8080
   - Or change port in `application.yml`

2. **Database connection issues**
   - H2 database is in-memory and recreated on each restart
   - Check H2 console for database state

3. **Authentication not working**
   - Verify user exists in database
   - Check password encoding matches

### Development Tips

- Use H2 console to inspect user data
- Check application logs for detailed error messages
- Default users are recreated on each application restart

## Next Steps

This authentication system provides a solid foundation for:
- Role-based access control for different features
- User profile management
- Password reset functionality
- Email verification
- OAuth integration
- Multi-factor authentication

## Support

For issues or questions, please check the application logs and ensure all dependencies are properly installed.