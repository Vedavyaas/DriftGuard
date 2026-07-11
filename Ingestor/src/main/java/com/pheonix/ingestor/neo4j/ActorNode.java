package com.pheonix.ingestor.neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Node("Actor")
public class ActorNode {
    @Id
    private String actorId;

    @Relationship(type = "CAUSED", direction = Relationship.Direction.OUTGOING)
    private List<DriftEventNode> causedEvents;
    
    // Getters and Setters
    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }
    
    public List<DriftEventNode> getCausedEvents() { return causedEvents; }
    public void setCausedEvents(List<DriftEventNode> causedEvents) { this.causedEvents = causedEvents; }
}
