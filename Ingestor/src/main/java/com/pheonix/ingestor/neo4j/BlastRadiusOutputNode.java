package com.pheonix.ingestor.neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Node("BlastRadiusOutput")
public class BlastRadiusOutputNode {
    @Id
    private String blastId;
    
    private String computedSeverity;
    private String timestamp;
    private String mlNarrative;

    @Relationship(type = "INCLUDES", direction = Relationship.Direction.OUTGOING)
    private List<DriftEventNode> includedEvents;
    
    // Getters and Setters
    public String getBlastId() { return blastId; }
    public void setBlastId(String blastId) { this.blastId = blastId; }

    public String getComputedSeverity() { return computedSeverity; }
    public void setComputedSeverity(String computedSeverity) { this.computedSeverity = computedSeverity; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getMlNarrative() { return mlNarrative; }
    public void setMlNarrative(String mlNarrative) { this.mlNarrative = mlNarrative; }

    public List<DriftEventNode> getIncludedEvents() { return includedEvents; }
    public void setIncludedEvents(List<DriftEventNode> includedEvents) { this.includedEvents = includedEvents; }
}
