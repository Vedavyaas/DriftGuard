package com.pheonix.authentication.repository;

import com.pheonix.authentication.assets.ProjectManagerDTO;
import com.pheonix.authentication.assets.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    boolean existsByRole(Role role);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<ProjectManagerDTO> findAllByRole(Role role);

    ProjectManagerDTO findProjectedByUsername(String username);
}
