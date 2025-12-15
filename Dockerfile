# ===== Étape 1 build =====
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copier uniquement pom.xml pour cache Maven
COPY pom.xml .
RUN mvn dependency:go-offline

# Copier le reste du projet
COPY src ./src

# Build du JAR
RUN mvn clean package -DskipTests

# =====  Étape 2 runtime =====
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copier le JAR depuis l'étape build
COPY --from=build /app/target/*.jar app.jar

# Exposer le port (Render utilise $PORT)
EXPOSE 8080

# Lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
