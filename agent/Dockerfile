# syntax=docker/dockerfile:1
#
# Build multi-stage per l'agente AI (FastAPI + LangChain + LlamaIndex).
#
# Target disponibili:
#   deps    -> dipendenze Python installate (non usato direttamente)
#   test    -> esegue `pytest --cov`, non produce un'immagine da eseguire
#   runtime -> immagine finale che avvia il servizio (target di default)
#
# Esempi:
#   docker build -t healthcare-agent .
#   docker build --target test -t healthcare-agent:test .
#   DOCKER_BUILDKIT=1 docker build --target test \
#       --output type=local,dest=./coverage/agent .          # estrae i report di coverage su host

# ---------- Stage 1: dipendenze ----------
FROM python:3.11-slim AS deps
WORKDIR /app
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1
COPY requirements.txt .
RUN pip install -r requirements.txt

# ---------- Stage 2: test + code coverage (pytest-cov) ----------
FROM deps AS test
COPY . .
ENV OPENAI_API_KEY=""
# Report terminale + XML (per tool esterni) + HTML in /app/htmlcov
RUN pytest --cov=src --cov-report=term-missing --cov-report=xml --cov-report=html
RUN python3 evaluation/eval_faq.py

# ---------- Stage 3: runtime ----------
FROM deps AS runtime
WORKDIR /app
RUN useradd --create-home appuser
COPY . .
USER appuser
EXPOSE 5000
HEALTHCHECK --interval=10s --timeout=5s --retries=5 \
    CMD python3 -c "import urllib.request; urllib.request.urlopen('http://localhost:5000/health')" || exit 1
CMD ["python3", "main.py"]
