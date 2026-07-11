package com.pheonix.ingestor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class PythonResponse {
    
    @JsonProperty("total_events")
    private Integer totalEvents;
    
    @JsonProperty("risky_events")
    private Integer riskyEvents;
    
    @JsonProperty("compound_incidents")
    private List<CompoundIncidentDTO> compoundIncidents;
    
    @JsonProperty("is_risky")
    private Boolean isRisky;
    
    private String severity;
    
    @JsonProperty("risk_score")
    private Double riskScore;

    @JsonProperty("mitre_technique")
    private String mitreTechnique;

    @JsonProperty("mitre_name")
    private String mitreName;

    @JsonProperty("delta_direction")
    private String deltaDirection;

    @JsonProperty("actor")
    private String actor;
    
    public PythonResponse() {}

    public Integer getTotalEvents() { return totalEvents; }
    public void setTotalEvents(Integer totalEvents) { this.totalEvents = totalEvents; }

    public Integer getRiskyEvents() { return riskyEvents; }
    public void setRiskyEvents(Integer riskyEvents) { this.riskyEvents = riskyEvents; }

    public List<CompoundIncidentDTO> getCompoundIncidents() { return compoundIncidents; }
    public void setCompoundIncidents(List<CompoundIncidentDTO> compoundIncidents) { this.compoundIncidents = compoundIncidents; }

    public Boolean getIsRisky() { return isRisky; }
    public void setIsRisky(Boolean isRisky) { this.isRisky = isRisky; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public Double getRiskScore() { return riskScore; }
    public void setRiskScore(Double riskScore) { this.riskScore = riskScore; }

    public String getMitreTechnique() { return mitreTechnique; }
    public void setMitreTechnique(String mitreTechnique) { this.mitreTechnique = mitreTechnique; }

    public String getMitreName() { return mitreName; }
    public void setMitreName(String mitreName) { this.mitreName = mitreName; }

    public String getDeltaDirection() { return deltaDirection; }
    public void setDeltaDirection(String deltaDirection) { this.deltaDirection = deltaDirection; }

    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }
}
