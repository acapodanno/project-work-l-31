"""Motore RAG per la knowledge base FAQ di HealthCare Plus.

Espone `build_query_engine(data_dir)`, che ritorna un oggetto con un metodo
`.query(str) -> Response` (dove `Response.response` è il testo della risposta).

Se è disponibile una `OPENAI_API_KEY`, viene costruito un vero indice
vettoriale con **LlamaIndex** (chunking dei documenti in `data/`, embedding
OpenAI, retrieval top-k). In assenza della chiave (o se l'inizializzazione
fallisce per qualunque motivo: rete, quota, dipendenze mancanti) si ricade in
modo trasparente su `MockQueryEngine`, un motore deterministico basato su
ricerca di sovrapposizione lessicale sul file `data/faq.txt`.

Questo garantisce che l'assistente sia sempre testabile end-to-end (demo,
CI, valutazione in `evaluation/`) anche senza credenziali a pagamento.
"""
import re
from pathlib import Path


class _Response:
    """Wrapper minimale che replica l'interfaccia `response.response` di
    LlamaIndex, così il resto del codice (tool LangChain, endpoint FastAPI)
    tratta in modo identico il motore reale e quello mock."""

    def __init__(self, text: str):
        self.response = text


class MockQueryEngine:
    """Motore RAG offline: legge blocchi `Q: ... / A: ...` da `faq.txt` e
    risponde restituendo la FAQ con il maggior numero di parole in comune
    con la domanda dell'utente."""

    def __init__(self, data_dir: str):
        self.data_dir = Path(data_dir)
        self.faqs: list[dict] = []
        self._load_faqs()

    def _load_faqs(self) -> None:
        faq_file = self.data_dir / "faq.txt"
        if not faq_file.exists():
            return
        content = faq_file.read_text(encoding="utf-8")
        blocks = re.split(r"\n\s*\n", content.strip())
        for block in blocks:
            q_match = re.search(r"Q:\s*(.+)", block)
            a_match = re.search(r"A:\s*(.+)", block, re.DOTALL)
            if q_match and a_match:
                self.faqs.append(
                    {
                        "question": q_match.group(1).strip(),
                        "answer": a_match.group(1).strip(),
                    }
                )

    def query(self, query_str: str) -> _Response:
        query_words = set(re.findall(r"\w+", query_str.lower()))
        best_match = None
        max_overlap = 0
        for faq in self.faqs:
            q_words = set(re.findall(r"\w+", faq["question"].lower()))
            overlap = len(query_words.intersection(q_words))
            if overlap > max_overlap:
                max_overlap = overlap
                best_match = faq
        if best_match and max_overlap > 0:
            return _Response(best_match["answer"])
        return _Response(
            "Mi dispiace, non ho trovato informazioni specifiche su questo "
            "argomento nella knowledge base. Ti consiglio di contattare la "
            "reception oppure di segnalarmi il problema così posso aprire "
            "un ticket di assistenza."
        )


def build_query_engine(data_dir: str, openai_api_key: str = ""):
    """Factory del motore RAG. Prova prima LlamaIndex (se è stata fornita
    una API key), altrimenti torna al motore mock offline."""
    if openai_api_key:
        try:
            from llama_index.core import Settings, SimpleDirectoryReader, VectorStoreIndex
            from llama_index.embeddings.openai import OpenAIEmbedding
            from llama_index.llms.openai import OpenAI as LlamaOpenAI

            Settings.llm = LlamaOpenAI(model="gpt-4o-mini", api_key=openai_api_key)
            Settings.embed_model = OpenAIEmbedding(api_key=openai_api_key)

            documents = SimpleDirectoryReader(input_dir=data_dir).load_data()
            index = VectorStoreIndex.from_documents(documents)
            return index.as_query_engine(similarity_top_k=3)
        except Exception as exc:  # pragma: no cover - dipende da servizi esterni
            print(
                f"[rag_engine] Impossibile inizializzare LlamaIndex ({exc}). "
                "Uso il motore mock offline."
            )
    return MockQueryEngine(data_dir)
