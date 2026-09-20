# 4. Organizzazione del Codice Sorgente e Repository Git

[⬅ Indice](./README.md) | [Avanti: Processo di Sviluppo ➡](./5-Processo_Sviluppo.md)

Il progetto è organizzato come **monorepo**: nella stessa radice convivono il backend (Java/Spring), il frontend (Node/Angular), la documentazione e la configurazione di build/CI. Questa scelta mantiene allineati API e client e permette di far evolvere entrambi in un'unica pull request.

**Repository Git:** [https://github.com/acapodanno/project-work-l-31](https://github.com/acapodanno/project-work-l-31) (branch `main`).

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
 ├── .gitignore
 └── README.md                     # panoramica breve
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

## Due modi di eseguire l'applicazione

| Modalità | Comando | Cosa parte | Porte |
|---|---|---|---|
| **Sviluppo** | `mvn spring-boot:run` + `npm start` | Backend e dev server Angular separati, con hot reload | 8080 + 4200 |
| **Docker Compose** | `docker compose up --build` | Immagine backend (JRE 21 Alpine) e immagine frontend (Nginx 1.27 Alpine con la SPA compilata), con controlli di salute; `frontend` parte dopo `backend` | 8080 + 4200 (→ 80 nel container) |

### Avvio con Docker Compose

Dalla radice del repository, con Docker in esecuzione:

```bash
docker compose up --build
```

Al termine della costruzione delle immagini il frontend è su `http://localhost:4200`, il backend su `http://localhost:8080` (Swagger su `/swagger-ui.html`). Per fermare tutto: `Ctrl+C` e poi `docker compose down`. Le schermate dell'avvio e dei due container attivi sono nel [capitolo 6, §2.6](./6-Test_Funzionali.md#26-esecuzione-in-container-docker-compose).

Il test dei due moduli in container resta disponibile come stage dedicato dei Dockerfile, senza avviare nulla in ascolto:

```bash
docker build --target test ./backend      # JUnit + JaCoCo
docker build --target test ./frontend     # Karma headless + coverage
```

> **Verifica.** La modalità *Sviluppo* e l'avvio con Docker Compose sono stati eseguiti realmente (screenshot e verifiche del [capitolo 6](./6-Test_Funzionali.md)). Provando Compose sono emersi e sono stati corretti due difetti dei Dockerfile, descritti nel capitolo 6 (§4): il backend non partiva per un problema di permessi sulla cartella `uploads/` e il controllo di salute del frontend risultava sempre `unhealthy`. I referti caricati nel container sono nella cartella `/app/uploads` **interna al container** e si perdono quando lo si ricrea: per conservarli basterebbe un volume su quella cartella.

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
