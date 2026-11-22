# StaffSync — Employee Management System

Full stack employee management app. Admins add, edit and remove employees (with photos); signed-in users can browse, search and filter the team. Built with Spring Boot + MySQL, with a vanilla JavaScript UI served by the same app.

## Tech Stack

- Java 17, Spring Boot 3.5.6 (Web, Data JPA / Hibernate)
- MySQL
- JWT (HS256) with HttpSession fallback, BCrypt password hashing
- HTML, CSS, JavaScript frontend (served from `src/main/resources/static`)
- Maven

## Features

- Sign up, sign in, logout, current-user lookup
- Role-based access: `ROLE_ADMIN` can create, update and delete employees; everyone else is read-only
- Employee CRUD with photo upload (JPG, PNG, WEBP or GIF, up to 20 MB per file)
- Search across name, department, project, location and contact
- Department filter, grid and table views, light / dark theme
- Dashboard stats: total employees, departments and projects

## Getting Started

1. Create a MySQL database (the app creates and updates tables automatically via `ddl-auto=update`).
2. Set environment variables (or edit `application.properties` for local use):

   | Variable | Purpose | Default |
   |---|---|---|
   | `DB_URL` | JDBC URL, e.g. `jdbc:mysql://localhost:3306/<database>` | — |
   | `DB_USERNAME` | Database user | `root` |
   | `DB_PASSWORD` | Database password | — |
   | `APP_JWT_SECRET` | JWT signing secret (use a long random string) | — |
   | `CORS_ORIGINS` | Comma-separated allowed origins | — |
   | `UPLOAD_DIR` | Folder for uploaded photos | `uploads` |
   | `PORT` | Server port | `8081` |

3. Generate a JWT secret: `openssl rand -base64 48`
4. Run: `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`)
5. Open `http://localhost:8081`

Default admin and user accounts are created on first start. Change their passwords before deploying anywhere public.

## API Notes

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/signup`, `/api/auth/signin`, `/api/auth/logout` | Public |
| GET | `/api/auth/me` | Signed in |
| GET | `/api/employees?search=` | Public |
| GET | `/api/employees/{id}`, `/api/employees/stats` | Public |
| POST | `/api/employees` (multipart or JSON) | Admin |
| PUT | `/api/employees/{id}` | Admin |
| POST | `/api/employees/{id}/photo` | Admin |
| DELETE | `/api/employees/{id}` | Admin |

Send the token as `Authorization: Bearer <token>`. Uploaded photos are served from `/images/**`.

## Project Structure

```
src/main/java/com/mainapp/
├── controller/   # EmployeeCrud, SigninSignupLogout
├── service/      # EmployeeService, UserService
├── respository/  # Spring Data JPA repositories
├── entity/       # Employee, User
├── security/     # AuthInterceptor, JwtUtil
└── config/       # AppConfig (CORS, static resources), SecurityConfig (BCrypt)
src/main/resources/static/   # index.html, app.js, styles.css
```

## Known Gaps

- Employee photos are stored on local disk, so they are lost on hosts with ephemeral storage (use Cloudinary or S3 for production)
- JWT is hand-rolled and access control is a custom interceptor, not Spring Security
- No refresh tokens, pagination or input validation annotations yet
- Only a context-load test; no unit or integration tests
- No Dockerfile, Docker Compose or CI pipeline

## Author

Sai Pardeshi — [GitHub](https://github.com/saipardeshi) · [LinkedIn](https://linkedin.com/in/sai-pardeshi)
