package com.pheonix.ingestor.neo4j.repository;

import com.pheonix.ingestor.neo4j.MaintenanceWindowNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceWindowRepository extends Neo4jRepository<MaintenanceWindowNode, String> {
}
