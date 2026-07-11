from __future__ import annotations
import csv
import json
import os
from io import StringIO
from typing import List, Optional, Any, Dict
import uvicorn
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, field_validator
import sys

sys.path.insert(0, os.path.dirname(__file__))

from drift_detector import get_detector
from compound_correlator import correlate, CompoundIncident

_SVC_DIR = os.path.dirname(__file__)
_ROOT = os.path.join(_SVC_DIR, '..')
EVENTS_CSV = os.path.join(_ROOT, 'drift_event_labels.csv')
BASELINES_JSON = os.path.join(_ROOT, 'baseline_configs.json')

app = FastAPI(
    title="DriftGuard ML Intelligence",
    description="ML-driven drift detection, compound incident correlation, and compliance mapping.",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

class DriftEvent(BaseModel):
    event_id: str
    control_id: str
    timestamp: str
    changed_by: Optional[str] = ""
    change_source: str
    approval_status: str
    environment: str
    severity: str
    parameter: Optional[str] = ""
    old_value: Optional[Any] = None
    new_value: Optional[Any] = None
    domain: Optional[str] = ""
    system: Optional[str] = ""
    maintenance_window: Optional[bool] = False

class BatchRequest(BaseModel):
    events: List[DriftEvent]

class SingleEvent(BaseModel):
    event: DriftEvent

def _load_events_from_csv() -> List[Dict]:
    events = []
    try:
        with open(EVENTS_CSV, newline='') as f:
            reader = csv.DictReader(f)
            for row in reader:
                row['maintenance_window'] = row.get('maintenance_window', 'False').lower() == 'true'
                events.append(row)
    except FileNotFoundError:
        pass
    return events

def _load_baselines() -> Dict:
    try:
        with open(BASELINES_JSON) as f:
            return json.load(f)
    except FileNotFoundError:
        return {}

@app.get("/health")
def health():
    return {"status": "ok", "service": "DriftGuard ML Intelligence", "version": "1.0.0"}

@app.post("/analyze/event")
def analyze_single(body: SingleEvent):
    detector = get_detector()
    result = detector.predict(body.event.model_dump())
    return result

@app.post("/analyze/batch")
def analyze_batch(body: BatchRequest):
    detector = get_detector()
    events_dicts = [e.model_dump() for e in body.events]
    analyzed = detector.predict_batch(events_dicts)
    incidents = correlate(analyzed, events_dicts)
    return {
        "total_events": len(analyzed),
        "risky_events": sum(1 for a in analyzed if a['is_risky']),
        "compound_incidents": [inc.to_dict() for inc in incidents],
        "event_results": analyzed,
    }

@app.get("/dashboard/summary")
def dashboard_summary():
    detector = get_detector()
    raw_events = _load_events_from_csv()
    if not raw_events:
        raise HTTPException(status_code=503, detail="Event data not available")

    analyzed = detector.predict_batch(raw_events)

    domain_health: Dict[str, Dict] = {}
    for event, result in zip(raw_events, analyzed):
        domain = event.get('domain', 'unknown')
        if domain not in domain_health:
            domain_health[domain] = {"total": 0, "risky": 0, "critical": 0, "total_risk": 0.0}
        domain_health[domain]["total"] += 1
        if result['is_risky']:
            domain_health[domain]["risky"] += 1
        if result['severity'] == 'CRITICAL':
            domain_health[domain]["critical"] += 1
        domain_health[domain]["total_risk"] += result.get('risk_score', 0)

    combined = []
    for event, result in zip(raw_events, analyzed):
        if result['is_risky']:
            combined.append({**event, **result})
    combined.sort(key=lambda x: x.get('risk_score', 0), reverse=True)
    top_events = combined[:20]

    risky_analyzed = [r for r in analyzed if r['is_risky']]
    incidents = correlate(risky_analyzed, raw_events)

    severity_dist: Dict[str, int] = {}
    for r in analyzed:
        sev = r.get('severity', 'LOW')
        severity_dist[sev] = severity_dist.get(sev, 0) + 1

    from collections import defaultdict
    daily_risk: Dict[str, int] = defaultdict(int)
    for event, result in zip(raw_events, analyzed):
        if result['is_risky']:
            day = event.get('timestamp', '')[:10]
            daily_risk[day] += 1
    timeline = [{"date": k, "count": v} for k, v in sorted(daily_risk.items())[-30:]]

    compliance_counts: Dict[str, int] = {}
    for inc in incidents:
        for v in inc.compliance_violations:
            compliance_counts[v] = compliance_counts.get(v, 0) + 1
    compliance_leaderboard = sorted(compliance_counts.items(), key=lambda x: x[1], reverse=True)[:10]

    return {
        "total_events": len(analyzed),
        "risky_events": sum(1 for r in analyzed if r['is_risky']),
        "compound_incidents_count": len(incidents),
        "domain_health": domain_health,
        "severity_distribution": severity_dist,
        "top_risky_events": top_events,
        "compound_incidents": [inc.to_dict() for inc in incidents[:10]],
        "timeline": timeline,
        "compliance_leaderboard": [
            {"framework": k, "violations": v} for k, v in compliance_leaderboard
        ],
    }

@app.get("/report/generate")
def generate_report():
    detector = get_detector()
    raw_events = _load_events_from_csv()
    if not raw_events:
        raise HTTPException(status_code=503, detail="Event data not available")

    analyzed = detector.predict_batch(raw_events)
    risky = [a for a in analyzed if a['is_risky']]
    incidents = correlate(risky, raw_events)

    critical_incidents = [i for i in incidents if i.max_severity == 'CRITICAL']
    high_incidents = [i for i in incidents if i.max_severity == 'HIGH']

    return {
        "report_title": "DriftGuard Security Control Drift Intelligence Report",
        "summary": {
            "total_events_analyzed": len(analyzed),
            "risky_events_detected": len(risky),
            "compound_incidents": len(incidents),
            "critical_incidents": len(critical_incidents),
            "high_incidents": len(high_incidents),
        },
        "executive_narrative": (
            f"DriftGuard analyzed {len(analyzed)} configuration change events and identified "
            f"{len(risky)} risky drift events. These were correlated into {len(incidents)} compound incidents. "
            f"Of these, {len(critical_incidents)} are classified CRITICAL and require immediate remediation. "
            f"The highest-risk incident ({incidents[0].incident_id if incidents else 'N/A'}) spans "
            f"{len(incidents[0].domains) if incidents else 0} security domains with a blast radius exposure "
            f"score of {incidents[0].blast_radius['exposure_score'] if incidents else 0}/100."
        ),
        "critical_incidents": [inc.to_dict() for inc in critical_incidents[:5]],
        "all_incidents": [inc.to_dict() for inc in incidents],
    }

@app.get("/baselines")
def get_baselines():
    data = _load_baselines()
    controls = []
    for domain, domain_data in data.get('baseline_configs', {}).items():
        for ctrl_id, ctrl in domain_data.get('controls', {}).items():
            controls.append({
                "control_id": ctrl_id,
                "domain": domain,
                "name": ctrl.get('name'),
                "description": ctrl.get('description'),
                "severity": ctrl.get('severity'),
                "compliance": ctrl.get('compliance_mappings', []),
            })
    return {"controls": controls, "total": len(controls)}

if __name__ == "__main__":
    uvicorn.run("api:app", host="0.0.0.0", port=9003, reload=True, log_level="info")
