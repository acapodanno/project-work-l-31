"""Piccolo modulo separato per costruire l'agente, cosi' i test possono
importarlo/mockarlo senza dover istanziare l'intera app FastAPI."""
from ..agent.agent_executor import build_agent


def get_agent(query_engine, openai_api_key: str = ""):
    return build_agent(query_engine, openai_api_key)
