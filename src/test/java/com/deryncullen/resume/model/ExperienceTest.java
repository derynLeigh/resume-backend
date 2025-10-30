package com.deryncullen.resume.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Experience Entity Tests")
class ExperienceTest {

    private Validator validator;
    private Experience experience;
    private Profile profile;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        experience = new Experience();
        profile = Profile.builder()
                .firstName("Deryn")
                .lastName("Cullen")
                .email("deryn@example.com")
                .title("Technical Product Owner")
                .build();
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should fail validation when required fields are null")
        void shouldFailValidationWhenRequiredFieldsAreNull() {
            // Given
            experience.setCompanyName(null);
            experience.setJobTitle(null);
            experience.setStartDate(null);

            // When
            Set<ConstraintViolation<Experience>> violations = validator.validate(experience);

            // Then
            assertThat(violations).hasSize(3);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactlyInAnyOrder(
                            "Company name is required",
                            "Job title is required",
                            "Start date is required"
                    );
        }

        @Test
        @DisplayName("Should pass validation with valid data")
        void shouldPassValidationWithValidData() {
            // Given
            experience.setCompanyName("Sky");
            experience.setJobTitle("Technical Product Owner");
            experience.setLocation("Leeds, LS12 1BE");
            experience.setStartDate(LocalDate.of(2023, 2, 1));
            experience.setCurrent(true);
            experience.setDescription("Leading digital identity platform...");

            // When
            Set<ConstraintViolation<Experience>> violations = validator.validate(experience);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation when end date is before start date")
        void shouldFailValidationWhenEndDateBeforeStartDate() {
            // Given
            experience.setCompanyName("Sky");
            experience.setJobTitle("Software Engineer");
            experience.setStartDate(LocalDate.of(2023, 2, 1));
            experience.setEndDate(LocalDate.of(2022, 12, 1));

            // When
            Set<ConstraintViolation<Experience>> violations = validator.validate(experience);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("End date must be after start date");
        }

        @Test
        @DisplayName("Should fail validation when future dates are used")
        void shouldFailValidationWithFutureDates() {
            // Given
            experience.setCompanyName("Sky");
            experience.setJobTitle("Technical Product Owner");
            experience.setStartDate(LocalDate.now().plusDays(1));

            // When
            Set<ConstraintViolation<Experience>> violations = validator.validate(experience);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Start date cannot be in the future");
        }
    }

    @Nested
    @DisplayName("Entity Behavior Tests")
    class EntityBehaviorTests {

        @Test
        @DisplayName("Should calculate duration for current position")
        void shouldCalculateDurationForCurrentPosition() {
            // Given
            experience.setStartDate(LocalDate.now().minusYears(2).minusMonths(3));
            experience.setCurrent(true);
            experience.setEndDate(null);

            // When
            String duration = experience.getDuration();

            // Then
            assertThat(duration).matches("\\d+ years?, \\d+ months?");
        }

        @Test
        @DisplayName("Should calculate duration for past position")
        void shouldCalculateDurationForPastPosition() {
            // Given
            experience.setStartDate(LocalDate.of(2020, 1, 1));
            experience.setEndDate(LocalDate.of(2022, 6, 30));
            experience.setCurrent(false);

            // When
            String duration = experience.getDuration();

            // Then
            assertThat(duration).isEqualTo("2 years, 6 months");
        }

        @Test
        @DisplayName("Should format date range correctly")
        void shouldFormatDateRange() {
            // Given
            experience.setStartDate(LocalDate.of(2023, 2, 1));
            experience.setCurrent(true);

            // When
            String dateRange = experience.getFormattedDateRange();

            // Then
            assertThat(dateRange).isEqualTo("Feb 2023 - Present");
        }

        @Test
        @DisplayName("Should handle achievements list")
        void shouldHandleAchievementsList() {
            // Given
            experience.addAchievement("Led team of 5 engineers");
            experience.addAchievement("Improved velocity by 30%");
            experience.addAchievement("Earned PSPO I certification");

            // Then
            assertThat(experience.getAchievements()).hasSize(3);
            assertThat(experience.getAchievements()).containsExactly(
                    "Led team of 5 engineers",
                    "Improved velocity by 30%",
                    "Earned PSPO I certification"
            );
        }

        @Test
        @DisplayName("Should handle technologies list")
        void shouldHandleTechnologiesList() {
            // Given
            experience.setTechnologies(Arrays.asList("Java", "Spring Boot", "React", "PostgreSQL"));

            // Then
            assertThat(experience.getTechnologies()).hasSize(4);
            assertThat(experience.getTechnologiesAsString()).isEqualTo("Java, Spring Boot, React, PostgreSQL");
        }

        @Test
        @DisplayName("Should enforce end date null when position is current")
        void shouldEnforceEndDateNullWhenCurrent() {
            // Given
            experience.setStartDate(LocalDate.of(2023, 1, 1));
            experience.setEndDate(LocalDate.of(2024, 1, 1));

            // When
            experience.setCurrent(true);

            // Then
            assertThat(experience.getEndDate()).isNull();
        }
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should build experience using builder pattern")
        void shouldBuildExperienceUsingBuilder() {
            // Given/When
            Experience builtExperience = Experience.builder()
                    .companyName("Sky")
                    .jobTitle("Technical Product Owner")
                    .location("Leeds, LS12 1BE")
                    .startDate(LocalDate.of(2025, 1, 1))
                    .current(true)
                    .description("Leading digital identity platform")
                    .achievements(Arrays.asList("Achievement 1", "Achievement 2"))
                    .technologies(Arrays.asList("Java", "Spring Boot"))
                    .profile(profile)
                    .build();

            // Then
            assertThat(builtExperience.getCompanyName()).isEqualTo("Sky");
            assertThat(builtExperience.getJobTitle()).isEqualTo("Technical Product Owner");
            assertThat(builtExperience.isCurrent()).isTrue();
            assertThat(builtExperience.getEndDate()).isNull();
            assertThat(builtExperience.getAchievements()).hasSize(2);
            assertThat(builtExperience.getTechnologies()).hasSize(2);
        }
    }
}