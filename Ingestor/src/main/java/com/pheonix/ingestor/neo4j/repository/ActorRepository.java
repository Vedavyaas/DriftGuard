package com.pheonix.ingestor.neo4j.repository;

import com.pheonix.ingestor.neo4j.ActorNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActorRepository extends Neo4jRepository<ActorNode, String> {
}
