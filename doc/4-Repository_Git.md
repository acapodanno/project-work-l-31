# 4. Organizzazione del Codice Sorgente e Repository Git

[⬅ Indice](./README.md) | [Avanti: Processo di Sviluppo ➡](./5-Processo_Sviluppo.md)

Il progetto è organizzato come **monorepo**: nella stessa radice convivono il backend (Java/Spring), il frontend (Node/Angular), la documentazione e la configurazione di build/CI. Questa scelta mantiene allineati API e client e permette di far evolvere entrambi in un'unica pull request.

## Struttura delle Cartelle

```text
/project-work-l-31
 ├── backend/                      # Spring Boot REST API (modulo Maven indipendente)
 │    ├── src/main/java/…/healthcare/
 │    │    ├── controller/         # 9 controller REST
 │    │    ├── service/            # logica di dominio (+ strategy/ per le transizioni di stato)
 │    │    ├── repository/         # Spring Data JPA
 │    │    ├── entity/  dto/  mapper/
 │    │    ├── security/           # filtro JWT, SecurityConfig, OwnershipService, TOTP
 │    │    ├── exception/          # GlobalExceptionHandler (ProblemDetail)
 │    │    └── DataSeeder.java     # utenti/medici di prova all'avvio
 │    ├── src/main/resources/application.properties
 │    ├── src/test/java            # 34 classi di test (137 test)
 │    ├── pom.xml                  # dipendenze, JaCoCo, plugin di compilazione
 │    └── Dockerfile               # deps → test (JaCoCo) → build → runtime
 │
 ├── frontend/                     # Angular SPA
 │    ├── src/app/                 # components/, pages/, services/, guards/, interceptors/, models/, shared/
 │    ├── src/environments/        # backendUrl per sviluppo e produzione
 │    ├── src/styles.css           # design system: variabili CSS e classi condivise
 │    ├── karma.conf.js            # launcher ChromeHeadlessCI + report di coverage
 │    ├── nginx.conf               # serve la SPA con fallback su index.html
 │    ├── package.json  angular.json
 │    └── Dockerfile               # deps → test (Karma) → build → runtime (Nginx)
 │
 ├── doc/                          # questa relazione (9 capitoli) + img/ (screenshot reali)
 ├── .github/workflows/ci.yml      # pipeline CI: build → test → release per modulo
 ├── docker-compose.yml            # backend + frontend (Nginx) con healthcheck
 ├── docker-compose.test.yml       # esegue solo gli stage `test` dei due Dockerfile
 ├── Dockerfile                    # immagine "all-in-one" (backend con SPA incorporata)
 ├── pom.xml                       # pom di servizio: compila l'Angular dentro il backend
 ├── .dockerignore  .gitignore
 ├── README.md                     # panoramica breve
 └── REPORT.md                     # riassunto esecutivo
```

## Setup per lo Sviluppo (clonazione ed esecuzione)

**Prerequisiti:** JDK **21**, Maven 3.9+, Node.js 20 o 22 con npm. Con un JDK più vecchio (ad esempio 11) il backend non compila.

1. **Clonare il repository**
   ```bash
   git clone <URL_DEL_REPOSITORY>
   cd project-work-l-31
   ```

2. **Avviare il backend** (porta `8080`)
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   Il database H2 è in memoria: si ricrea a ogni avvio e il `DataSeeder` inserisce 2 pazienti, 5 medici e l'utente di supporto (credenziali nel [README](./README.md#avvio-rapido)). Sono disponibili anche `http://localhost:8080/swagger-ui.html` e la console H2 su `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:healthcaredb`, utente `sa`, password vuota).

3. **Avviare il frontend** (porta `4200`)
   ```bash
   cd frontend
   npm install
   npm start
   ```

4. **Configurazione.** Il backend legge la chiave di firma dei JWT dalla variabile d'ambiente `HEALTHCARE_JWT_SECRET` (con un valore di default di sviluppo). Il frontend punta al backend tramite `backendUrl` in `frontend/src/environments/environment*.ts` (`http://localhost:8080/api`). I PDF dei referti vengono scritti nella cartella `uploads/` relativa alla directory da cui parte il processo Java.

5. **Test**
   ```bash
   cd backend  && mvn test                                          # 137 test + report JaCoCo in target/site/jacoco
   cd frontend && npx ng test --no-watch --browsers=ChromeHeadlessCI --code-coverage
   ```
   La variabile `CHROME_BIN` deve puntare a Chrome/Chromium se non è nel percorso predefinito.

## Tre modi di eseguire l'applicazione

| Modalità | Comando | Cosa parte | Porte |
|---|---|---|---|
| **Sviluppo** | `mvn spring-boot:run` + `npm start` | Backend e dev server Angular separati, con hot reload | 8080 + 4200 |
| **Docker Compose** | `docker compose up --build` | Immagine backend (JRE Alpine) e immagine frontend (Nginx), con healthcheck; `frontend` dipende da `backend` | 8080 + 4200 (→ 80 nel container) |
| **Immagine unica** | `docker build -t healthcare-app .` e `docker run --rm -p 8080:8080 healthcare-app` | Un solo container: il Spring Boot serve anche la SPA incorporata in `static/` | 8080 |

Come funziona l'immagine unica: il `pom.xml` di radice usa **frontend-maven-plugin** per installare Node, eseguire `npm ci` e compilare Angular con la configurazione `production,embedded` (definita in `frontend/angular.json`), che scrive l'output in `backend/src/main/resources/static`. Poi `backend/pom.xml` impacchetta il jar includendo quei file. I due comandi Maven vanno eseguiti in quest'ordine (lo fa il `Dockerfile`): non sono un reactor unico perché `backend/pom.xml` deve restare autonomo per `backend/Dockerfile` e per la CI.

> **Nota di verifica.** Le modalità *Sviluppo* sono state eseguite e provate direttamente (screenshot e verifiche del [capitolo 6](./6-Test_Funzionali.md)). Le configurazioni Docker sono state rilette e allineate al codice, ma le immagini non sono state ricostruite durante questo aggiornamento della documentazione (Docker non era disponibile nell'ambiente). L'immagine unica non definisce un fallback per le rotte Angular profonde: un refresh su `/dashboard` va gestito con un controller di forward o una regola di rewrite, in alternativa si usa la modalità Compose con Nginx.

## Flusso di lavoro Git

- **Branch principale `main`**, aggiornato tramite **pull request** da branch tematici (`feature/…`, `fix/…`, `ci/…`, `ui/…`). Nella storia sono integrate 8 pull request (#21, #23, #24, #25, #26, #27, #29, #30). Nel repository remoto sono presenti anche branch generati da Dependabot per gli aggiornamenti di dipendenze.
- **Messaggi di commit** in stile *Conventional Commits* (`feat:`, `fix:`, `style:`, `ci:`, `test:`, `chore:`), con corpo esplicativo sulle modifiche più consistenti. Nelle fasi iniziali alcuni commit ravvicinati hanno messaggi generici (`add .`): è un limite di disciplina riconosciuto, corretto nelle iterazioni successive.
- **Pulizia del repository.** File non sorgente (dipendenze e ambienti locali) tracciati per errore, nell'ordine di migliaia, sono stati rimossi dal tracking e sono state aggiunte regole `.gitignore` per evitarne il ripetersi.

### File ignorati (`.gitignore`)

- `node_modules/`, `target/`, `dist/`, `coverage/` (dipendenze e artefatti di build);
- `backend/src/main/resources/static/` (output della SPA incorporata);
- `.idea/`, `.vscode/`, file di sistema (`.DS_Store`) e cache Python;
- `*.pdf`, `*.db`, `.env` (file pesanti o riservati, tra cui i referti caricati).

### Release automatiche

Ad ogni push su `main` che modifica un modulo, la CI pubblica una **GitHub Release** con tag `backend-v1.0.<run>` (jar) o `frontend-v1.0.<run>` (archivio zip del build), dove `<run>` è il numero di esecuzione del workflow. I dettagli sono nel [capitolo 5](./5-Processo_Sviluppo.md#ci-pipeline-e-automazione).

[Avanti: Processo di Sviluppo ➡](./5-Processo_Sviluppo.md)
