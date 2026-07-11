package com.pheonix.ingestor.neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("DriftEvent")
public class DriftEventNode {
    @Id
    private String eventId;
    
    private String timestamp;
    private String parameter;
    private String baselineValue;
    private String currentValue;
    private String changeSource;
    private String approvalStatus;
    private String environment;
    private Boolean isMaintenanceWindow;
    private String severity;
    private Boolean isRisky;
    private Boolean predictedRisky;

    @Relationship(type = "AFFECTS", direction = Relationship.Direction.OUTGOING)
    private ControlNode control;

    @Relationship(type = "TRIGGERED_BY", direction = Relationship.Direction.OUTGOING)
    private ActorNode triggeredBy;

    // Getters and Setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getParameter() { return parameter; }
    public void setParameter(String parameter) { this.parameter = parameter; }

    public String getBaselineValue() { return baselineValue; }
    public void setBaselineValue(String baselineValue) { this.baselineValue = baselineValue; }

    public String getCurrentValue() { return currentValue; }
    public void setCurrentValue(String currentValue) { this.currentValue = currentValue; }

    public String getChangeSource() { return changeSource; }
    public void setChangeSource(String changeSource) { this.changeSource = changeSource; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public Boolean getIsMaintenanceWindow() { return isMaintenanceWindow; }
    public void setIsMaintenanceWindow(Boolean maintenanceWindow) { isMaintenanceWindow = maintenanceWindow; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public Boolean getIsRisky() { return isRisky; }
    public void setIsRisky(Boolean risky) { isRisky = risky; }

    public Boolean getPredictedRisky() { return predictedRisky; }
    public void setPredictedRisky(Boolean predictedRisky) { this.predictedRisky = predictedRisky; }

    public ControlNode getControl() { return control; }
    public void setControl(ControlNode control) { this.control = control; }

    public ActorNode getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(ActorNode triggeredBy) { this.triggeredBy = triggeredBy; }
}
