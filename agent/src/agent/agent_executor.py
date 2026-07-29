"""Costruzione dell'agente conversazionale.

`build_agent(query_engine, openai_api_key)` ritorna un oggetto con un metodo
`.run(message, patient_id) -> str`:

- se è disponibile una `OPENAI_API_KEY`, costruisce un vero agente LangChain
  "tool calling" (modello OpenAI) che decide autonomamente se interrogare
  `rag_faq_tool` o `create_ticket_tool`;
- altrimenti (o se l'inizializzazione fallisce) usa `OfflineAgentExecutor`,
  un router deterministico a parole chiave che replica la stessa decisione
  senza bisogno di un LLM esterno. Questo mantiene l'assistente funzionante
  offline per demo, test automatici e valutazione (`evaluation/`).
"""
from .tools import create_ticket_tool, make_rag_tool

TICKET_KEYWORDS = [
    "ticket",
    "segnal",
    "problema",
    "errore",
    "guasto",
    "non funziona",
    "bug",
    "malfunzion",
]


class OfflineAgentExecutor:
    """Router deterministico usato quando non è disponibile una LLM online."""

    mode = "offline-mock"

    def __init__(self, rag_tool, ticket_tool):
        self.rag_tool = rag_tool
        self.ticket_tool = ticket_tool

    def run(self, message: str, patient_id: int) -> str:
        lowered = message.lower()
        if any(keyword in lowered for keyword in TICKET_KEYWORDS):
            title = message.strip()[:80]
            return self.ticket_tool.invoke(
                {"patient_id": patient_id, "title": title, "description": message}
            )
        return self.rag_tool.invoke({"question": message})


class OnlineAgentExecutor:
    """Wrapper sottile su un `AgentExecutor` LangChain reale."""

    mode = "langchain-openai"

    def __init__(self, executor):
        self._executor = executor

    def run(self, message: str, patient_id: int) -> str:
        result = self._executor.invoke(
            {"input": f"[patient_id={patient_id}] {message}"}
        )
        return result["output"] if isinstance(result, dict) else str(result)


def build_agent(query_engine, openai_api_key: str = ""):
    rag_tool = make_rag_tool(query_engine)

    if openai_api_key:
        try:
            from langchain.agents import AgentExecutor, create_tool_calling_agent
            from langchain_core.prompts import ChatPromptTemplate
            from langchain_openai import ChatOpenAI

            llm = ChatOpenAI(model="gpt-4o-mini", api_key=openai_api_key, temperature=0)
            tools = [rag_tool, create_ticket_tool]
            prompt = ChatPromptTemplate.from_messages(
                [
                    (
                        "system",
                        "Sei l'assistente virtuale di HealthCare Plus. Usa "
                        "rag_faq_tool per rispondere a domande informative "
                        "(orari, specializzazioni, convenzioni) e "
                        "create_ticket_tool quando il paziente segnala un "
                        "problema tecnico da risolvere.",
                    ),
                    ("human", "{input}"),
                    ("placeholder", "{agent_scratchpad}"),
                ]
            )
            agent = create_tool_calling_agent(llm, tools, prompt)
            executor = AgentExecutor(agent=agent, tools=tools)
            return OnlineAgentExecutor(executor)
        except Exception as exc:  # pragma: no cover - dipende da servizi esterni
            print(
                f"[agent_executor] Impossibile inizializzare l'agente "
                f"LangChain online ({exc}). Uso il router offline."
            )

    return OfflineAgentExecutor(rag_tool, create_ticket_tool)
