import os
import json
from typing import List, Dict, Any
from haystack import Document, Pipeline
from haystack.document_stores.in_memory import InMemoryDocumentStore
from haystack.components.builders import PromptBuilder
from haystack.components.generators import OpenAIGenerator
from haystack.utils import Secret

document_store = InMemoryDocumentStore()

def get_generator():
    api_key = os.environ.get("GROQ_API_KEY")
    if not api_key:
        return None
    return OpenAIGenerator(
        api_key=Secret.from_token(api_key),
        api_base_url="https://api.groq.com/openai/v1",
        model="meta-llama/llama-4-scout-17b-16e-instruct"
    )

def index_event(event_data: Dict[str, Any], analysis_result: Dict[str, Any]):
    full_event = {**event_data, **analysis_result}
    content = (
        f"Event ID: {full_event.get('event_id')}\n"
        f"Control ID: {full_event.get('control_id')}\n"
        f"Domain: {full_event.get('domain')}\n"
        f"System: {full_event.get('system')}\n"
        f"Environment: {full_event.get('environment')}\n"
        f"Severity: {full_event.get('severity')}\n"
        f"Parameter: {full_event.get('parameter')}\n"
        f"Change Source: {full_event.get('change_source')}\n"
        f"Changed By: {full_event.get('changed_by')}\n"
        f"Approval Status: {full_event.get('approval_status')}\n"
        f"Maintenance Window: {full_event.get('maintenance_window')}\n"
        f"Old Value: {full_event.get('old_value')}\n"
        f"New_Value: {full_event.get('new_value')}\n"
        f"Is Risky: {full_event.get('is_risky')}\n"
        f"Risk Score: {full_event.get('risk_score')}\n"
        f"MITRE ATT&CK: {full_event.get('mitre_technique')} ({full_event.get('mitre_name')})\n"
        f"Severity Delta: {full_event.get('severity_delta')}\n"
    )
    
    doc = Document(
        content=content,
        meta={
            "event_id": full_event.get("event_id"),
            "control_id": full_event.get("control_id"),
            "domain": full_event.get("domain"),
            "system": full_event.get("system"),
            "environment": full_event.get("environment")
        }
    )
    document_store.write_documents([doc])

def query_events(query_text: str) -> Dict[str, Any]:
    docs = document_store.filter_documents()
    if not docs:
        return {
            "query": query_text,
            "answer": "No drift events have been ingested yet.",
            "sources": []
        }
    
    context = "\n---\n".join([doc.content for doc in docs[:20]])
    
    template = """
    You are an expert Security Analyst. Answer the user's question using the context of ingested security control drift events.
    If the question asks about remediation, prioritize remediation sequencing based on risk and MITRE ATT&CK guidelines.

    Drift Events Context:
    {{context}}

    Question: {{query}}

    Answer:
    """
    
    generator = get_generator()
    if not generator:
        return {
            "query": query_text,
            "answer": "Groq API key not set. Ingested events are recorded, but RAG queries are disabled.",
            "sources": [d.meta for d in docs]
        }
        
    pipeline = Pipeline()
    pipeline.add_component("prompt_builder", PromptBuilder(template=template))
    pipeline.add_component("llm", generator)
    pipeline.connect("prompt_builder", "llm")
    
    results = pipeline.run({
        "prompt_builder": {
            "context": context,
            "query": query_text
        }
    })
    
    return {
        "query": query_text,
        "answer": results["llm"]["replies"][0],
        "sources": [d.meta for d in docs]
    }
