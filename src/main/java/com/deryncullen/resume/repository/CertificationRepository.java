package com.deryncullen.resume.repository;

import com.deryncullen.resume.model.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Certification entity
 * Provides data access methods for certifications with expiration tracking
 */
@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {

    /**
     * Find all certifications for a profile, ordered by date obtained descending
     */
    List<Certification> findByProfileIdOrderByDateObtainedDesc(Long profileId);

    /**
     * Find certification by ID and profile ID
     */
    Optional<Certification> findByIdAndProfileId(Long id, Long profileId);

    /**
     * Find certifications that don't expire
     */
    List<Certification> findByProfileIdAndDoesNotExpireTrue(Long profileId);

    /**
     * Check if a certification already exists for a profile
     * Used to prevent duplicate certifications
     */
    boolean existsByProfileIdAndNameAndIssuingOrganization(
            Long profileId,
            String name,
            String issuingOrganization
    );

    /**
     * Find certifications by issuing organization
     * Useful for grouping certifications by vendor (e.g. all AWS certs)
     */
    List<Certification> findByProfileIdAndIssuingOrganization(
            Long profileId,
            String issuingOrganization
    );

    /**
     * Find certifications ordered by display order, then by date
     * For consistent UI presentation
     */
    List<Certification> findByProfileIdOrderByDisplayOrderAscDateObtainedDesc(Long profileId);

    /**
     * Find certifications expiring before a certain date
     */
    @Query("SELECT c FROM Certification c WHERE c.profile.id = :profileId " +
            "AND c.doesNotExpire = false AND c.expirationDate < :date")
    List<Certification> findExpiringBefore(@Param("profileId") Long profileId,
                                           @Param("date") LocalDate date);

    /**
     * Find expired certifications
     */
    @Query("SELECT c FROM Certification c WHERE c.profile.id = :profileId " +
            "AND c.doesNotExpire = false AND c.expirationDate < :currentDate")
    List<Certification> findExpired(@Param("profileId") Long profileId,
                                    @Param("currentDate") LocalDate currentDate);

    /**
     * Find certifications expiring soon (within next 3 months)
     */
    @Query("SELECT c FROM Certification c WHERE c.profile.id = :profileId " +
            "AND c.doesNotExpire = false " +
            "AND c.expirationDate > :currentDate " +
            "AND c.expirationDate <= :threeMonthsLater")
    List<Certification> findExpiringSoon(@Param("profileId") Long profileId,
                                         @Param("currentDate") LocalDate currentDate,
                                         @Param("threeMonthsLater") LocalDate threeMonthsLater);

    /**
     * Find valid (non-expired) certifications
     */
    @Query("SELECT c FROM Certification c WHERE c.profile.id = :profileId " +
            "AND (c.doesNotExpire = true OR c.expirationDate >= :currentDate)")
    List<Certification> findValid(@Param("profileId") Long profileId,
                                  @Param("currentDate") LocalDate currentDate);

    /**
     * Get maximum display order for a profile's certifications
     */
    @Query("SELECT MAX(c.displayOrder) FROM Certification c WHERE c.profile.id = :profileId")
    Optional<Integer> findMaxDisplayOrderByProfileId(@Param("profileId") Long profileId);

    /**
     * Count certifications for a profile
     */
    long countByProfileId(Long profileId);

    /**
     * Delete all certifications for a profile
     */
    void deleteByProfileId(Long profileId);
}