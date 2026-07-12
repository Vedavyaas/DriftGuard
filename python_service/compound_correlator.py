from __future__ import annotations
from dataclasses import dataclass, field
from typing import List, Dict, Any
from datetime import datetime, timezone
import networkx as nx

CORRELATION_WINDOW_HOURS = 2

DOMAIN_ATTACK_PATHS = {
    frozenset({"cloud", "network"}): "Lateral movement via cloud-to-network bridge",
    frozenset({"cloud", "endpoint"}): "Agent disable + cloud misconfiguration = blind spot",
    frozenset({"cloud", "identity"}): "Access escalation through identity + cloud",
    frozenset({"network", "endpoint"}): "Endpoint agent killed + firewall opened = open door",
    frozenset({"network", "identity"}): "MFA bypass + network exposure",
    frozenset({"endpoint", "identity"}): "Credential theft path via endpoint compromise",
    frozenset({"cloud", "network", "endpoint"}): "🚨 Full lateral movement path",
    frozenset({"cloud", "network", "identity"}): "🚨 Cloud + network + identity — coordinated attack path",
    frozenset({"cloud", "network", "endpoint", "identity"}): "🚨 CRITICAL: All domains compromised — maximum blast radius",
}

def _parse_ts(ts_str: str) -> datetime:
    try:
        return datetime.fromisoformat(ts_str.replace('Z', '+00:00'))
    except Exception:
        return datetime(2000, 1, 1, tzinfo=timezone.utc)

@dataclass
class CompoundIncident:
    incident_id: str
    events: List[Dict[str, Any]]
    domains: List[str]
    actors: List[str]
    total_risk_score: float
    max_severity: str
    attack_path: str
    blast_radius: Dict[str, Any]
    compliance_violations: List[str]
    time_span_minutes: float
    remediation_steps: List[str]
    analyst_narrative: str
    graph_data: Dict[str, Any] = field(default_factory=dict)

    def to_dict(self) -> dict:
        return {
            "incident_id": self.incident_id,
            "event_ids": [e.get("event_id") for e in self.events],
            "domains": self.domains,
            "actors": self.actors,
            "total_risk_score": self.total_risk_score,
            "max_severity": self.max_severity,
            "attack_path": self.attack_path,
            "blast_radius": self.blast_radius,
            "compliance_violations": self.compliance_violations,
            "time_span_minutes": self.time_span_minutes,
            "remediation_steps": self.remediation_steps,
            "analyst_narrative": self.analyst_narrative,
            "event_count": len(self.events),
            "graph_data": self.graph_data,
        }

SEVERITY_ORDER = ["INFO", "LOW", "MEDIUM", "HIGH", "CRITICAL"]

COMPLIANCE_MAP = {
    "logging_enabled": ["NIST AU-2", "NIST CM-3", "CIS 8.5", "GDPR Art 32"],
    "encryption_strength": ["NIST SC-28", "GDPR Art 32"],
    "inbound_rules": ["NIST CM-6", "CIS 4.1"],
    "agent_status": ["NIST SI-4", "CIS 2.1"],
    "mfa_enforced": ["NIST IA-2", "GDPR Art 25"],
}

REMEDIATION_TEMPLATES = {
    "logging_enabled": "1. Re-enable logging immediately on {system}. 2. Review audit trail gap. 3. File incident report.",
    "encryption_strength": "1. Revert encryption to AES-256 on {system}. 2. Rotate any keys generated during the downgrade window. 3. Audit data accessed.",
    "inbound_rules": "1. Revert firewall rules on {system} to RESTRICTED. 2. Check for active connections on opened ports. 3. Block suspicious IPs.",
    "agent_status": "1. Restart endpoint agent on affected servers. 2. Review agent logs for the gap period. 3. Run full posture scan.",
    "mfa_enforced": "1. Re-enable MFA enforcement on {system}. 2. Force re-authentication for all users. 3. Review login events during gap.",
}

def correlate(analyzed_events: List[Dict[str, Any]], original_events: List[Dict[str, Any]]) -> List[CompoundIncident]:
    event_map = {e['event_id']: e for e in original_events if 'event_id' in e}
    enriched: List[Dict[str, Any]] = []
    for a in analyzed_events:
        if not a.get('is_risky'):
            continue
        eid = a.get('event_id')
        orig = event_map.get(eid, {})
        merged = {**orig, **a}
        enriched.append(merged)

    if not enriched:
        return []

    G = nx.Graph()
    for e in enriched:
        G.add_node(e['event_id'], data=e)

    for i, e1 in enumerate(enriched):
        for j, e2 in enumerate(enriched):
            if i >= j:
                continue
            ts1 = _parse_ts(e1.get('timestamp', ''))
            ts2 = _parse_ts(e2.get('timestamp', ''))
            delta = abs((ts1 - ts2).total_seconds()) / 3600

            same_actor = e1.get('changed_by') == e2.get('changed_by') != ''
            within_window = delta <= CORRELATION_WINDOW_HOURS

            if same_actor or within_window:
                G.add_edge(e1['event_id'], e2['event_id'])

    incidents: List[CompoundIncident] = []
    for idx, component in enumerate(nx.connected_components(G)):
        subgraph = G.subgraph(component)
        component_events = [subgraph.nodes[eid]['data'] for eid in subgraph.nodes]
        
        # Serialize subgraph for frontend React Flow rendering
        graph_data = {
            "nodes": [
                {"id": node_id, "data": subgraph.nodes[node_id]['data']} 
                for node_id in subgraph.nodes
            ],
            "edges": [
                {"source": u, "target": v} 
                for u, v in subgraph.edges
            ]
        }

        domains = list({e.get('domain', 'unknown') for e in component_events})
        actors = list({e.get('changed_by', 'unknown') for e in component_events})
        total_risk = sum(e.get('risk_score', 0) for e in component_events)

        max_sev = max(severities, key=lambda s: SEVERITY_ORDER.index(s) if s in SEVERITY_ORDER else 0) if (severities := [e.get('severity', 'LOW') for e in component_events]) else "LOW"

        domain_set = frozenset(domains)
        attack_path = DOMAIN_ATTACK_PATHS.get(domain_set, f"Cross-domain drift across: {', '.join(domains)}")

        blast_radius = _compute_blast_radius(component_events, domains)

        violations: list = []
        for e in component_events:
            param = e.get('parameter', '')
            violations.extend(COMPLIANCE_MAP.get(param, []))
        violations = list(dict.fromkeys(violations))

        timestamps = [_parse_ts(e.get('timestamp', '')) for e in component_events]
        span_mins = 0.0
        if len(timestamps) > 1:
            span_mins = round((max(timestamps) - min(timestamps)).total_seconds() / 60, 1)

        sorted_events = sorted(component_events, key=lambda e: e.get('risk_score', 0), reverse=True)
        remediation_steps = []
        for e in sorted_events:
            param = e.get('parameter', '')
            template = REMEDIATION_TEMPLATES.get(param, f"Review and remediate control {e.get('control_id')}")
            step = template.format(system=e.get('system', e.get('control_id', 'unknown')))
            remediation_steps.append(f"[{e.get('severity', 'LOW')}] {step}")

        from llm_narrative import generate_analyst_narrative
        inc_id = f"INC-{idx+1:04d}"
        llm_data = generate_analyst_narrative(
            incident_id=inc_id,
            domains=domains,
            actors=actors,
            events=component_events,
            blast_radius=blast_radius
        )
        
        narrative = llm_data.get("narrative", "")
        remediation_steps = llm_data.get("remediation", remediation_steps)

        incidents.append(CompoundIncident(
            incident_id=inc_id,
            events=component_events,
            domains=domains,
            actors=actors,
            total_risk_score=round(total_risk, 2),
            max_severity=max_sev,
            attack_path=attack_path,
            blast_radius=blast_radius,
            compliance_violations=violations,
            time_span_minutes=span_mins,
            remediation_steps=remediation_steps,
            analyst_narrative=narrative,
        ))

    incidents.sort(key=lambda inc: inc.total_risk_score, reverse=True)
    return incidents

def _compute_blast_radius(events: List[Dict], domains: List[str]) -> Dict[str, Any]:
    systems = list({e.get('system', e.get('control_id', 'unknown')) for e in events})
    environments = list({e.get('environment', 'unknown') for e in events})
    parameters = list({e.get('parameter', 'unknown') for e in events})

    domain_penalty = len(domains) * 20
    env_penalty = 40 if 'production' in environments else (20 if 'staging' in environments else 5)
    exposure_score = min(100, domain_penalty + env_penalty)

    return {
        "affected_systems": systems,
        "affected_environments": environments,
        "exposed_parameters": parameters,
        "domain_count": len(domains),
        "exposure_score": exposure_score,
        "exposure_level": "CRITICAL" if exposure_score >= 80 else "HIGH" if exposure_score >= 60 else "MEDIUM" if exposure_score >= 40 else "LOW",
    }
