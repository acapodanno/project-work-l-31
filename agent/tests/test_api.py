import os

os.environ.setdefault("OPENAI_API_KEY", "")

from fastapi.testclient import TestClient

from src.api.main import app

client = TestClient(app)


def test_health_endpoint_reports_offline_mode_without_api_key():
    resp = client.get("/health")
    assert resp.status_code == 200
    body = resp.json()
    assert body["status"] == "ok"
    assert body["mode"] == "offline-mock"


def test_chat_endpoint_answers_faq_question():
    resp = client.post(
        "/api/chat", json={"message": "Quali sono gli orari di apertura?", "patientId": 1}
    )
    assert resp.status_code == 200
    assert "response" in resp.json()
    assert len(resp.json()["response"]) > 0


def test_chat_endpoint_rejects_malformed_payload():
    resp = client.post("/api/chat", json={"message": "ciao"})  # manca patientId
    assert resp.status_code == 422


def test_chat_endpoint_returns_502_when_agent_raises(monkeypatch):
    import src.api.main as main_module

    def boom(message, patient_id):
        raise RuntimeError("boom")

    monkeypatch.setattr(main_module.agent, "run", boom)

    resp = client.post("/api/chat", json={"message": "ciao", "patientId": 1})

    assert resp.status_code == 502
    assert "non è al momento disponibile" in resp.json()["detail"]


def test_cors_headers_present_for_frontend_origin():
    resp = client.get("/health", headers={"Origin": "http://localhost:4200"})
    assert resp.headers.get("access-control-allow-origin") == "*"
