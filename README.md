# CockroachDB JPA Sandbox

A order system sandbox using CockroachDB and [Spring Data JPA](https://spring.io/projects/spring-data-jpa) 
with Hibernate.

Highlights:

- Intercepting accidental read-only transactions
- Using a routing datasource to switch between read-only and read-write data sources
- Using transaction propagation attributes following ECB (https://blog.cloudneutral.se/)

# Using

## Prerequisites

- JDK8+ with 1.8 language level (OpenJDK compatible)
- Maven 3+ (wrapper provided)
- CockroachDB with a database named `sandbox`

## Building

    ./mvnw clean install
    
## Running

    java -jar target/crdb-jpa-sandbox.jar --spring.datasource.url=jdbc:postgresql://localhost:26257/sandbox?sslmode=disable


        