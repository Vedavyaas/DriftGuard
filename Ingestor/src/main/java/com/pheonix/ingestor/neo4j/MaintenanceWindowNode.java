package com.pheonix.ingestor.neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("MaintenanceWindow")
public class MaintenanceWindowNode {
    @Id
    private String windowId;
    
    private String startTime;
    private String endTime;
    private String approvedScope;
    
    // Getters and Setters
    public String getWindowId() { return windowId; }
    public void setWindowId(String windowId) { this.windowId = windowId; }
    
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    
    public String getApprovedScope() { return approvedScope; }
    public void setApprovedScope(String approvedScope) { this.approvedScope = approvedScope; }
}
