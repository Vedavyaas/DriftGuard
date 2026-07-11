package com.pheonix.ingestor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DriftEventDTO {

    private String projectHash;
    private String provider;

    @JsonProperty("event_id")
    private String eventId;

    @JsonProperty("control_id")
    private String controlId;

    private String timestamp;

    @JsonProperty("changed_by")
    private String changedBy;

    @JsonProperty("change_source")
    private String changeSource;

    @JsonProperty("approval_status")
    private String approvalStatus;

    private String environment;
    private String severity;
    private String parameter;

    @JsonProperty("old_value")
    private Object oldValue;

    @JsonProperty("new_value")
    private Object newValue;

    private String domain;
    private String system;

    @JsonProperty("maintenance_window")
    private Boolean maintenanceWindow;

    public DriftEventDTO() {
    }

    public String getProjectHash() { return projectHash; }
    public void setProjectHash(String projectHash) { this.projectHash = projectHash; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getControlId() { return controlId; }
    public void setControlId(String controlId) { this.controlId = controlId; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public String getChangeSource() { return changeSource; }
    public void setChangeSource(String changeSource) { this.changeSource = changeSource; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getParameter() { return parameter; }
    public void setParameter(String parameter) { this.parameter = parameter; }

    public Object getOldValue() { return oldValue; }
    public void setOldValue(Object oldValue) { this.oldValue = oldValue; }

    public Object getNewValue() { return newValue; }
    public void setNewValue(Object newValue) { this.newValue = newValue; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getSystem() { return system; }
    public void setSystem(String system) { this.system = system; }

    public Boolean getMaintenanceWindow() { return maintenanceWindow; }
    public void setMaintenanceWindow(Boolean maintenanceWindow) { this.maintenanceWindow = maintenanceWindow; }
}
