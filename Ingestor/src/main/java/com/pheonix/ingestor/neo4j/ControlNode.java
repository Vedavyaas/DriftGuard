package com.pheonix.ingestor.neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Node("Control")
public class ControlNode {
    @Id
    private String controlId;
    
    @Property("project_hash")
    private String projectHash;
    
    private String domain;
    private String system;
    private String environment;
    private String severityIfDrifted;

    @Relationship(type = "MAPPED_TO", direction = Relationship.Direction.OUTGOING)
    private List<ComplianceFrameworkNode> complianceMappings;

    // Getters and Setters
    public String getControlId() { return controlId; }
    public void setControlId(String controlId) { this.controlId = controlId; }

    public String getProjectHash() { return projectHash; }
    public void setProjectHash(String projectHash) { this.projectHash = projectHash; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getSystem() { return system; }
    public void setSystem(String system) { this.system = system; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public String getSeverityIfDrifted() { return severityIfDrifted; }
    public void setSeverityIfDrifted(String severityIfDrifted) { this.severityIfDrifted = severityIfDrifted; }

    public List<ComplianceFrameworkNode> getComplianceMappings() { return complianceMappings; }
    public void setComplianceMappings(List<ComplianceFrameworkNode> complianceMappings) { this.complianceMappings = complianceMappings; }
}
