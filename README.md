# Spring Boot Authentication & Social Platform

A complete social platform built with Spring Boot that demonstrates user authentication, personal profiles, status updates, and community posts with a modern, responsive frontend.

## Features

### Authentication & Security
- **User Registration**: Create new accounts with username, email, and password validation
- **User Authentication**: Secure login system with Spring Security
- **Password Encryption**: BCrypt password hashing for security
- **Session Management**: Secure session handling

### Social Features
- **Personal Profile**: Customizable user profile page
- **Status Updates**: Set and update your current status (up to 200 characters)
- **Post Creation**: Create and share posts with the community (up to 1000 characters)
- **Community Feed**: View all posts from users in chronological order
- **User Profiles**: View other users' profiles and their posts
- **Post Management**: Edit and delete your own posts

### UI & Design
- **Responsive Design**: Modern Bootstrap-based frontend with gradient design
- **Interactive Elements**: Hover animations and smooth transitions
- **Form Validation**: Client-side and server-side validation
- **Real-time Updates**: Dynamic content loading and pagination

### Technical Features
- **H2 Database**: In-memory database for development and testing
- **JPA Relationships**: User-Post one-to-many relationships
- **Pagination**: Efficient handling of large post lists
- **Time Tracking**: Automatic timestamp creation for posts

## Technologies Used

- **Backend**: Spring Boot 3.2.0, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf, Bootstrap 5.1.3, Font Awesome 6.0.0
- **Database**: H2 (in-memory)
- **Build Tool**: Maven
- **Java Version**: 17

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Getting Started

### 1. Clone the repository
```bash
git clone <repository-url>
cd spring-auth-app
```

### 2. Build the application
```bash
mvn clean install
```

### 3. Run the application
```bash
mvn spring-boot:run
```

Alternatively, you can run the JAR file:
```bash
java -jar target/spring-auth-app-1.0.0.jar
```

### 4. Access the application
Open your browser and navigate to: `http://localhost:8080`

## Application URLs

- **Home Page**: `http://localhost:8080/`
- **Login**: `http://localhost:8080/login`
- **Register**: `http://localhost:8080/register`
- **Dashboard**: `http://localhost:8080/dashboard` (requires authentication)
- **Personal Profile**: `http://localhost:8080/personal` (requires authentication)
- **Community Feed**: `http://localhost:8080/personal/feed` (requires authentication)
- **User Profile**: `http://localhost:8080/personal/user/{username}` (requires authentication)
- **H2 Database Console**: `http://localhost:8080/h2-console`

### H2 Database Console Access
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: `password`

## Usage

### Registration & Login
1. Navigate to the registration page
2. Fill in the required fields:
   - Username (3-50 characters, unique)
   - Email (valid email format, unique)
   - Password (minimum 6 characters)
3. Submit the form
4. Upon successful registration, you'll be redirected to the login page
5. Login with your credentials

### Personal Profile Management
1. After login, navigate to "My Profile"
2. **Update Status**: Change your current status (up to 200 characters)
3. **Create Posts**: Share your thoughts with the community (up to 1000 characters)
4. **Manage Posts**: View, edit, or delete your posts
5. **Profile Stats**: View your post count and account information

### Community Interaction
1. **Community Feed**: View all posts from all users
2. **User Profiles**: Click on usernames to view their profiles
3. **Pagination**: Navigate through multiple pages of posts
4. **Real-time Updates**: Posts appear in chronological order

### Dashboard
- View account status and personal statistics
- Quick access to profile and feed
- Access to H2 database console for development

## Project Structure

```
src/
├── main/
│   ├── java/com/example/
│   │   ├── SpringAuthApplication.java          # Main application class
│   │   ├── config/
│   │   │   └── SecurityConfig.java             # Spring Security configuration
│   │   ├── controller/
│   │   │   ├── AuthController.java             # Authentication controller
│   │   │   └── PersonalController.java         # Personal page & posts controller
│   │   ├── model/
│   │   │   ├── User.java                       # User entity with status
│   │   │   └── Post.java                       # Post entity
│   │   ├── repository/
│   │   │   ├── UserRepository.java             # User repository
│   │   │   └── PostRepository.java             # Post repository
│   │   ├── security/
│   │   │   └── CustomUserDetailsService.java  # Custom UserDetailsService
│   │   └── service/
│   │       ├── UserService.java                # User service layer
│   │       └── PostService.java               # Post service layer
│   └── resources/
│       ├── application.properties              # Application configuration
│       └── templates/                          # Thymeleaf templates
│           ├── index.html                      # Home page
│           ├── login.html                      # Login page
│           ├── register.html                   # Registration page
│           ├── dashboard.html                  # User dashboard
│           ├── personal.html                   # Personal profile page
│           ├── feed.html                       # Community feed
│           ├── user-profile.html               # Other user profiles
│           └── layout.html                     # Base layout template
└── pom.xml                                     # Maven dependencies
```

## API Endpoints

### Authentication
- `GET /` - Home page
- `GET /login` - Login page
- `POST /login` - Process login
- `GET /register` - Registration page
- `POST /register` - Process registration
- `POST /logout` - Logout user

### Personal Features (Authenticated)
- `GET /dashboard` - User dashboard
- `GET /personal` - Personal profile page
- `POST /personal/status` - Update user status
- `POST /personal/post` - Create new post
- `POST /personal/post/{id}/delete` - Delete post
- `GET /personal/feed` - Community feed with pagination
- `GET /personal/user/{username}` - View user profile

## Database Schema

### Users Table
- `id` (Primary Key)
- `username` (Unique)
- `email` (Unique)
- `password` (Encrypted)
- `enabled` (Boolean)
- `status` (User status text)

### Posts Table
- `id` (Primary Key)
- `content` (Post content)
- `created_at` (Timestamp)
- `author_id` (Foreign Key to Users)

## Security Features

- **BCrypt Password Encoding**: All passwords are encrypted using BCrypt
- **CSRF Protection**: Cross-Site Request Forgery protection enabled
- **Session Management**: Secure session handling
- **Input Validation**: Server-side validation with Bean Validation
- **SQL Injection Prevention**: JPA repository pattern prevents SQL injection
- **Authorization**: Role-based access control for protected endpoints

## Customization

### Database Configuration
To use a different database, modify `application.properties`:
```properties
# For MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/your_database
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```

### UI Customization
- Modify CSS in the `<style>` sections of HTML templates
- Update Bootstrap version in template files
- Change color scheme by updating CSS custom properties
- Customize post display limits and pagination settings

### Feature Extensions
- Add user avatars and image uploads
- Implement like/comment system for posts
- Add user following/followers functionality
- Implement real-time notifications
- Add post categories and tags

## Development

### Running in Development Mode
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Building for Production
```bash
mvn clean package -Pproduction
```

### Testing
```bash
mvn test
```

## Troubleshooting

### Common Issues

1. **Port 8080 already in use**
   - Change the port in `application.properties`: `server.port=8081`

2. **Database connection issues**
   - Check H2 console settings
   - Verify database URL and credentials

3. **Authentication not working**
   - Clear browser cache and cookies
   - Check if user exists in the database

4. **Posts not displaying**
   - Check if user is properly authenticated
   - Verify database contains post data

5. **Status updates not saving**
   - Ensure status is under 200 characters
   - Check for validation errors in browser console

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## Future Enhancements

- **File Uploads**: User avatars and post images
- **Real-time Features**: WebSocket support for live updates
- **Advanced Search**: Search posts and users
- **Mobile App**: React Native or Flutter mobile application
- **API Documentation**: Swagger/OpenAPI documentation
- **Performance Optimization**: Caching and query optimization

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For questions or issues, please open an issue in the repository or contact the development team. 