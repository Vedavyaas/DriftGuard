# DriftGuard — Sample Drift Detection Report
**Report ID**: RPT-2024-001  
**Generated**: 2024-01-15T14:32:00Z  
**Analysis Window**: 2024-01-14T00:00:00Z → 2024-01-15T14:00:00Z  
**Total Events Analyzed**: 1000  
**Risky Events Detected**: 637  
**Compound Incidents**: 12

---

## Executive Summary

DriftGuard analyzed 1,000 configuration change events across four security domains (cloud, network, endpoint, identity). **637 events (63.7%)** were classified as risky, with **12 compound incidents** identified where related drifts were correlated across domains. The most critical incident (INC-0001) spans all four domains with an exposure score of **100/100**, indicating a coordinated drift pattern consistent with MITRE ATT&CK techniques T1562.001 and T1133. Immediate remediation is required for 3 CRITICAL incidents before end of business.

---

## Detected Risky Drift Events — Top 10

---

### DRIFT-001 ⛔ CRITICAL
**Control**: `CTRL-AWS-CW-001` — CloudWatch Logging  
**Domain**: Cloud | **Environment**: Production  
**Timestamp**: `2024-01-14T02:17:43Z`  
**Actor**: `deploy_pipeline` | **Change Source**: `ci_cd`  
**Approval Status**: `pending`  

| Field | Baseline | Current | Delta |
|---|---|---|---|
| `logging_enabled` | `true` | `false` | **DEGRADED** (Δ = +1.0) |

**ML Classification**: Risky (confidence: 99.8%) | **Risk Score**: 18.5  
**Severity Delta**: +1.0 (complete logging disable — maximum posture degradation)  
**Actor History**: 4 prior risky events (actor multiplier: ×1.20)

**MITRE ATT&CK**: T1562.001 — *Impair Defenses: Disable or Modify Tools*  

**Compliance Violations**:
- NIST AU-2 (Audit Events)
- NIST CM-3 (Configuration Change Control)
- CIS Control 8.5 (Collect Detailed Audit Logs)
- GDPR Article 32 (Security of Processing)

**Blast Radius**: CloudWatch disabled on `aws-prod-cloudwatch` means all subsequent changes in this production region are now **unlogged**. Any attacker actions during this window will leave no trail.

**Remediation Steps**:
1. `[IMMEDIATE]` Re-enable CloudWatch logging on `aws-prod-cloudwatch` via AWS Console or CLI: `aws cloudwatch put-metric-alarm --alarm-name logging-enabled ...`
2. `[URGENT]` Audit the 2h window (02:17–04:17 UTC) for any activity that occurred while logging was disabled — check VPC flow logs and S3 access logs as fallback.
3. `[24h]` Review why `deploy_pipeline` submitted a change with `pending` approval to a production logging control. Update pipeline guardrails.
4. `[48h]` File GDPR Article 32 incident report if any personal data was processed during the logging gap.

---

### DRIFT-002 ⛔ CRITICAL
**Control**: `CTRL-FW-EDGE-001` — Edge Firewall Inbound Rules  
**Domain**: Network | **Environment**: Production  
**Timestamp**: `2024-01-14T02:31:12Z`  
**Actor**: `deploy_pipeline` | **Change Source**: `manual`  
**Approval Status**: `expired`  

| Field | Baseline | Current | Delta |
|---|---|---|---|
| `inbound_rules` | `RESTRICTED` | `OPEN_0.0.0.0/0` | **DEGRADED** (Δ = +1.0) |

**ML Classification**: Risky (confidence: 99.9%) | **Risk Score**: 20.0  
**Actor History**: 5 prior risky events (actor multiplier: ×1.25)

**MITRE ATT&CK**: T1133 — *External Remote Services*

**Compliance Violations**:
- NIST CM-6 (Configuration Settings)
- CIS Control 4.1 (Establish Secure Configurations)

**Blast Radius**: All production services behind `fw-prod-edge-01` are now internet-accessible. Combined with DRIFT-001 (logging disabled), this creates a **blind-spot attack surface** — intrusions will not be logged.

**Remediation Steps**:
1. `[IMMEDIATE]` Revert `fw-prod-edge-01` inbound rules to RESTRICTED posture.
2. `[IMMEDIATE]` Check for active connections on newly opened ports (run `netstat -an` or AWS Security Group flow logs).
3. `[URGENT]` Block suspicious IP ranges identified in the exposure window.
4. `[24h]` Approval token for this change was expired — revoke actor's approval cache and require fresh MFA-authenticated approval.

---

### DRIFT-003 ⛔ CRITICAL
**Control**: `CTRL-EP-AGENT-001` — Endpoint Security Agent  
**Domain**: Endpoint | **Environment**: Production  
**Timestamp**: `2024-01-14T03:05:44Z`  
**Actor**: `deploy_pipeline` | **Change Source**: `automation`  
**Approval Status**: `pending`  

| Field | Baseline | Current | Delta |
|---|---|---|---|
| `agent_status` | `running` | `stopped` | **DEGRADED** (Δ = +1.0) |

**ML Classification**: Risky (confidence: 99.7%) | **Risk Score**: 17.4  

**MITRE ATT&CK**: T1562.001 — *Impair Defenses: Disable or Modify Tools*

**Compliance Violations**:
- NIST SI-4 (System Monitoring)
- CIS Control 2.1 (Establish and Maintain a Software Inventory)

**Remediation Steps**:
1. `[IMMEDIATE]` Restart endpoint agent: `systemctl start endpoint-agent` on `endpoint-agent-prod-01`
2. `[URGENT]` Run full posture scan for the agent-offline period.
3. `[24h]` Review why automation submitted a pending-approval change to endpoint agents.

---

### DRIFT-004 🔴 HIGH
**Control**: `CTRL-AWS-ENC-001` — RDS Encryption Strength  
**Domain**: Cloud | **Environment**: Production  
**Timestamp**: `2024-01-14T09:12:00Z`  
**Actor**: `ci_pipeline_bot` | **Change Source**: `ci_cd`  
**Approval Status**: `pending`  

| Field | Baseline | Current | Delta |
|---|---|---|---|
| `encryption_strength` | `AES-256` | `AES-128` | **DEGRADED** (Δ = +0.67) |

**ML Classification**: Risky (confidence: 97.1%) | **Risk Score**: 12.8  

**MITRE ATT&CK**: T1552 — *Unsecured Credentials*

**Compliance Violations**:
- NIST SC-28 (Protection of Information at Rest)
- GDPR Article 32 (Security of Processing — encryption requirement)

**Remediation Steps**:
1. `[URGENT]` Revert RDS instance `aws-prod-rds-01` to AES-256 encryption.
2. `[URGENT]` Rotate all encryption keys generated during the AES-128 window.
3. `[48h]` Audit data accessed from this RDS instance during the downgrade window.

---

### DRIFT-005 🔴 HIGH
**Control**: `CTRL-ID-MFA-001` — Identity MFA Enforcement  
**Domain**: Identity | **Environment**: Production  
**Timestamp**: `2024-01-14T11:45:22Z`  
**Actor**: `admin_override` | **Change Source**: `manual`  
**Approval Status**: `pending`  

| Field | Baseline | Current | Delta |
|---|---|---|---|
| `mfa_enforced` | `true` | `false` | **DEGRADED** (Δ = +1.0) |

**ML Classification**: Risky (confidence: 99.5%) | **Risk Score**: 10.0  

**MITRE ATT&CK**: T1556 — *Modify Authentication Process*

**Compliance Violations**:
- NIST IA-2 (Identification and Authentication)
- GDPR Article 25 (Data Protection by Design)

**Remediation Steps**:
1. `[IMMEDIATE]` Re-enable MFA enforcement on Okta production tenant.
2. `[IMMEDIATE]` Force re-authentication for all active sessions.
3. `[24h]` Review login events during the MFA-disabled window for anomalous access.

---

### DRIFT-006 🟡 MEDIUM
**Control**: `CTRL-NET-LOG-001` — Network Flow Logging  
**Domain**: Network | **Environment**: Staging  
**Timestamp**: `2024-01-14T14:00:00Z`  
**Actor**: `scheduled_task` | **Change Source**: `scheduled_task`  
**Approval Status**: `approved` | **Maintenance Window**: true  

**ML Classification**: Risky (confidence: 62%) | **Risk Score**: 0.6  
*Note: Risk significantly suppressed — maintenance window (70% discount) + approved (90% discount applied cumulatively). Low priority.*

**Remediation Steps**:
1. `[72h]` Verify flow logging was re-enabled after the scheduled maintenance completed.

---

### DRIFT-007–010 (Additional High-Risk Events)

| ID | Control | Domain | Env | Severity | Risk Score | MITRE |
|---|---|---|---|---|---|---|
| DRIFT-007 | CTRL-AWS-S3-001 (bucket policy) | Cloud | Production | HIGH | 9.6 | T1530 |
| DRIFT-008 | CTRL-EP-FW-001 (host firewall) | Endpoint | Production | HIGH | 8.0 | T1562.004 |
| DRIFT-009 | CTRL-ID-SESSION-001 (session timeout) | Identity | Staging | MEDIUM | 3.6 | T1078 |
| DRIFT-010 | CTRL-NET-IDS-001 (IDS signature update) | Network | Production | MEDIUM | 5.0 | T1562.006 |

---

## Compound Incident Analysis

### INC-0001 — 🚨 All-Domain Breach Path (CRITICAL)
**Risk Score**: 67.9 | **Events**: DRIFT-001, DRIFT-002, DRIFT-003, DRIFT-005  
**Domains**: Cloud + Network + Endpoint + Identity  
**Actor**: `deploy_pipeline` (all events within 58 minutes)  
**Time Span**: 02:17–03:05 UTC (48 minutes)  
**Blast Radius**: 100/100 — CRITICAL  

**Attack Path**:
> 🚨 CRITICAL: All domains compromised — maximum blast radius. Logging disabled (T1562.001), firewall opened (T1133), endpoint agent stopped (T1562.001), MFA disabled (T1556) — a textbook pre-exfiltration preparation sequence.

**Compliance Violations**: NIST AU-2, NIST CM-6, NIST SI-4, NIST IA-2, CIS 8.5, CIS 4.1, CIS 2.1, GDPR Art 32, GDPR Art 25

**Priority Remediation Sequence** (order matters):
1. **[NOW]** Restore logging (DRIFT-001) — without logs, all other fixes are unverifiable
2. **[NOW]** Revert firewall rules (DRIFT-002) — close the external exposure
3. **[NOW]** Restart endpoint agent (DRIFT-003) — restore detection capability
4. **[NOW]** Re-enable MFA (DRIFT-005) — prevent unauthorized access
5. **[1h]** Initiate incident response — this sequence matches T1562 pre-exfiltration preparation

---

### INC-0002 — 🔴 Encryption + Logging Correlation (HIGH)
**Risk Score**: 31.3 | **Events**: DRIFT-004, DRIFT-001  
**Domains**: Cloud (encryption downgrade correlated with logging disable)  
**Attack Path**: Agent disable + cloud misconfiguration = blind spot

---

## Compliance Impact Summary

| Framework | Violations | Controls Affected |
|---|---|---|
| NIST AU-2 | 8 | Logging controls across all domains |
| GDPR Art 32 | 7 | Encryption + logging in production |
| CIS 8.5 | 6 | Audit log collection |
| NIST CM-6 | 5 | Configuration settings |
| NIST IA-2 | 4 | Authentication controls |
| CIS 4.1 | 4 | Firewall configuration |
| NIST SI-4 | 3 | System monitoring |
| NIST SC-28 | 3 | Data at rest encryption |

---

## Recommended Actions by Priority

| Priority | Action | Deadline |
|---|---|---|
| 🚨 P0 | Remediate INC-0001 (all 4 events) | **Within 1 hour** |
| 🔴 P1 | Revert RDS encryption to AES-256 (DRIFT-004) | **Within 4 hours** |
| 🔴 P1 | File GDPR Article 32 incident report | **Within 72 hours** |
| 🟡 P2 | Audit `deploy_pipeline` actor — 5+ risky events | **Within 24 hours** |
| 🟡 P2 | Review CI/CD pipeline approval gates | **Within 1 week** |
| 🟢 P3 | Verify DRIFT-006 maintenance window completion | **Within 72 hours** |
