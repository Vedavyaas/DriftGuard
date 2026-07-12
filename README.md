# DriftGuard

Security control drift detection and incident management for cloud-native infrastructure.

DriftGuard ingests configuration change events from cloud providers — either as structured JSON or free-form log text — routes them through Apache Kafka, classifies each change using a trained PyTorch model, correlates related events into compound incidents using graph analysis, and surfaces everything through a role-based React dashboard. A RAG layer backed by Groq LLM (Llama 4) allows analysts to query historical incidents in natural language.

---

## Features

- Accepts structured JSON events and unstructured log text from the same endpoint
- NLP preprocessing via Apache OpenNLP to extract domain, severity, and actor from raw text
- PyTorch binary classifier trained on labeled drift events with MITRE ATT&CK mapping
- Graph-based compound incident correlation (NetworkX) across cloud, network, identity, and endpoint domains
- Blast radius scoring and compliance violation mapping (NIST, CIS, GDPR)
- RAG query interface using Haystack and Groq Llama 4 for natural language incident analysis
- LLM-generated analyst narratives and remediation steps per compound incident
- Real-time event buffering with threshold-triggered batch correlation
- Scheduled sweeper for unread critical incidents with Kafka-driven SMTP email alerts
- Role-based access: Admin manages users; Project Manager manages projects and incidents
- Per-project health score updated on every incident state change
- JWT (RS256) authentication with stateless sessions across all services
- Spring Cloud Gateway routing and Eureka service discovery

---
