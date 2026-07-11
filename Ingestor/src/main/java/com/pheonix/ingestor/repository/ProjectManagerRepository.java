package com.pheonix.ingestor.repository;

import com.pheonix.ingestor.assets.ProjectDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectManagerRepository extends JpaRepository<ProjectManagerEntity, Long> {
    boolean existsByProjectName(String s);

    Optional<ProjectManagerEntity> findByProjectHash(String projectHash);

    List<ProjectDTO> findAllByManagerName(String managerName);
}
