"""Entrypoint dell'agente AI RAG di HealthCare Plus.

Avvio:
    python3 main.py
oppure, in modo equivalente:
    uvicorn src.api.main:app --reload --port 5000
"""
import uvicorn

if __name__ == "__main__":
    uvicorn.run("src.api.main:app", host="0.0.0.0", port=5000, reload=False)
