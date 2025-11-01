package com.deryncullen.resume.repository;

import com.deryncullen.resume.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByEmail(String email);

    List<Profile> findByActiveTrue();

    boolean existsByEmail(String email);

    @Query("SELECT p FROM Profile p LEFT JOIN FETCH p.experiences WHERE p.id = :id")
    Optional<Profile> findByIdWithExperiences(@Param("id") Long id);

    @Query("SELECT p FROM Profile p LEFT JOIN FETCH p.educations WHERE p.id = :id")
    Optional<Profile> findByIdWithEducations(@Param("id") Long id);

    @Query("SELECT p FROM Profile p LEFT JOIN FETCH p.skills WHERE p.id = :id")
    Optional<Profile> findByIdWithSkills(@Param("id") Long id);

    @Query("SELECT p FROM Profile p LEFT JOIN FETCH p.certifications WHERE p.id = :id")
    Optional<Profile> findByIdWithCertifications(@Param("id") Long id);

    // Removed findByIdWithAllRelations - use separate queries in service layer instead

    @Query("SELECT DISTINCT p FROM Profile p WHERE p.active = true")
    List<Profile> findAllActiveWithRelations();
}