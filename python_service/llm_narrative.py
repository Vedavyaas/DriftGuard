import os
import json
from typing import List, Dict, Any
from groq import Groq

_client = None

def get_groq_client():
    global _client
    if _client is None:
        api_key = os.environ.get("GROQ_API_KEY")
        if api_key:
            _client = Groq(api_key=api_key)
    return _client

def generate_analyst_narrative(incident_id: str, domains: List[str], actors: List[str], events: List[Dict[str, Any]], blast_radius: Dict[str, Any]) -> Dict[str, Any]:
    client = get_groq_client()
    events_summary = []
    for e in events:
        events_summary.append(
            f"- Event {e.get('event_id')} on system '{e.get('system')}' (Domain: {e.get('domain')}): "
            f"Parameter '{e.get('parameter')}' drifted from '{e.get('old_value')}' to '{e.get('new_value')}'. "
            f"Severity: {e.get('severity')}. Risk Score: {e.get('risk_score')}. "
            f"MITRE Technique: {e.get('mitre_technique')} ({e.get('mitre_name')})."
        )
    events_str = "\n".join(events_summary)
    
    prompt = f"""You are an elite Security Analyst auditing control drift incidents in a cloud-native enterprise.
Analyze the following compound security control drift incident.

Incident ID: {incident_id}
Affected Domains: {", ".join(domains)}
Actors Involved: {", ".join(actors)}
Exposure Score: {blast_radius.get('exposure_score')}/100 ({blast_radius.get('exposure_level')})
Affected Systems: {", ".join(blast_radius.get('affected_systems', []))}

Correlated Control Drift Events:
{events_str}

Respond ONLY with a JSON object containing:
1. "narrative": A concise professional narrative detailing the evidence chain, the combined MITRE hazard, and compliance framework impact.
2. "remediation": A list of exactly 3 priority-sequenced technical remediation steps to fix the drifts in order of risk priority.

Example JSON output format:
{{
  "narrative": "Detailed narrative here...",
  "remediation": [
    "1. Action item one...",
    "2. Action item two...",
    "3. Action item three..."
  ]
}}
"""

    if not client:
        first_event = events[0] if events else {}
        fallback_narrative = (
            f"DriftGuard detected a correlated multi-domain incident ({incident_id}) spanning {', '.join(domains)} domains. "
            f"The evidence chain indicates actor {', '.join(actors)} executed a series of changes starting at "
            f"{first_event.get('timestamp', 'unknown time')}. Posture degradation level: {blast_radius.get('exposure_level')}."
        )
        return {
            "narrative": fallback_narrative,
            "remediation": [
                f"1. Restore baseline parameter '{events[0].get('parameter')}' on '{events[0].get('system')}' immediately.",
                "2. Audit all session activity for the duration of the drift window.",
                "3. Re-verify the posture integrity of the affected control domains."
            ]
        }

    try:
        completion = client.chat.completions.create(
            model="meta-llama/llama-4-scout-17b-16e-instruct",
            messages=[
                {"role": "system", "content": "You are a professional CSIRT analyst. Return valid JSON only."},
                {"role": "user", "content": prompt}
            ],
            temperature=0.2,
            max_tokens=500,
            response_format={"type": "json_object"}
        )
        res_data = json.loads(completion.choices[0].message.content.strip())
        return {
            "narrative": res_data.get("narrative", ""),
            "remediation": res_data.get("remediation", [])
        }
    except Exception as e:
        return {
            "narrative": f"Incident {incident_id} spans {', '.join(domains)} domains with exposure score {blast_radius.get('exposure_score')}/100.",
            "remediation": [
                f"1. Review and revert changed control policies on affected systems: {', '.join(blast_radius.get('affected_systems', []))}.",
                "2. Audit logs for suspicious activity during this window.",
                "3. Reset the system configuration state back to compliant baselines."
            ]
        }
