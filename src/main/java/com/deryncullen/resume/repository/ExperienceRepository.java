package com.deryncullen.resume.repository;

import com.deryncullen.resume.model.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    /**
     * Find all experiences for a profile, ordered by start date descending
     */
    List<Experience> findByProfileIdOrderByStartDateDesc(Long profileId);

    /**
     * Find experience by ID and profile ID
     */
    Optional<Experience> findByIdAndProfileId(Long id, Long profileId);

    /**
     * Find current experiences for a profile
     */
    List<Experience> findByProfileIdAndCurrentTrue(Long profileId);

    /**
     * Get maximum display order for a profile's experiences
     */
    @Query("SELECT MAX(e.displayOrder) FROM Experience e WHERE e.profile.id = :profileId")
    Optional<Integer> findMaxDisplayOrderByProfileId(@Param("profileId") Long profileId);

    /**
     * Count experiences for a profile
     */
    long countByProfileId(Long profileId);

    /**
     * Delete all experiences for a profile
     */
    void deleteByProfileId(Long profileId);
}
