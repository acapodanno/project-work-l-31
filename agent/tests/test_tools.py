from unittest.mock import Mock, patch

import requests

from src.agent.tools import create_ticket_tool


def test_create_ticket_success():
    with patch("src.agent.tools.requests.post") as mock_post:
        mock_post.return_value = Mock(status_code=201, json=lambda: {"id": 42})
        result = create_ticket_tool.invoke(
            {
                "patient_id": 1,
                "title": "Errore login",
                "description": "Non riesco ad accedere al portale",
            }
        )
        assert "42" in result
        assert mock_post.call_args.kwargs["json"]["patientId"] == 1


def test_create_ticket_backend_unreachable_falls_back_to_simulated():
    with patch(
        "src.agent.tools.requests.post",
        side_effect=requests.exceptions.ConnectionError("backend down"),
    ):
        result = create_ticket_tool.invoke(
            {
                "patient_id": 1,
                "title": "Errore login",
                "description": "Non riesco ad accedere al portale",
            }
        )
        assert "SIMULATO" in result


def test_create_ticket_backend_error_status():
    with patch("src.agent.tools.requests.post") as mock_post:
        mock_post.return_value = Mock(status_code=500, json=lambda: {})
        result = create_ticket_tool.invoke(
            {"patient_id": 1, "title": "Errore", "description": "desc"}
        )
        assert "500" in result
