package com.pheonix.ingestor.neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("DriftEvent")
public class DriftEventNode {
    @Id
    private String eventId;
    
    private String timestamp;
    private String currentValue;
    private String changeSource;
    private String approvalStatus;
    private Boolean isRisky;
    private String severity;

    @Relationship(type = "AFFECTS_CONTROL", direction = Relationship.Direction.OUTGOING)
    private ControlNode control;

    @Relationship(type = "OCCURRED_DURING", direction = Relationship.Direction.OUTGOING)
    private MaintenanceWindowNode maintenanceWindow;
    
    // Getters and Setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getCurrentValue() { return currentValue; }
    public void setCurrentValue(String currentValue) { this.currentValue = currentValue; }

    public String getChangeSource() { return changeSource; }
    public void setChangeSource(String changeSource) { this.changeSource = changeSource; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public Boolean getIsRisky() { return isRisky; }
    public void setIsRisky(Boolean isRisky) { this.isRisky = isRisky; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public ControlNode getControl() { return control; }
    public void setControl(ControlNode control) { this.control = control; }

    public MaintenanceWindowNode getMaintenanceWindow() { return maintenanceWindow; }
    public void setMaintenanceWindow(MaintenanceWindowNode maintenanceWindow) { this.maintenanceWindow = maintenanceWindow; }
}
