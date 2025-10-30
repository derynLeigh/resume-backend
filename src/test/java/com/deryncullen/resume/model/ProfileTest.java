package com.deryncullen.resume.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Profile Entity Tests")
class ProfileTest {

    private Validator validator;
    private Profile profile;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        profile = new Profile();
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should fail validation when required fields are null")
        void shouldFailValidationWhenRequiredFieldsAreNull() {
            // Given
            profile.setFirstName(null);
            profile.setLastName(null);
            profile.setEmail(null);
            profile.setTitle(null);

            // When
            Set<ConstraintViolation<Profile>> violations = validator.validate(profile);

            // Then
            assertThat(violations).hasSize(4);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactlyInAnyOrder(
                            "First name is required",
                            "Last name is required",
                            "Email is required",
                            "Professional title is required"
                    );
        }

        @Test
        @DisplayName("Should pass validation with valid data")
        void shouldPassValidationWithValidData() {
            // Given
            profile.setFirstName("Deryn");
            profile.setLastName("Cullen");
            profile.setEmail("derynleigh.cullen@icloud.com");
            profile.setPhone("07789557513");
            profile.setLocation("Bradford, BD12 9HA");
            profile.setLinkedInUrl("https://linkedin.com/in/deryncullen");
            profile.setTitle("Technical Product Owner");
            profile.setSummary("Dynamic technology professional...");

            // When
            Set<ConstraintViolation<Profile>> violations = validator.validate(profile);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation with invalid email format")
        void shouldFailValidationWithInvalidEmail() {
            // Given
            profile.setFirstName("Deryn");
            profile.setLastName("Cullen");
            profile.setEmail("invalid-email");
            profile.setTitle("Technical Product Owner");

            // When
            Set<ConstraintViolation<Profile>> violations = validator.validate(profile);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Email must be valid");
        }

        @Test
        @DisplayName("Should fail validation with invalid URL format")
        void shouldFailValidationWithInvalidUrl() {
            // Given
            profile.setFirstName("Deryn");
            profile.setLastName("Cullen");
            profile.setEmail("deryn@example.com");
            profile.setTitle("Technical Product Owner");
            profile.setLinkedInUrl("not-a-url");

            // When
            Set<ConstraintViolation<Profile>> violations = validator.validate(profile);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("LinkedIn URL must be valid");
        }
    }

    @Nested
    @DisplayName("Entity Behavior Tests")
    class EntityBehaviorTests {

        @Test
        @DisplayName("Should set creation timestamp on new profile")
        void shouldSetCreationTimestamp() {
            // Given
            LocalDateTime before = LocalDateTime.now();

            // When
            profile.onCreate();

            // Then
            assertThat(profile.getCreatedAt()).isNotNull();
            assertThat(profile.getCreatedAt()).isAfterOrEqualTo(before);
            assertThat(profile.getUpdatedAt()).isNotNull();
            assertThat(profile.getUpdatedAt()).isEqualTo(profile.getCreatedAt());
        }

        @Test
        @DisplayName("Should update timestamp on profile update")
        void shouldUpdateTimestamp() throws InterruptedException {
            // Given
            profile.onCreate();
            LocalDateTime createdAt = profile.getCreatedAt();
            Thread.sleep(10); // Ensure time difference

            // When
            profile.onUpdate();

            // Then
            assertThat(profile.getUpdatedAt()).isAfter(createdAt);
            assertThat(profile.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("Should correctly build full name")
        void shouldBuildFullName() {
            // Given
            profile.setFirstName("Deryn");
            profile.setLastName("Cullen");

            // When
            String fullName = profile.getFullName();

            // Then
            assertThat(fullName).isEqualTo("Deryn Cullen");
        }

        @Test
        @DisplayName("Should mark profile as active by default")
        void shouldBeActiveByDefault() {
            // Given/When
            Profile newProfile = new Profile();

            // Then
            assertThat(newProfile.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should build profile using builder pattern")
        void shouldBuildProfileUsingBuilder() {
            // Given/When
            Profile builtProfile = Profile.builder()
                    .firstName("Deryn")
                    .lastName("Cullen")
                    .email("derynleigh.cullen@icloud.com")
                    .phone("07789557513")
                    .location("Bradford, BD12 9HA")
                    .linkedInUrl("https://linkedin.com/in/deryncullen")
                    .githubUrl("https://github.com/deryncullen")
                    .title("Technical Product Owner")
                    .summary("Dynamic technology professional...")
                    .active(true)
                    .build();

            // Then
            assertThat(builtProfile.getFirstName()).isEqualTo("Deryn");
            assertThat(builtProfile.getLastName()).isEqualTo("Cullen");
            assertThat(builtProfile.getEmail()).isEqualTo("derynleigh.cullen@icloud.com");
            assertThat(builtProfile.isActive()).isTrue();
        }
    }
}