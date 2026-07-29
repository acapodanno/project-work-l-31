# Agent AI RAG — HealthCare Plus

Assistente virtuale conversazionale, integrato nel frontend Angular (scheda
"Assistente AI RAG"), che risponde alle domande frequenti dei pazienti
consultando una knowledge base aziendale tramite **RAG (Retrieval-Augmented
Generation)** e, quando rileva la segnalazione di un problema tecnico, apre
automaticamente un ticket di assistenza sul backend Spring Boot.

## Stack tecnico

| Livello | Tecnologia |
|---|---|
| API | FastAPI + Uvicorn |
| Orchestrazione agente / tool calling | LangChain |
| Motore RAG | LlamaIndex (con fallback offline deterministico) |
| Comunicazione col backend | `requests` verso `POST /api/tickets` (Spring Boot) |

## Struttura

```
agent/
├── main.py                    # Entry point (uvicorn su porta 5000)
├── requirements.txt
├── .env.example
├── data/
│   └── faq.txt                # Knowledge base FAQ (formato Q: / A:)
├── src/
│   ├── config.py              # Caricamento configurazione da .env
│   ├── rag/
│   │   └── rag_engine.py      # LlamaIndex + MockQueryEngine offline
│   ├── agent/
│   │   ├── tools.py           # create_ticket_tool, rag_faq_tool
│   │   └── agent_executor.py  # Agente LangChain (online) / router offline
│   └── api/
│       └── main.py            # App FastAPI (/health, /api/chat)
├── tests/                     # Test unitari (pytest)
└── evaluation/                # Valutazione della qualità delle risposte RAG
```

## Modalità online vs offline

Se in `.env` è presente una `OPENAI_API_KEY`, l'agente costruisce un vero
indice vettoriale LlamaIndex sui documenti in `data/` e un agente LangChain
capace di scegliere autonomamente tra i due tool disponibili.

Se la chiave non è presente (o l'inizializzazione fallisce per qualunque
motivo: rete, quota, dipendenze), l'agente ricade automaticamente in
modalità **offline/mock**: un motore RAG deterministico basato su
sovrapposizione lessicale (`MockQueryEngine`) e un router a parole chiave
(`OfflineAgentExecutor`) che decide se rispondere con la FAQ o aprire un
ticket. Questo rende l'intero sistema testabile end-to-end senza costi e
senza connessione a servizi esterni — utile per demo, test automatici e CI.
L'endpoint `GET /health` riporta sempre la modalità attiva (`"mode"`).

## Avvio

```bash
cd agent
python3 -m venv venv
source venv/bin/activate        # Windows: venv\Scripts\activate
pip install -r requirements.txt
cp .env.example .env            # opzionale: inserire OPENAI_API_KEY

python3 main.py                 # oppure: uvicorn src.api.main:app --reload --port 5000
```

Il servizio sarà disponibile su `http://localhost:5000` (`/health`, `/api/chat`).

> Nota: la cartella `venv/` presente in precedenza nel repository era vuota
> (creata ma senza dipendenze installate). Rigenerarla con i comandi sopra.

## Test

```bash
cd agent
pip install -r requirements.txt
pytest -v
```

I test coprono: parsing/matching della knowledge base (`test_rag_engine.py`),
il tool di creazione ticket incluso il caso di backend non raggiungibile
(`test_tools.py`), il routing offline domanda/ticket (`test_agent_executor.py`)
e gli endpoint FastAPI (`test_api.py`).

## Valutazione della qualità RAG

```bash
cd agent
python3 evaluation/eval_faq.py
```

Misura, per un set di domande di test (`evaluation/test_dataset_faq.json`),
se la risposta del motore RAG contiene le parole chiave attese. A differenza
di suite complete come Ragas/DeepEval (che richiedono un LLM "giudice" e
credenziali a pagamento), questa metrica è oggettiva e funziona anche in
modalità offline — sul motore mock ottiene attualmente 8/8 (100%).
