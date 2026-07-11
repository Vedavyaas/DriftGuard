# DriftGuard — Architecture Documentation & AI/ML Approach

## Overview

DriftGuard is a security control drift detection and governance platform. It continuously monitors configuration changes across enterprise security controls, classifies each change using machine learning, correlates related drift events into compound incidents, and surfaces risk posture intelligence through an analyst dashboard.

---

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        External Event Sources                        │
│   CI/CD Pipelines │ Cloud Watch │ Endpoint Agents │ SIEM Forwarders  │
└───────────────────────────────┬─────────────────────────────────────┘
                                │  JSON drift event
                                ▼
                    ┌───────────────────────┐
                    │   Apache Kafka         │
                    │   Topic: drift-events  │
                    └───────────┬───────────┘
                                │ @KafkaListener
                                ▼
┌───────────────────────────────────────────────────────────────────────┐
│                    Spring Boot Ingestor  (port 9002)                   │
│                                                                        │
│  DriftIngestionService                                                 │
│   1. Parse & validate projectHash  (PostgreSQL)                        │
│   2. Call Python ML service  POST /analyze/event                       │
│   3. Persist DriftEventNode → AFFECTS → ControlNode  (Neo4j)          │
│   4. Auto-escalate project status to AT_RISK if production + risky     │
└──────────────┬────────────────────────────────────┬───────────────────┘
               │ REST (RestTemplate)                 │ Spring Data JPA
               ▼                                     ▼
┌──────────────────────────┐          ┌──────────────────────────────┐
│  Python ML Service        │          │  PostgreSQL (port 5432)       │
│  FastAPI  (port 9003)     │          │  • ProjectManagerEntity       │
│                           │          │  • projectHash index          │
│  drift_detector.py        │          └──────────────────────────────┘
│  compound_correlator.py   │
│  api.py                   │          ┌──────────────────────────────┐
└──────────────────────────┘          │  Neo4j (port 7687)            │
                                       │  • DriftEventNode             │
                                       │  • ControlNode                │
                                       │  • ActorNode                  │
                                       │  • ComplianceFrameworkNode    │
                                       └──────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                API Gateway  (port 9000)                              │
│   Routes /AUTHENTICATION/* → Auth Service (port 9001)               │
│   Routes /INGESTOR/*       → Ingestor Service (port 9002)           │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ JWT-authenticated requests
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                React Frontend  (port 5173)                           │
│   Login → ProjectManagerDashboard                                    │
│            └── Drift Intelligence Tab                                │
│                 ├── Control Health Heatmap (by domain)               │
│                 ├── Drift Timeline (last 30 days)                    │
│                 ├── Compound Incident List                           │
│                 ├── Compliance Leaderboard                           │
│                 └── Executive Report                                 │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Java Microservices

| Service | Port | Role |
|---|---|---|
| **DiscoveryServer** | 8761 | Netflix Eureka — service registry |
| **Authentication** | 9001 | JWT issuance (RS256), ADMIN + PROJECTMANAGER roles |
| **Gateway** | 9000 | Spring Cloud Gateway — JWT validation + dynamic routing |
| **Ingestor** | 9002 | Project management, Kafka consumer, Neo4j writer |

### Ingestor Data Flow
1. Kafka consumer deserializes `drift-events` JSON → `DriftEventRequest`
2. Validates `project_hash` against PostgreSQL via `ProjectManagerRepository`
3. Calls `POST http://localhost:9003/analyze/event` with full event payload
4. Receives `MLAnalysisResult` (is_risky, risk_score, MITRE technique)
5. Upserts `ControlNode` and `ActorNode` in Neo4j
6. Creates `DriftEventNode` with ML classification persisted
7. If `is_risky=true` and `environment=production` → sets project `status=AT_RISK`

---

## Python ML Service

### Files
| File | Responsibility |
|---|---|
| `ml_engine.py` | Trains the PyTorch classifier; outputs `drift_model.pth` + `encoding_map.json` |
| `drift_detector.py` | Loads model; extracts features; predicts + scores per event |
| `compound_correlator.py` | NetworkX graph: correlates risky events → compound incidents |
| `api.py` | FastAPI: `/analyze/event`, `/analyze/batch`, `/dashboard/summary`, `/report/generate` |

---

## AI/ML Approach

### 1. Drift Detection — Supervised Neural Classifier

**Architecture**: Multi-Layer Perceptron (MLP) with 2 hidden layers.

```
Input (16 features) → Dense(16, ReLU) → Dense(8, ReLU) → Dense(1, Sigmoid)
```

**Why MLP over Isolation Forest?**
Isolation Forest is unsupervised and assumes most data is benign. Our dataset has labeled ground truth (`is_risky`) — a supervised classifier achieves higher precision and allows deliberate feature engineering. The concept of anomaly detection is preserved: the model learns what "normal" approved/automated changes look like versus deviations from that baseline.

**Training results**: Precision 100%, Recall 100%, Benign Suppression 100% on the 1000-event labeled dataset.

### 2. Feature Engineering (8 feature groups, 16 dimensions)

| Feature | Extraction Method | Rationale |
|---|---|---|
| `change_source` (one-hot, 4) | Categorical encoding | `manual` changes carry higher risk than `ci_cd` |
| `approval_status` (one-hot, 3) | Categorical encoding | `pending`/`expired` approvals are primary risk signal |
| `environment` (one-hot, 3) | Categorical encoding | Production drift is 3× more critical than dev |
| `severity` (one-hot, 5) | Categorical encoding | CRITICAL/HIGH correlated with risk |
| `maintenance_window` (1) | Boolean → float | Approved windows suppress false positives |
| `hour_of_day` (1) | `timestamp.split('T')[1][:2]` / 23.0 | After-hours changes are statistically riskier |
| **`severity_delta`** | `old_value_scale - new_value_scale` | Quantifies security posture degradation (AES-256→AES-128 = +0.67) |
| **`actor_risky_history`** | In-memory counter per actor | Repeat offenders amplify risk score by 5% per prior incident (capped at +50%) |

### 3. Risk Score Formula

```
base_risk    = severity_weight × env_criticality
suppression  = 0.9 × base_risk  (if approved)
             = 0.7 × base_risk  (if maintenance_window)
delta_mult   = 1.0 + max(0, severity_delta)     # 1.0–2.0
actor_mult   = 1.0 + min(0.5, history × 0.05)   # 1.0–1.5

risk_score   = max(0, base_risk - suppression) × delta_mult × actor_mult
```

### 4. Benign Change Suppression

Three-layer suppression prevents alert fatigue:

| Layer | Condition | Discount |
|---|---|---|
| Approval gate | `approval_status = approved` | 90% risk discount |
| Maintenance window | `maintenance_window = true` | 70% risk discount |
| Automated change | `change_source = ci_cd` + approved | Combined: near-zero risk |

### 5. Cross-Domain Compound Incident Correlation

**Algorithm** (NetworkX undirected graph):

1. Each **risky** drift event is a graph node.
2. Edges connect events that share:
   - Same `changed_by` actor (any time gap), **OR**
   - Events within a **2-hour sliding window**
3. **Connected components** = compound incidents (sorted by total risk score)

**Attack Path Detection**: Domain-pair combos map to known attacker patterns:

| Domains | Attack Path |
|---|---|
| cloud + network + endpoint + identity | 🚨 CRITICAL: All domains — maximum blast radius |
| cloud + network + endpoint | 🚨 Full lateral movement path |
| logging_disabled + firewall broadened + encryption downgraded | Single compound incident with compounded risk |

### 6. Blast Radius Estimation

```
exposure_score = min(100, domain_count × 20 + env_penalty)
env_penalty:   production=40, staging=20, development=5
```

Output: CRITICAL (≥80) / HIGH (≥60) / MEDIUM (≥40) / LOW (<40)

---

## Data Dictionary

### `drift_event_labels.csv` (1000 rows, 4 domains)

| Column | Type | Values | Description |
|---|---|---|---|
| `event_id` | string | `EVT-XXXX` | Unique event identifier |
| `control_id` | string | `CTRL-XXXX` | Control being monitored |
| `timestamp` | ISO 8601 | — | When the change occurred |
| `changed_by` | string | username / system | Actor who made the change |
| `change_source` | enum | `manual`, `ci_cd`, `automation`, `scheduled_task` | How the change was made |
| `approval_status` | enum | `approved`, `pending`, `expired` | Change approval state |
| `environment` | enum | `production`, `staging`, `development` | Target environment |
| `severity` | enum | `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`, `INFO` | Change severity |
| `parameter` | string | `logging_enabled`, `encryption_strength`, etc. | Which control parameter changed |
| `old_value` | string | — | Value before change |
| `new_value` | string | — | Value after change |
| `domain` | enum | `cloud`, `network`, `endpoint`, `identity` | Security domain |
| `system` | string | hostname / ARN | Affected system |
| `maintenance_window` | boolean | true/false | Was this inside an approved window? |
| `is_risky` | boolean | true/false | Ground-truth risk label |

### `baseline_configs.json` (60+ controls)

| Field | Description |
|---|---|
| `control_id` | Unique control identifier |
| `name` | Human-readable name |
| `parameter` | The monitored parameter |
| `expected_value` | Compliant state value |
| `severity` | Risk level if drifted |
| `compliance_mappings` | NIST/CIS/GDPR framework references |
| `environment_overrides` | Per-environment baseline variations |

---

## Deployment Guide

### Prerequisites
- Java 21, Maven 3.9+
- Python 3.11+
- PostgreSQL 15 (port 5432, db: `project_db`)
- Neo4j 5.x (port 7687, auth: `neo4j/admin123`)
- Apache Kafka (port 9092, topic: `drift-events`)

### Startup Order
```bash
# 1. Infrastructure
brew services start postgresql
brew services start neo4j
# Start Kafka
bin/zookeeper-server-start.sh config/zookeeper.properties &
bin/kafka-server-start.sh config/server.properties &

# Create Kafka topic
kafka-topics.sh --create --topic drift-events \
  --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# 2. Java Services (each in a separate terminal)
cd DiscoveryServer && mvn spring-boot:run   # port 8761
cd Authentication  && mvn spring-boot:run   # port 9001
cd Gateway         && mvn spring-boot:run   # port 9000
cd Ingestor        && mvn spring-boot:run   # port 9002

# 3. ML Model (one-time)
cp /path/to/scratch/drift_model.pth /Users/chandhru/DriftGuard/

# 4. Python ML Service
cd python_service && python3 api.py          # port 9003

# 5. React Frontend
cd frontend && npm run dev                   # port 5173

# 6. Test pipeline
python3 test_event_producer.py --batch --project-hash <HASH>
```
