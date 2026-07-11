package com.pheonix.ingestor.neo4j.repository;

import com.pheonix.ingestor.neo4j.ControlNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ControlRepository extends Neo4jRepository<ControlNode, String> {
}
