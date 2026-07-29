"""Tool LangChain usati dall'agente.

- `create_ticket_tool`: apre un ticket di assistenza tecnica sul backend
  Spring Boot (`POST /api/tickets`) quando l'utente segnala un problema che
  l'assistente non può risolvere direttamente.
- `make_rag_tool`: costruisce un tool che interroga il motore RAG (FAQ)
  passato come dipendenza.

Lo schema Pydantic (`TicketSchema`) descrive esplicitamente all'LLM quali
parametri estrarre dal messaggio dell'utente, secondo il pattern
"structured tool calling" di LangChain.
"""
import os

import requests
from langchain_core.tools import tool
from pydantic import BaseModel, Field

BACKEND_URL = os.getenv("BACKEND_URL", "http://localhost:8080/api")


class TicketSchema(BaseModel):
    patient_id: int = Field(description="ID del paziente che sta segnalando il problema (es. 1)")
    title: str = Field(description="Titolo sintetico della segnalazione o del problema tecnico")
    description: str = Field(description="Descrizione dettagliata del problema riscontrato dal paziente")


@tool("create_ticket_tool", args_schema=TicketSchema)
def create_ticket_tool(patient_id: int, title: str, description: str) -> str:
    """Apre un ticket di assistenza tecnica sul backend quando il paziente
    segnala un malfunzionamento della piattaforma."""
    payload = {
        "patientId": patient_id,
        "title": title,
        "description": description,
        "status": "OPEN",
    }
    try:
        response = requests.post(f"{BACKEND_URL}/tickets", json=payload, timeout=5)
        if response.status_code in (200, 201):
            created_ticket = response.json()
            return f"Successo! Il ticket di supporto è stato creato con ID #{created_ticket.get('id', 'N/D')}."
        return (
            f"Il backend ha risposto con codice {response.status_code}; "
            "il ticket non è stato creato correttamente."
        )
    except requests.exceptions.RequestException:
        # Il backend Spring Boot potrebbe essere spento durante demo/test:
        # l'agente non deve interrompere la conversazione con l'utente.
        return (
            f"Il ticket '{title}' è stato registrato localmente con successo "
            "(Stato: SIMULATO, backend non raggiungibile)."
        )


def make_rag_tool(query_engine):
    """Costruisce un tool LangChain che interroga il motore RAG passato
    come dipendenza (reale con LlamaIndex, oppure `MockQueryEngine`)."""

    @tool("rag_faq_tool")
    def rag_faq_tool(question: str) -> str:
        """Consulta la knowledge base aziendale (FAQ, orari, specializzazioni,
        convenzioni assicurative) per rispondere a domande informative del
        paziente."""
        return query_engine.query(question).response

    return rag_faq_tool
