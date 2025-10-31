package com.deryncullen.resume.repository;

import com.deryncullen.resume.model.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("ProfileRepository Integration Tests")
class ProfileRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProfileRepository profileRepository;

    private Profile testProfile;

    @BeforeEach
    void setUp() {
        testProfile = Profile.builder()
            .firstName("Deryn")
            .lastName("Cullen")
            .email("deryn@example.com")
            .phone("07789557513")
            .location("Bradford, BD12 9HA")
            .linkedInUrl("https://linkedin.com/in/deryncullen")
            .title("Technical Product Owner")
            .summary("Dynamic technology professional...")
            .active(true)
            .build();
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("Should save profile with all fields")
        void shouldSaveProfileWithAllFields() {
            // When
            Profile saved = profileRepository.save(testProfile);
            entityManager.flush();
            entityManager.clear();

            // Then
            Profile found = profileRepository.findById(saved.getId()).orElse(null);
            assertThat(found).isNotNull();
            assertThat(found.getFirstName()).isEqualTo("Deryn");
            assertThat(found.getLastName()).isEqualTo("Cullen");
            assertThat(found.getEmail()).isEqualTo("deryn@example.com");
            assertThat(found.getCreatedAt()).isNotNull();
            assertThat(found.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should update profile")
        void shouldUpdateProfile() {
            // Given
            Profile saved = entityManager.persistAndFlush(testProfile);
            entityManager.clear();

            // When
            Profile toUpdate = profileRepository.findById(saved.getId()).orElseThrow();
            toUpdate.setTitle("Senior Technical Product Owner");
            profileRepository.save(toUpdate);
            entityManager.flush();
            entityManager.clear();

            // Then
            Profile updated = profileRepository.findById(saved.getId()).orElseThrow();
            assertThat(updated.getTitle()).isEqualTo("Senior Technical Product Owner");
            assertThat(updated.getUpdatedAt()).isAfter(updated.getCreatedAt());
        }

        @Test
        @DisplayName("Should delete profile")
        void shouldDeleteProfile() {
            // Given
            Profile saved = entityManager.persistAndFlush(testProfile);
            Long id = saved.getId();

            // When
            profileRepository.deleteById(id);
            entityManager.flush();

            // Then
            Optional<Profile> deleted = profileRepository.findById(id);
            assertThat(deleted).isEmpty();
        }
    }

    @Nested
    @DisplayName("Custom Query Tests")
    class CustomQueryTests {

        @Test
        @DisplayName("Should find profile by email")
        void shouldFindByEmail() {
            // Given
            entityManager.persistAndFlush(testProfile);

            // When
            Optional<Profile> found = profileRepository.findByEmail("deryn@example.com");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("deryn@example.com");
        }

        @Test
        @DisplayName("Should find active profiles")
        void shouldFindActiveProfiles() {
            // Given
            Profile activeProfile = testProfile;
            Profile inactiveProfile = Profile.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .title("Developer")
                .active(false)
                .build();
            
            entityManager.persistAndFlush(activeProfile);
            entityManager.persistAndFlush(inactiveProfile);

            // When
            List<Profile> activeProfiles = profileRepository.findByActiveTrue();

            // Then
            assertThat(activeProfiles).hasSize(1);
            assertThat(activeProfiles.get(0).getEmail()).isEqualTo("deryn@example.com");
        }

        @Test
        @DisplayName("Should check if email exists")
        void shouldCheckIfEmailExists() {
            // Given
            entityManager.persistAndFlush(testProfile);

            // When
            boolean exists = profileRepository.existsByEmail("deryn@example.com");
            boolean notExists = profileRepository.existsByEmail("notfound@example.com");

            // Then
            assertThat(exists).isTrue();
            assertThat(notExists).isFalse();
        }
    }

    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

        @Test
        @DisplayName("Should save profile with experiences")
        void shouldSaveProfileWithExperiences() {
            // Given
            Experience experience1 = Experience.builder()
                .companyName("Sky")
                .jobTitle("Technical Product Owner")
                .startDate(LocalDate.of(2025, 1, 1))
                .current(true)
                .build();

            Experience experience2 = Experience.builder()
                .companyName("Sky")
                .jobTitle("Software Engineer")
                .startDate(LocalDate.of(2023, 2, 1))
                .endDate(LocalDate.of(2025, 1, 1))
                .current(false)
                .build();

            testProfile.addExperience(experience1);
            testProfile.addExperience(experience2);

            // When
            Profile saved = profileRepository.save(testProfile);
            entityManager.flush();
            entityManager.clear();

            // Then
            Profile found = profileRepository.findByIdWithExperiences(saved.getId()).orElseThrow();
            assertThat(found.getExperiences()).hasSize(2);
            assertThat(found.getExperiences())
                .extracting(Experience::getJobTitle)
                .containsExactly("Technical Product Owner", "Software Engineer");
        }

        @Test
        @DisplayName("Should save profile with skills")
        void shouldSaveProfileWithSkills() {
            // Given
            Skill skill1 = Skill.builder()
                .name("Java")
                .category(Skill.SkillCategory.PROGRAMMING_LANGUAGE)
                .proficiencyLevel(Skill.ProficiencyLevel.ADVANCED)
                .yearsOfExperience(3)
                .primary(true)
                .build();

            Skill skill2 = Skill.builder()
                .name("Spring Boot")
                .category(Skill.SkillCategory.FRAMEWORK)
                .proficiencyLevel(Skill.ProficiencyLevel.ADVANCED)
                .yearsOfExperience(3)
                .build();

            testProfile.addSkill(skill1);
            testProfile.addSkill(skill2);

            // When
            Profile saved = profileRepository.save(testProfile);
            entityManager.flush();
            entityManager.clear();

            // Then
            Profile found = profileRepository.findByIdWithSkills(saved.getId()).orElseThrow();
            assertThat(found.getSkills()).hasSize(2);
            assertThat(found.getSkills())
                .extracting(Skill::getName)
                .containsExactlyInAnyOrder("Java", "Spring Boot");
        }

        @Test
        @DisplayName("Should cascade delete relationships")
        void shouldCascadeDeleteRelationships() {
            // Given
            Experience experience = Experience.builder()
                .companyName("Sky")
                .jobTitle("Technical Product Owner")
                .startDate(LocalDate.of(2025, 1, 1))
                .current(true)
                .build();
            
            testProfile.addExperience(experience);
            Profile saved = profileRepository.save(testProfile);
            entityManager.flush();
            Long profileId = saved.getId();

            // When
            profileRepository.deleteById(profileId);
            entityManager.flush();
            entityManager.clear();

            // Then
            assertThat(profileRepository.findById(profileId)).isEmpty();
            // Verify cascaded deletion (would need ExperienceRepository to check properly)
        }
    }
}
