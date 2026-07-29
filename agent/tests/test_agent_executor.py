from unittest.mock import Mock, patch

from src.agent.agent_executor import OfflineAgentExecutor, build_agent
from src.rag.rag_engine import MockQueryEngine


def _engine(tmp_path):
    (tmp_path / "faq.txt").write_text(
        "Q: Quali sono gli orari?\nA: Aperti dal lunedì al venerdì.",
        encoding="utf-8",
    )
    return MockQueryEngine(str(tmp_path))


def test_build_agent_without_api_key_returns_offline_executor(tmp_path):
    agent = build_agent(_engine(tmp_path), openai_api_key="")
    assert isinstance(agent, OfflineAgentExecutor)
    assert agent.mode == "offline-mock"


def test_offline_agent_routes_faq_question_to_rag(tmp_path):
    agent = build_agent(_engine(tmp_path), openai_api_key="")
    response = agent.run("Quali sono i vostri orari?", patient_id=1)
    assert "lunedì" in response


def test_offline_agent_routes_problem_report_to_ticket_tool(tmp_path):
    agent = build_agent(_engine(tmp_path), openai_api_key="")
    with patch("src.agent.tools.requests.post") as mock_post:
        mock_post.return_value = Mock(status_code=201, json=lambda: {"id": 7})
        response = agent.run(
            "Ho un errore nel caricamento dei referti, potete aprire un ticket?",
            patient_id=3,
        )
    assert "7" in response
    assert mock_post.call_args.kwargs["json"]["patientId"] == 3
