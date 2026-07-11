import torch
import torch.nn as nn
import numpy as np
import json
import os
from collections import defaultdict
from typing import List

ENCODING_MAP_PATH = os.path.join(os.path.dirname(__file__), '..', 'encoding_map.json')
MODEL_PATH = os.path.join(os.path.dirname(__file__), '..', 'drift_model.pth')

SEVERITY_WEIGHTS = {
    "CRITICAL": 10,
    "HIGH": 8,
    "MEDIUM": 5,
    "LOW": 2,
    "INFO": 0,
}

ENV_CRITICALITY = {
    "production": 1.0,
    "staging": 0.6,
    "development": 0.3,
}

MITRE_MAP = {
    "logging_enabled": {"technique": "T1562.001", "name": "Impair Defenses: Disable or Modify Tools"},
    "encryption_strength": {"technique": "T1552", "name": "Unsecured Credentials"},
    "inbound_rules": {"technique": "T1133", "name": "External Remote Services"},
    "agent_status": {"technique": "T1562.001", "name": "Impair Defenses: Disable or Modify Tools"},
    "mfa_enforced": {"technique": "T1556", "name": "Modify Authentication Process"},
}

_VALUE_SCALE = {
    "true": 1.0,  "false": 0.0,
    "enabled": 1.0, "disabled": 0.0,
    "running": 1.0, "stopped": 0.0,
    "aes-256": 1.0, "aes-192": 0.66, "aes-128": 0.33, "none": 0.0,
    "restricted": 1.0, "open_0.0.0.0/0": 0.0, "open": 0.0,
    "enforced": 1.0, "optional": 0.5, "disabled": 0.0,
}

def _value_to_scale(val: str) -> float:
    if val is None:
        return 0.5
    return _VALUE_SCALE.get(str(val).strip().lower(), 0.5)

def compute_severity_delta(old_value, new_value) -> float:
    old_s = _value_to_scale(str(old_value) if old_value is not None else "")
    new_s = _value_to_scale(str(new_value) if new_value is not None else "")
    return round(old_s - new_s, 4)

class DriftClassifier(nn.Module):
    def __init__(self, input_dim: int):
        super().__init__()
        self.net = nn.Sequential(
            nn.Linear(input_dim, 16),
            nn.ReLU(),
            nn.Linear(16, 8),
            nn.ReLU(),
            nn.Linear(8, 1),
            nn.Sigmoid()
        )

    def forward(self, x):
        return self.net(x)

class DriftDetector:
    def __init__(self):
        with open(ENCODING_MAP_PATH) as f:
            self.encoding_map: dict = json.load(f)

        self._build_feature_list()

        self.model = DriftClassifier(len(self.feature_names))
        self.model.load_state_dict(torch.load(MODEL_PATH, weights_only=True))
        self.model.eval()
        self._actor_risky_count: dict = defaultdict(int)

    def _build_feature_list(self):
        cat_cols = ['change_source', 'approval_status', 'environment', 'severity']
        self.feature_names = []
        for col in cat_cols:
            for val in self.encoding_map[col]:
                self.feature_names.append(f"{col}_{val}")
        self.feature_names.append('maintenance_window_val')
        self.feature_names.append('hour_of_day')

    def _extract_features(self, event: dict) -> np.ndarray:
        cat_cols = ['change_source', 'approval_status', 'environment', 'severity']
        features = []
        for col in cat_cols:
            for val in self.encoding_map[col]:
                features.append(1.0 if event.get(col) == val else 0.0)
        features.append(1.0 if event.get('maintenance_window') else 0.0)
        ts = event.get('timestamp', 'T00:')
        try:
            hour = float(ts.split('T')[1].split(':')[0]) / 23.0
        except Exception:
            hour = 0.0
        features.append(hour)
        return np.array(features, dtype=np.float32)

    def predict(self, event: dict, update_actor_history: bool = True) -> dict:
        features = self._extract_features(event)
        tensor = torch.tensor(features).unsqueeze(0)
        with torch.no_grad():
            prob = self.model(tensor).item()
        is_risky = prob >= 0.5

        sev_delta = compute_severity_delta(
            event.get('old_value'), event.get('new_value')
        )
        delta_multiplier = 1.0 + max(0.0, sev_delta)

        actor = event.get('changed_by', '') or ''
        actor_risky_history = self._actor_risky_count.get(actor, 0)
        actor_multiplier = 1.0 + min(0.5, actor_risky_history * 0.05)

        severity = event.get('severity', 'LOW')
        environment = event.get('environment', 'development')
        rule_weight = SEVERITY_WEIGHTS.get(severity, 0)
        env_crit = ENV_CRITICALITY.get(environment, 0.3)

        suppression_discount = 0
        if event.get('approval_status') == 'approved':
            suppression_discount = rule_weight * 0.9
        elif event.get('maintenance_window'):
            suppression_discount = rule_weight * 0.7

        base_risk = max(0.0, (rule_weight * env_crit) - suppression_discount)
        risk_score = round(base_risk * delta_multiplier * actor_multiplier, 2)

        if update_actor_history and is_risky and actor:
            self._actor_risky_count[actor] += 1

        parameter = event.get('parameter', '')
        mitre = MITRE_MAP.get(parameter, {"technique": "T1562", "name": "Impair Defenses"})

        return {
            "event_id":          event.get('event_id'),
            "control_id":        event.get('control_id'),
            "is_risky":          bool(is_risky),
            "confidence":        round(prob if is_risky else 1 - prob, 4),
            "risk_score":        risk_score,
            "severity":          severity,
            "environment":       environment,
            "severity_delta":    sev_delta,
            "delta_direction":   "DEGRADED" if sev_delta > 0 else ("IMPROVED" if sev_delta < 0 else "NEUTRAL"),
            "actor_risky_history": actor_risky_history,
            "actor":             actor,
            "mitre_technique":   mitre["technique"],
            "mitre_name":        mitre["name"],
        }

    def predict_batch(self, events: list) -> list:
        results = []
        for event in events:
            results.append(self.predict(event, update_actor_history=True))
        return results

    def reset_actor_history(self):
        self._actor_risky_count.clear()

_detector: DriftDetector | None = None

def get_detector() -> DriftDetector:
    global _detector
    if _detector is None:
        _detector = DriftDetector()
    return _detector
