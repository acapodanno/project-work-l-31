# syntax=docker/dockerfile:1
#
# Dockerfile "all-in-one": un solo container con backend Spring Boot +
# frontend Angular incorporato come risorsa statica. Pensato per la
# consegna/demo del project work (avvio con un solo `docker run`), non
# per lo sviluppo quotidiano.
#
# Il pom.xml in root compila il frontend tramite frontend-maven-plugin
# (fase `generate-resources`, con la configuration Angular "embedded"
# definita in frontend/angular.json) e lo scrive in
# backend/src/main/resources/static; backend/pom.xml (modulo Maven
# indipendente e invariato) lo impacchetta poi nel jar. I due comandi vanno
# lanciati in questo ordine: NON sono un reactor Maven unico, perché senza
# una relazione di parent/dipendenza esplicita Maven non garantirebbe che
# root venga eseguito prima di backend (vedi commento in pom.xml).
#
# backend/pom.xml resta un modulo Maven indipendente e invariato: per lo
# sviluppo/CI con servizi separati (hot reload, Nginx, coverage indipendente)
# continuare a usare backend/Dockerfile + frontend/Dockerfile tramite
# docker-compose.yml.
#
# Uso:
#   docker build -t healthcare-app .
#   docker run --rm -p 8080:8080 healthcare-app

# ---------- Stage 1: build (Maven + Node/npm scaricati dal frontend-maven-plugin) ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache delle dipendenze Maven del backend (layer invalidato solo se cambia backend/pom.xml)
COPY backend/pom.xml backend/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -B -f backend/pom.xml dependency:go-offline

# pom.xml di root (frontend-maven-plugin) + sorgenti di frontend e backend
COPY pom.xml pom.xml
COPY frontend frontend
COPY backend/src backend/src

# 1) Compila l'Angular e lo scrive in backend/src/main/resources/static
RUN --mount=type=cache,target=/root/.m2 mvn -B -f pom.xml clean generate-resources

# 2) Compila e impacchetta il backend, che include lo static/ appena generato.
#    backend/pom.xml non fissa un <finalName> (resta invariato per non
#    impattare backend/Dockerfile e la CI): il jar si chiama
#    healthcare-backend-<version>.jar, e Spring Boot Maven Plugin genera
#    anche un jar "-plain" (libreria, non eseguibile) accanto ad esso. Lo
#    escludiamo esplicitamente e rinominiamo l'eseguibile in un nome fisso.
RUN --mount=type=cache,target=/root/.m2 mvn -B -f backend/pom.xml clean package -DskipTests && \
    cp $(ls backend/target/*.jar | grep -v -- '-plain\.jar$') backend/target/app.jar

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build /app/backend/target/app.jar app.jar
USER spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
