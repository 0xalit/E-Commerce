FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Create the final lightweight image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copying ONLY the built jar from the last builder
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
