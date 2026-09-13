# 4. Organizzazione del Codice Sorgente e Repository Git

[⬅ Indice](./README.md) | [Avanti: Processo di Sviluppo ➡](./5-Processo_Sviluppo.md)

Il progetto è architettato come un "Monorepo" virtuale, contenente nella stessa root folder sia il progetto backend (Java/Spring) che quello frontend (Node/Angular). Questa scelta agevola lo sviluppo e riduce i conflitti di versionamento dell'intera piattaforma.

## Struttura delle Cartelle
La radice del progetto espone due sotto-progetti indipendenti:

```text
/project-work-l-31
 ├── backend/                   # Spring Boot REST API
 │    ├── src/main/java         # Classi Java (Controller, Entity, Service)
 │    ├── src/main/resources    # Configurazioni (application.properties)
 │    ├── pom.xml               # Dipendenze Maven
 │    └── .gitignore            # Ignora /target, file IDE e compilati
 │
 ├── frontend/                  # Angular SPA
 │    ├── src/app/              # Componenti, Servizi, Interceptors, Modelli
 │    ├── src/styles.css        # Variabili globali e classi (es. .glass-card)
 │    ├── package.json          # Dipendenze npm
 │    └── .gitignore            # Ignora /node_modules, /dist e IDE
 │
 ├── doc/                       # Documentazione del Progetto (questo report)
 └── .gitignore                 # Regole Globali (macOS, editor)
```

## Setup del Progetto per lo Sviluppo (Clonazione ed Esecuzione)

Per eseguire l'intero stack in locale, è sufficiente seguire questi passi:

1. **Clonare il Repository:**
   ```bash
   git clone <URL_DEL_TUO_REPOSITORY>
   cd project-work-l-31
   ```

2. **Avviare il Backend (Spring Boot 3):**
   - Assicurarsi di aver installato Java 21+ e Maven.
   - Aprire il terminale nella directory `backend/`:
     ```bash
     mvn spring-boot:run
     ```
   - L'API e lo Swagger si avvieranno sulla porta `8080`. Essendo basato su H2, il database in memoria si autoconfigurerà al volo ad ogni riavvio.

3. **Avviare il Frontend (Angular 19):**
   - Assicurarsi di aver installato Node.js (v20 o v22+).
   - Aprire il terminale nella directory `frontend/`:
     ```bash
     npm install
     npm run start
     ```
   - L'applicazione SPA si avvierà in modalità watch sulla porta `4200` (o `45694` in caso di setup personalizzati).

## Best Practices adottate su Git
Per ottimizzare lo spazio del repository e migliorarne le performance, sono stati generati e applicati file `.gitignore` mirati ad impedire il tracciamento di:
- `node_modules/` (Librerie Javascript molto pesanti)
- `target/` e `dist/` (Artefatti di build di Maven ed Angular)
- `.idea/` e `.vscode/` (Impostazioni private degli editor locali)
- File di sistema (es. `.DS_Store` tipico di macOS)

## Containerizzazione (Docker)

Ogni modulo ha un proprio `Dockerfile` multi-stage, con uno stage `test` dedicato al calcolo del code coverage (JaCoCo per il backend, Karma/Istanbul per il frontend) separato dallo stage `runtime` finale:

```text
backend/Dockerfile    # deps -> test (JaCoCo) -> build -> runtime (JRE Alpine, porta 8080)
frontend/Dockerfile   # deps -> test (Karma headless + coverage) -> build -> runtime (Nginx, porta 80)
```

`docker-compose.yml` orchestra i due servizi runtime con healthcheck e variabili d'ambiente per la comunicazione inter-container. `docker-compose.test.yml` esegue invece solo gli stage `test`. Il vecchio `Dockerfile` combinato in root è mantenuto solo per compatibilità, con un commento che rimanda ai due file dedicati.
