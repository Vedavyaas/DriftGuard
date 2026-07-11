package com.pheonix.ingestor.repository;

import com.pheonix.ingestor.model.RawDriftEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface RawDriftEventRepository extends JpaRepository<RawDriftEvent, Long> {

    /** Latest N events for a project, newest first. */
    List<RawDriftEvent> findTop100ByProjectHashOrderByReceivedAtDesc(String projectHash);

    /** Count events for a project (used to decide if cleanup is needed). */
    long countByProjectHash(String projectHash);

    /** Delete oldest events for a project, keeping only the newest N. */
    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM raw_drift_events
            WHERE project_hash = :hash
            AND id NOT IN (
                SELECT id FROM raw_drift_events
                WHERE project_hash = :hash
                ORDER BY received_at DESC
                LIMIT :keep
            )
            """, nativeQuery = true)
    void deleteOldestBeyondLimit(@Param("hash") String projectHash, @Param("keep") int keep);
}
