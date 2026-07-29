"""Configurazione centrale dell'agente AI.

Carica le variabili d'ambiente da `.env` (se presente) e le espone tramite
un semplice oggetto `settings`, così i moduli non devono leggere `os.environ`
direttamente in più punti.
"""
import os
from dataclasses import dataclass
from pathlib import Path

from dotenv import load_dotenv

load_dotenv()

BASE_DIR = Path(__file__).resolve().parent.parent


@dataclass
class Settings:
    data_dir: str = os.getenv("DATA_DIR", str(BASE_DIR / "data"))
    backend_url: str = os.getenv("BACKEND_URL", "http://localhost:8080/api")
    openai_api_key: str = os.getenv("OPENAI_API_KEY", "")

    @property
    def has_openai_key(self) -> bool:
        return bool(self.openai_api_key)


settings = Settings()
