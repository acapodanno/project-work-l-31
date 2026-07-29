# 9. Valutazione dei Risultati

[⬅ Indice](./README.md) | [Indietro: Risorse Utilizzate ⬅](./8-Risorse_Utilizzate.md)

Questa sezione analizza in modo critico potenzialità e limiti del prodotto realizzato, così come effettivamente verificato nel codice e nei test, non solo dichiarato in fase di progettazione.

## Punti di forza

**Architettura a doppia modalità (online/offline) su due livelli indipendenti.** Sia il motore RAG (LlamaIndex ↔ `MockQueryEngine`) sia l'agente conversazionale (LangChain `AgentExecutor` ↔ `OfflineAgentExecutor`) espongono la stessa interfaccia indipendentemente dal fatto che una `OPENAI_API_KEY` sia disponibile. Il vantaggio non è solo economico (nessun costo per demo/valutazione): è **testabilità**. L'intera suite `agent/tests/` e lo script `agent/evaluation/eval_faq.py` sono stati eseguiti realmente in fase di sviluppo (100% di accuratezza sul dataset di valutazione FAQ) proprio perché non dipendono da una chiave API esterna.

**Applicazione consapevole di pattern OOP oltre il CRUD di base.** Il backend usa lo Strategy pattern per le transizioni di stato (`AppointmentStatusStrategy`, `TicketStatusStrategy` con relative *Factory*), evitando catene di `if/else` sullo stato e rispettando l'Open/Closed Principle: aggiungere un nuovo stato non richiede modificare il codice esistente, solo aggiungere una nuova strategia.

**Sicurezza a più livelli, non solo autenticazione di base.** JWT stateless (scalabilità orizzontale, nessuna sessione server-side) combinato con 2FA via TOTP (RFC 6238) e RBAC per ruolo (Paziente/Medico/Supporto) va oltre il requisito minimo della traccia, che chiedeva "un backend RESTful" senza specificare vincoli di sicurezza.

**Copertura di test misurabile, non dichiarata.** Il backend usa JaCoCo per una metrica oggettiva di copertura; l'agente ha ora 4 file di test (`test_rag_engine.py`, `test_tools.py`, `test_agent_executor.py`, `test_api.py`) che coprono esplicitamente anche il caso di errore (backend irraggiungibile durante la creazione di un ticket).

## Limiti e criticità

**Persistenza dei dati non garantita (H2 in-memory).** Come dichiarato nello stesso `REPORT.md`, il database H2 "si autoconfigura al volo ad ogni riavvio": ogni riavvio del backend azzera pazienti, appuntamenti, referti e ticket. Questa è una scelta corretta per abbassare l'attrito di valutazione (nessun setup di database richiesto), ma è un limite reale che precluderebbe un uso in produzione senza migrazione a un database persistente (PostgreSQL/MySQL) — passaggio peraltro reso relativamente semplice dall'uso di Spring Data JPA, che disaccoppia la logica applicativa dal database concreto.

**`DataExtractionAgent` è una simulazione dichiarata, non estrazione AI reale.** La classe genera valori ematici casuali (`Random.nextInt`) etichettati come output di un "AI Medical Agent v1.0"; il commento nel codice sorgente lo definisce esplicitamente come simulazione ("in a real-world scenario, this would call Tesseract OCR or an AI API like OpenAI Vision"). È corretto presentarlo in relazione come *proof-of-concept dell'interfaccia* (upload referto → risposta strutturata) e non come funzionalità di estrazione dati realmente basata su AI, per evitare di sovrastimare le capacità del sistema.

**Il percorso "online" dell'agente (LangChain + LlamaIndex + OpenAI) è verificato via code review, non tramite esecuzione con chiave API reale.** Il codice segue i pattern documentati delle rispettive librerie ed è stato validato a livello di sintassi e logica, ma non è stato eseguito end-to-end con una `OPENAI_API_KEY` valida in questa fase (l'ambiente di sviluppo non aveva accesso a credenziali OpenAI). Il percorso offline, al contrario, è stato eseguito realmente e supera tutti i test. Prima della consegna finale è consigliabile un test manuale con una chiave reale per validare il comportamento dell'agente online.

**Il Dockerfile containerizza solo il backend.** Lo stage finale espone esclusivamente il jar Spring Boot su porta 8080; il build Angular (stage intermedio) non viene copiato in un'immagine servita (es. Nginx), quindi il frontend containerizzato non è servito staticamente. Per un deployment realmente "a container" servirebbe un terzo stage Nginx o un reverse proxy davanti a entrambi i servizi (backend + agente Python).

**Continuous Deployment non implementato.** La pipeline CI (`.github/workflows/ci.yml`) copre build e test di backend, agente e frontend, ma non un deploy automatico verso un ambiente di staging/produzione, in assenza di un'infrastruttura di hosting dedicata per un progetto didattico. Estensione naturale: deploy containerizzato su un servizio PaaS (Render, Railway, Azure Container Apps) attivato al merge su `main`.

## Sintesi

Il prodotto rispetta ed estende le richieste essenziali della traccia (backend RESTful + frontend + servizio significativo per il settore sanitario), aggiungendo sicurezza avanzata e un assistente AI RAG con architettura dual-mode ben progettata. I limiti individuati sono per lo più conseguenze consapevoli di scelte di scope appropriate a un progetto universitario (DB in-memory, mock di estrazione dati, assenza di CD) piuttosto che difetti di progettazione, e sono qui dichiarati esplicitamente proprio per dimostrare consapevolezza critica del proprio lavoro.
