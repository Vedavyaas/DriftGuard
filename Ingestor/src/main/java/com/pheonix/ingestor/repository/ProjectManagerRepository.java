package com.pheonix.ingestor.repository;

import com.pheonix.ingestor.assets.ProjectDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectManagerRepository extends JpaRepository<ProjectManagerEntity, Long> {
    boolean existsByProjectName(String s);

    List<ProjectDTO> findAllByManagerName(String managerName);
}
