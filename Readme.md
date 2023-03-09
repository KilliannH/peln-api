# Introduction
An API to communicate with Bloom.
R&D [WIP] Project

Spring Boot Application using in-memory H2 database
Spring security
Spring Data JPA
Spring Boot 3.0.4

JWT Token based auth with Roles elevation
H2-ui to access in memory db

Java 17

# Getting Started
1.	Copy resources/application.properties.example to resources/application.properties
2.	Change the appropriate values (ex: token.secret needs to be at least a 512bytes key)
3.	make sure you have Java 17 installed and setup in your project structure and Maven
4.	mvn clean
5.  mvn install
6.  Run the application
7.  testdb.mv.db and testdb.trace.db files will be created at basedir (document based db for persistence)
8.  Navigate to localhost:9000/h2-ui
9.  Connect to db using credentials from application.properties, "jdbc:h2:./testdb" for JDBC Url.
10. you need to populate the db with 3 roles to be able to register users, run resources/init.sql in h2-ui
11. you can use the api and bypass security for tests using security.enabled in application.properties

# Build and Test
make sure you have maven and Java configured in your $PATH
```nvm clean install```
to build a jarfile ```nvm package```
to run jarfile ```java -jar ./path-to-.jar```

# Contribute
TODO: Explain how other users and developers can contribute to make your code better.

If you want to learn more about creating good readme files then refer the following [guidelines](https://docs.microsoft.com/en-us/azure/devops/repos/git/create-a-readme?view=azure-devops). You can also seek inspiration from the below readme files:
- [ASP.NET Core](https://github.com/aspnet/Home)
- [Visual Studio Code](https://github.com/Microsoft/vscode)
- [Chakra Core](https://github.com/Microsoft/ChakraCore)