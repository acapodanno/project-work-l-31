# NOTA: questo Dockerfile combinato è mantenuto per compatibilità, ma è
# stato superato da due Dockerfile dedicati e più completi (con stage `test`
# per il code coverage):
#   backend/Dockerfile   (Spring Boot, target: deps | test | build | runtime, porta 8080)
#   frontend/Dockerfile  (Angular + Nginx, target: deps | test | build | runtime, porta 80)
#
# Per avviare l'intero stack in un colpo solo:
#   docker compose up --build
#
# Per eseguire solo i test con coverage dei due servizi:
#   docker compose -f docker-compose.test.yml build
#
# Dockerfile per l'avvio del build del progetto full-stack

# Fase 1: Build del backend Spring Boot
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY backend/pom.xml backend/
COPY backend/src backend/src
RUN mvn -f backend/pom.xml clean package -DskipTests

# Fase 2: Build del frontend Angular
FROM node:23-alpine AS frontend-build
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# Fase 3: Runtime finale (Esempio per il backend)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/backend/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
