# SmartQuizApp

This is a Spring Boot Java Online Quiz application scaffold created for quick testing and deployment.

## Run locally (MySQL)
1. Create a MySQL database `smartquizapp` and set permissions.
2. Update environment variables or edit `src/main/resources/application.properties`.
3. Build and run:
   mvn clean package
   java -jar target/smartquizapp-0.0.1-SNAPSHOT.jar
4. Open http://localhost:8080
5. Admin user is seeded by `data.sql`: username `admin`, password `admin` (plaintext for demo).

## Deploy on Render
- Push this repo to GitHub, create a Render Web Service (Docker), and connect the repo.
- Set env vars in Render: `JDBC_DATABASE_URL`, `JDBC_DATABASE_USERNAME`, `JDBC_DATABASE_PASSWORD`.
- Render will build using Dockerfile.

