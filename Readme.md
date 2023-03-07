Spring Boot Application using in-memory H2 database
Spring security
Spring Data JPA
Spring Boot 3.0.4

JWT Token based auth with Roles elevation
H2-ui to access in memory db

Java 17

Based on : https://www.bezkoder.com/spring-boot-security-login-jwt/

---
testdb.mv.db, testdb.trace.db will be generated at basedir

you need to populate the db with 3 roles to be able to register users :
ROLE_USER, ROLE_ADMIN, ROLE_MODERATOR