package com.deryncullen.resume.repository;

import com.deryncullen.resume.model.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Education entity
 * Provides data access methods for education records
 */
@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {

    /**
     * Find all education records for a profile, ordered by graduation date descending
     */
    List<Education> findByProfileIdOrderByGraduationDateDesc(Long profileId);

    /**
     * Find education by ID and profile ID
     */
    Optional<Education> findByIdAndProfileId(Long id, Long profileId);

    /**
     * Get maximum display order for a profile's education records
     */
    @Query("SELECT MAX(e.displayOrder) FROM Education e WHERE e.profile.id = :profileId")
    Optional<Integer> findMaxDisplayOrderByProfileId(@Param("profileId") Long profileId);

    /**
     * Count education records for a profile
     */
    long countByProfileId(Long profileId);

    /**
     * Delete all education records for a profile
     */
    void deleteByProfileId(Long profileId);
}