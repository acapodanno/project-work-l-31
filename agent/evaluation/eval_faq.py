#!/usr/bin/env python3
"""Valutazione leggera del motore RAG sulla knowledge base FAQ.

A differenza di suite complete come Ragas/DeepEval (che richiedono un LLM
"giudice" e chiavi API a pagamento), questo script misura una metrica
semplice ma oggettiva: per ogni domanda del dataset di valutazione, verifica
che la risposta del motore RAG contenga le parole chiave attese
(`expected_keywords`). Funziona quindi anche in modalità offline/mock,
rendendolo eseguibile in CI senza credenziali esterne.

Uso:
    cd agent
    python3 evaluation/eval_faq.py
"""
import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from src.config import settings
from src.rag.rag_engine import build_query_engine


def main() -> int:
    dataset_path = Path(__file__).parent / "test_dataset_faq.json"
    dataset = json.loads(dataset_path.read_text(encoding="utf-8"))

    engine = build_query_engine(settings.data_dir, settings.openai_api_key)

    passed = 0
    for case in dataset:
        response = engine.query(case["question"]).response.lower()
        matched = [kw for kw in case["expected_keywords"] if kw.lower() in response]
        ok = len(matched) == len(case["expected_keywords"])
        passed += int(ok)
        status = "OK " if ok else "FAIL"
        print(f"[{status}] {case['question']!r} -> parole attese trovate: {matched}")

    total = len(dataset)
    accuracy = passed / total if total else 0.0
    print(f"\nAccuracy: {passed}/{total} ({accuracy:.0%})")
    return 0 if passed == total else 1


if __name__ == "__main__":
    raise SystemExit(main())
