"""Applicazione FastAPI dell'agente AI RAG.

Espone:
- `GET /health`   -> stato del servizio e modalità attiva (online/offline).
- `POST /api/chat` -> endpoint conversazionale usato dal frontend Angular
  (`agent.service.ts`), payload `{message, patientId}` -> `{response}`.
"""
from fastapi import FastAPI
from pydantic import BaseModel

from ..config import settings
from ..rag.rag_engine import build_query_engine
from .agent_dependencies import get_agent

app = FastAPI(
    title="HealthCare Plus - AI Agent",
    description="Assistente virtuale RAG con apertura automatica di ticket di assistenza.",
    version="1.0.0",
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
def chat(payload: ChatRequest):
    text = agent.run(payload.message, payload.patientId)
    return ChatResponse(response=text)
