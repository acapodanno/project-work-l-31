from src.rag.rag_engine import MockQueryEngine


def _write_faq(tmp_path):
    (tmp_path / "faq.txt").write_text(
        "Q: Quali sono gli orari di apertura?\n"
        "A: Aperti dal lunedì al venerdì dalle 08:00 alle 20:00.\n"
        "\n"
        "Q: Quali specializzazioni offrite?\n"
        "A: Cardiologia, Dermatologia e Pediatria.",
        encoding="utf-8",
    )


def test_faq_loading(tmp_path):
    _write_faq(tmp_path)
    engine = MockQueryEngine(str(tmp_path))
    assert len(engine.faqs) == 2


def test_faq_best_match(tmp_path):
    _write_faq(tmp_path)
    engine = MockQueryEngine(str(tmp_path))
    result = engine.query("Quali sono i vostri orari di apertura?")
    assert "lunedì" in result.response


def test_faq_no_match_returns_fallback_message(tmp_path):
    _write_faq(tmp_path)
    engine = MockQueryEngine(str(tmp_path))
    result = engine.query("xilofono marmellata trattore")
    assert "non ho trovato" in result.response


def test_missing_faq_file_does_not_crash(tmp_path):
    engine = MockQueryEngine(str(tmp_path))
    assert engine.faqs == []
    assert "non ho trovato" in engine.query("orari?").response
