package com.pheonix.ingestor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class CompoundIncidentDTO {
    @JsonProperty("incident_id")
    private String incidentId;
    
    @JsonProperty("max_severity")
    private String maxSeverity;
    
    private List<String> domains;
    
    @JsonProperty("compliance_violations")
    private List<String> complianceViolations;

    @JsonProperty("time_span_minutes")
    private Double timeSpanMinutes;

    @JsonProperty("remediation_steps")
    private List<String> remediationSteps;

    @JsonProperty("analyst_narrative")
    private String analystNarrative;

    @JsonProperty("graph_data")
    private Object graphData;
    
    // Additional fields mapped from Python could be added here
    
    public CompoundIncidentDTO() {}

    public String getIncidentId() { return incidentId; }
    public void setIncidentId(String incidentId) { this.incidentId = incidentId; }

    public String getMaxSeverity() { return maxSeverity; }
    public void setMaxSeverity(String maxSeverity) { this.maxSeverity = maxSeverity; }

    public List<String> getDomains() { return domains; }
    public void setDomains(List<String> domains) { this.domains = domains; }

    public List<String> getComplianceViolations() { return complianceViolations; }
    public void setComplianceViolations(List<String> complianceViolations) { this.complianceViolations = complianceViolations; }

    public Double getTimeSpanMinutes() { return timeSpanMinutes; }
    public void setTimeSpanMinutes(Double timeSpanMinutes) { this.timeSpanMinutes = timeSpanMinutes; }

    public List<String> getRemediationSteps() { return remediationSteps; }
    public void setRemediationSteps(List<String> remediationSteps) { this.remediationSteps = remediationSteps; }

    public String getAnalystNarrative() { return analystNarrative; }
    public void setAnalystNarrative(String analystNarrative) { this.analystNarrative = analystNarrative; }

    public Object getGraphData() { return graphData; }
    public void setGraphData(Object graphData) { this.graphData = graphData; }
}
