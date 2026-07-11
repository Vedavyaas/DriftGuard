package com.pheonix.ingestor.neo4j.repository;

import com.pheonix.ingestor.neo4j.BlastRadiusOutputNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlastRadiusOutputRepository extends Neo4jRepository<BlastRadiusOutputNode, String> {
}
