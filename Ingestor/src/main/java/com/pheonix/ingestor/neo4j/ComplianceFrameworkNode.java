package com.pheonix.ingestor.neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("ComplianceFramework")
public class ComplianceFrameworkNode {
    @Id
    private String frameworkId;

    public String getFrameworkId() { return frameworkId; }
    public void setFrameworkId(String frameworkId) { this.frameworkId = frameworkId; }
}
