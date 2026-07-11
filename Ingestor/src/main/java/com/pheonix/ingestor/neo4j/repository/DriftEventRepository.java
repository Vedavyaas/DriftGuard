package com.pheonix.ingestor.neo4j.repository;

import com.pheonix.ingestor.neo4j.DriftEventNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriftEventRepository extends Neo4jRepository<DriftEventNode, String> {
}
