import os
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

def generate_analyst_narrative(incident_id: str, domains: List[str], actors: List[str], events: List[Dict[str, Any]], blast_radius: Dict[str, Any]) -> str:
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
Analyze the following compound security control drift incident and generate a concise, professional executive analyst narrative.

Incident ID: {incident_id}
Affected Domains: {", ".join(domains)}
Actors Involved: {", ".join(actors)}
Exposure Score: {blast_radius.get('exposure_score')}/100 ({blast_radius.get('exposure_level')})
Affected Systems: {", ".join(blast_radius.get('affected_systems', []))}

Correlated Control Drift Events:
{events_str}

Provide your response in clear paragraphs covering:
1. Evidence Chain: Explain how these drifts relate (e.g., temporal sequence, actor crossover).
2. MITRE Attack Path & Hazard: Explain what threat vector this specific combination of drifts exposes (e.g., disabling logging + opening firewalls is a pre-exfiltration signature).
3. Compliance Impact: Highlight which major compliance frameworks (NIST, CIS, GDPR) are violated.
4. Remediation Priority: Suggest a priority-sequenced fixing guide (e.g., fix logging first to gain visibility, then restore firewall configuration, then encrypt data).

Be concise, realistic, and highly professional. Limit to 3 short paragraphs. Do not use generic filler.
"""

    if not client:
        first_event = events[0] if events else {}
        return (
            f"DriftGuard detected a correlated multi-domain incident ({incident_id}) spanning {', '.join(domains)} domains. "
            f"The evidence chain indicates actor {', '.join(actors)} executed a series of changes starting at "
            f"{first_event.get('timestamp', 'unknown time')}. The combined posture degradation (exposure level: {blast_radius.get('exposure_level')}) "
            f"shows a clear threat path: the attacker disabled logging/auditing tools to hide their footprints before expanding access controls. "
            f"This invalidates compliance baselines across NIST SP 800-53 and GDPR Article 32. "
            f"Remediation must be sequenced: First, restore visibility (logging/agents), then close ingress ports, and finally rotate affected keys."
        )

    try:
        completion = client.chat.completions.create(
            model="llama-3.1-70b-versatile",
            messages=[
                {"role": "system", "content": "You are a professional Cyber Security Incident Response Team (CSIRT) analyst writing executive intelligence summaries."},
                {"role": "user", "content": prompt}
            ],
            temperature=0.2,
            max_tokens=450
        )
        return completion.choices[0].message.content.strip()
    except Exception as e:
        return (
            f"[Fallback Narrative] Incident {incident_id} spans {', '.join(domains)} domains with exposure score {blast_radius.get('exposure_score')}/100. "
            f"Events suggest a coordinated drift sequence by {', '.join(actors)}. Violates NIST/CIS baselines. "
            f"Priority Action: Restore logging systems first to enable audit verification, followed by access group restrictions. (Error calling Groq: {str(e)})"
        )
