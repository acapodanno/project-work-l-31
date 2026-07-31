"""Applicazione FastAPI dell'agente AI RAG.

Espone:
- `GET /health`   -> stato del servizio e modalità attiva (online/offline).
- `POST /api/chat` -> endpoint conversazionale usato dal frontend Angular
  (`agent.service.ts`), payload `{message, patientId}` -> `{response}`.
"""
import logging

from fastapi import Depends, FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

from ..config import settings
from ..rag.rag_engine import build_query_engine
from .agent_dependencies import get_agent
from .auth import get_current_user

logger = logging.getLogger(__name__)

app = FastAPI(
    title="HealthCare Plus - AI Agent",
    description="Assistente virtuale RAG con apertura automatica di ticket di assistenza.",
    version="1.0.0",
)

# Consente le chiamate cross-origin dal frontend Angular (dev: localhost:4200).
# Coerente con il CORS "*" già usato dal backend Spring Boot per questo progetto.
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

query_engine = build_query_engine(settings.data_dir, settings.openai_api_key)
agent = get_agent(query_engine, settings.openai_api_key)


class ChatRequest(BaseModel):
    message: str
    patientId: int


class ChatResponse(BaseModel):
    response: str


@app.get("/health")
def health():
    return {"status": "ok", "mode": agent.mode}


@app.post("/api/chat", response_model=ChatResponse)
def chat(payload: ChatRequest, current_user: str = Depends(get_current_user)):
    try:
        text = agent.run(payload.message, payload.patientId)
    except Exception:
        logger.exception("Errore durante l'esecuzione dell'agente AI")
        raise HTTPException(
            status_code=502,
            detail="L'assistente AI non è al momento disponibile. Riprova tra qualche istante.",
        )
    return ChatResponse(response=text)
