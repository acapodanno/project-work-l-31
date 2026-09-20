# syntax=docker/dockerfile:1
#
# Build multi-stage per il backend Spring Boot.
#
# Target disponibili:
#   deps    -> dipendenze Maven scaricate e cache (non usato direttamente)
#   test    -> esegue `mvn verify` (JUnit + JaCoCo), non produce un'immagine da eseguire
#   build   -> pacchettizza il jar (senza rieseguire i test)
#   runtime -> immagine finale minimale (target di default)
#
# Esempi:
#   docker build -t healthcare-backend .                       # immagine runtime
#   docker build --target test -t healthcare-backend:test .    # solo per eseguire i test in un container
#   DOCKER_BUILDKIT=1 docker build --target test \
#       --output type=local,dest=./coverage/backend .          # estrae il report JaCoCo su host

# ---------- Stage 1: dipendenze ----------
FROM maven:3.9-eclipse-temurin-21 AS deps
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline

# ---------- Stage 2: test + code coverage (JaCoCo) ----------
FROM deps AS test
COPY src ./src
# `verify` esegue anche il goal `report` di JaCoCo configurato nel pom.xml.
# Report HTML: target/site/jacoco/index.html — Report XML (per tool esterni): target/site/jacoco/jacoco.xml
RUN mvn -B clean verify

# ---------- Stage 3: build del jar (senza rieseguire i test, già eseguiti nello stage `test`) ----------
FROM deps AS build
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Stage 4: runtime minimale ----------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
# wget serve solo per l'HEALTHCHECK di docker-compose (Swagger è pubblico,
# quindi /v3/api-docs risponde 200 senza autenticazione).
RUN apk add --no-cache wget && \
    addgroup -S spring && adduser -S spring -G spring
COPY --from=build /app/target/*.jar app.jar
USER spring
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=5s --retries=5 \
    CMD wget -qO- http://localhost:8080/v3/api-docs || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
