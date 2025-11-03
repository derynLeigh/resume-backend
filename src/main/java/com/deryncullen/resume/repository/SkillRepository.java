package com.deryncullen.resume.repository;

import com.deryncullen.resume.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Skill entity
 * Provides data access methods for skills with category filtering
 */
@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    /**
     * Find all skills for a profile, ordered by display order
     */
    List<Skill> findByProfileIdOrderByDisplayOrder(Long profileId);

    /**
     * Find skill by ID and profile ID
     */
    Optional<Skill> findByIdAndProfileId(Long id, Long profileId);

    /**
     * Find skills by profile and category
     */
    List<Skill> findByProfileIdAndCategory(Long profileId, Skill.SkillCategory category);

    /**
     * Find primary skills for a profile
     */
    List<Skill> findByProfileIdAndPrimaryTrue(Long profileId);

    /**
     * Get maximum display order for a profile's skills
     */
    @Query("SELECT MAX(s.displayOrder) FROM Skill s WHERE s.profile.id = :profileId")
    Optional<Integer> findMaxDisplayOrderByProfileId(@Param("profileId") Long profileId);

    /**
     * Count skills for a profile
     */
    long countByProfileId(Long profileId);

    /**
     * Count skills by category for a profile
     */
    long countByProfileIdAndCategory(Long profileId, Skill.SkillCategory category);

    /**
     * Delete all skills for a profile
     */
    void deleteByProfileId(Long profileId);

    /**
     * Check if skill with name exists for profile
     */
    boolean existsByProfileIdAndNameIgnoreCase(Long profileId, String name);
}