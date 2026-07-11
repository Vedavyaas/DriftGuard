#!/usr/bin/env python3
"""
DriftGuard — Test Event Producer
Sends a sample drift event to the Kafka 'drift-events' topic
to test the full pipeline: Kafka → Ingestor → ML Service → Neo4j

Usage:
    python3 test_event_producer.py                     # sends 1 sample risky event
    python3 test_event_producer.py --batch             # sends 5 diverse events
    python3 test_event_producer.py --project-hash XXX  # override project hash

Requirements:
    pip install kafka-python
"""

import json
import argparse
from datetime import datetime, timezone

try:
    from kafka import KafkaProducer
except ImportError:
    print("Install kafka-python first:  pip install kafka-python")
    exit(1)

KAFKA_BOOTSTRAP = "localhost:9092"
TOPIC = "drift-events"

# ── Replace this with your actual project hash from the dashboard ──────────────
DEFAULT_PROJECT_HASH = "REPLACE_WITH_YOUR_PROJECT_HASH"

SAMPLE_EVENTS = [
    {
        "project_hash":     DEFAULT_PROJECT_HASH,
        "event_id":         "EVT-TEST-001",
        "control_id":       "CTRL-AWS-001",
        "timestamp":        datetime.now(timezone.utc).isoformat(),
        "changed_by":       "ci_pipeline_bot",
        "change_source":    "ci_cd",
        "approval_status":  "pending",
        "environment":      "production",
        "severity":         "CRITICAL",
        "parameter":        "logging_enabled",
        "old_value":        "true",
        "new_value":        "false",
        "domain":           "cloud",
        "system":           "aws-prod-cloudwatch",
        "maintenance_window": False
    },
    {
        "project_hash":     DEFAULT_PROJECT_HASH,
        "event_id":         "EVT-TEST-002",
        "control_id":       "CTRL-FW-001",
        "timestamp":        datetime.now(timezone.utc).isoformat(),
        "changed_by":       "ci_pipeline_bot",
        "change_source":    "manual",
        "approval_status":  "expired",
        "environment":      "production",
        "severity":         "HIGH",
        "parameter":        "inbound_rules",
        "old_value":        "RESTRICTED",
        "new_value":        "OPEN_0.0.0.0/0",
        "domain":           "network",
        "system":           "fw-prod-edge-01",
        "maintenance_window": False
    },
    {
        "project_hash":     DEFAULT_PROJECT_HASH,
        "event_id":         "EVT-TEST-003",
        "control_id":       "CTRL-EP-001",
        "timestamp":        datetime.now(timezone.utc).isoformat(),
        "changed_by":       "ci_pipeline_bot",
        "change_source":    "automation",
        "approval_status":  "pending",
        "environment":      "production",
        "severity":         "CRITICAL",
        "parameter":        "agent_status",
        "old_value":        "running",
        "new_value":        "stopped",
        "domain":           "endpoint",
        "system":           "endpoint-agent-prod-01",
        "maintenance_window": False
    },
    {
        "project_hash":     DEFAULT_PROJECT_HASH,
        "event_id":         "EVT-TEST-004",
        "control_id":       "CTRL-ID-001",
        "timestamp":        datetime.now(timezone.utc).isoformat(),
        "changed_by":       "admin_override",
        "change_source":    "manual",
        "approval_status":  "approved",
        "environment":      "staging",
        "severity":         "MEDIUM",
        "parameter":        "mfa_enforced",
        "old_value":        "true",
        "new_value":        "false",
        "domain":           "identity",
        "system":           "okta-staging",
        "maintenance_window": True
    },
    {
        "project_hash":     DEFAULT_PROJECT_HASH,
        "event_id":         "EVT-TEST-005",
        "control_id":       "CTRL-AWS-002",
        "timestamp":        datetime.now(timezone.utc).isoformat(),
        "changed_by":       "deploy_bot",
        "change_source":    "ci_cd",
        "approval_status":  "approved",
        "environment":      "development",
        "severity":         "LOW",
        "parameter":        "encryption_strength",
        "old_value":        "AES-256",
        "new_value":        "AES-128",
        "domain":           "cloud",
        "system":           "aws-dev-rds-01",
        "maintenance_window": False
    },
]


def send_events(events, project_hash=None):
    if project_hash:
        for e in events:
            e["project_hash"] = project_hash

    producer = KafkaProducer(
        bootstrap_servers=KAFKA_BOOTSTRAP,
        value_serializer=lambda v: json.dumps(v).encode("utf-8"),
    )

    for event in events:
        future = producer.send(TOPIC, value=event)
        result = future.get(timeout=10)
        print(f"  ✅ Sent {event['event_id']} [{event['severity']}] "
              f"→ partition {result.partition}, offset {result.offset}")

    producer.flush()
    producer.close()
    print(f"\nDone. {len(events)} event(s) published to '{TOPIC}'")
    print("Check the Ingestor logs for ML classification results.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="DriftGuard test event producer")
    parser.add_argument("--batch", action="store_true", help="Send all 5 sample events")
    parser.add_argument("--project-hash", type=str, help="Override project hash")
    args = parser.parse_args()

    events = SAMPLE_EVENTS if args.batch else [SAMPLE_EVENTS[0]]
    print(f"Publishing {len(events)} drift event(s) to Kafka topic '{TOPIC}'...")
    send_events(events, project_hash=args.project_hash)
