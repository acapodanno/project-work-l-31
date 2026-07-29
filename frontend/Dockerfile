# syntax=docker/dockerfile:1
#
# Build multi-stage per il frontend Angular.
#
# Target disponibili:
#   deps    -> dipendenze npm installate (non usato direttamente)
#   test    -> esegue `ng test` headless con coverage (Karma + Istanbul), non produce un'immagine da eseguire
#   build   -> build di produzione Angular
#   runtime -> immagine Nginx che serve i file statici (target di default)
#
# Esempi:
#   docker build -t healthcare-frontend .
#   docker build --target test -t healthcare-frontend:test .
#   DOCKER_BUILDKIT=1 docker build --target test \
#       --output type=local,dest=./coverage/frontend .          # estrae il report di coverage su host

# ---------- Stage 1: dipendenze ----------
FROM node:20-alpine AS deps
WORKDIR /app
COPY package*.json ./
RUN npm ci

# ---------- Stage 2: test + code coverage (Karma/Istanbul) ----------
FROM deps AS test
# Chrome headless richiede un browser reale: su Alpine si installa Chromium
# via apk (nessuna connessione a servizi esterni di download binari).
RUN apk add --no-cache chromium
ENV CHROME_BIN=/usr/bin/chromium-browser
COPY . .
# Report HTML/lcov in /app/coverage/healthcare-frontend
RUN npx ng test --no-watch --no-progress --browsers=ChromeHeadlessCI --code-coverage

# ---------- Stage 3: build di produzione ----------
FROM deps AS build
COPY . .
RUN npm run build

# ---------- Stage 4: runtime (Nginx) ----------
FROM nginx:1.27-alpine AS runtime
COPY --from=build /app/dist/healthcare-frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
HEALTHCHECK --interval=10s --timeout=5s --retries=5 \
    CMD wget -qO- http://localhost:80 || exit 1
