# Kanban API

A simple Kanban API that lets you store, update, patch and delete tasks. It is using WebSocket to send updates to the frontend

## Technologies used:

Java version: 23

REST: Spring Boot, Spring HATEOS, Springdoc-openapi, Swagger UI, Bucket4J

Persistance: Spring Data JPA, Hibernate, MySQL, Flyway

Security: Spring security (JWT filter)

Real-time: Spring WebSocket, STOMP, SockJS fallback

Build: Maven, Docker, Docker compose, Docker buildkit

Tests: JUnit 5, Mockito, Testcontainers

Observability: Spring Boot actuator

Other: GraphQL, Jacoco for CI test coverage check

## How to run

1. Make sure you have docker compose
2. Run docker-compose up --build in root folder
3. Application will be available on port 8080 
4. To generate JWT, just go to https://jwt.io/ and generate one using the jwt.secret in application.properties and send it as header "Bearer {token value}"

   JWT secret on the frontend is hard-coded, so if you change it here on the backend, the front will not work

# OpenAPI

OpenAPI is available at http://localhost:8080/swagger-ui

# Frontend

Front end is deployed at https://kanban-task-manager-frontend.lovable.app/. It is using localhost:8080 to fetch the backend from your local machine, so make sure the app is running on your machine.
Note that the front end is mostly generate by Lovable (vibe coding) just to visually demonstrate the API.

# WebSocket listener helper

In /ws-listener folder, you can connect to the session and see the messages that are published:

1. cd ws-listener/
2. npm install sockjs-client @stomp/stompjs
3. node --experimental-modules listen.js
