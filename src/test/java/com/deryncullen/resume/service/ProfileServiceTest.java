package com.deryncullen.resume.service;

import com.deryncullen.resume.dto.ProfileDTO;
import com.deryncullen.resume.dto.CreateProfileRequest;
import com.deryncullen.resume.dto.UpdateProfileRequest;
import com.deryncullen.resume.exception.ResourceNotFoundException;
import com.deryncullen.resume.exception.DuplicateResourceException;
import com.deryncullen.resume.model.Profile;
import com.deryncullen.resume.repository.ProfileRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileService Tests")
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ProfileMapper profileMapper;

    @InjectMocks
    private ProfileService profileService;

    private Profile testProfile;
    private ProfileDTO testProfileDTO;
    private CreateProfileRequest createRequest;
    private UpdateProfileRequest updateRequest;

    @BeforeEach
    void setUp() {
        testProfile = Profile.builder()
                .id(1L)
                .firstName("Deryn")
                .lastName("Cullen")
                .email("deryn@example.com")
                .title("Technical Product Owner")
                .active(true)
                .build();

        testProfileDTO = ProfileDTO.builder()
                .id(1L)
                .firstName("Deryn")
                .lastName("Cullen")
                .email("deryn@example.com")
                .title("Technical Product Owner")
                .active(true)
                .build();

        createRequest = CreateProfileRequest.builder()
                .firstName("Deryn")
                .lastName("Cullen")
                .email("deryn@example.com")
                .title("Technical Product Owner")
                .build();

        updateRequest = UpdateProfileRequest.builder()
                .firstName("Deryn")
                .lastName("Cullen")
                .email("deryn@example.com")
                .title("Senior Technical Product Owner")
                .build();
    }

    @Nested
    @DisplayName("Create Profile Tests")
    class CreateProfileTests {

        @Test
        @DisplayName("Should create profile successfully")
        void shouldCreateProfile() {
            // Given
            when(profileRepository.existsByEmail(anyString())).thenReturn(false);
            when(profileMapper.toEntity(any(CreateProfileRequest.class))).thenReturn(testProfile);
            when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);
            when(profileMapper.toDto(any(Profile.class))).thenReturn(testProfileDTO);

            // When
            ProfileDTO result = profileService.createProfile(createRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("deryn@example.com");
            verify(profileRepository).existsByEmail("deryn@example.com");
            verify(profileRepository).save(any(Profile.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            // Given
            when(profileRepository.existsByEmail(anyString())).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> profileService.createProfile(createRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessage("Profile with email deryn@example.com already exists");

            verify(profileRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Get Profile Tests")
    class GetProfileTests {

        @Test
        @DisplayName("Should get profile by ID")
        void shouldGetProfileById() {
            // Given
            when(profileRepository.findById(1L)).thenReturn(Optional.of(testProfile));
            when(profileMapper.toDto(any(Profile.class))).thenReturn(testProfileDTO);

            // When
            ProfileDTO result = profileService.getProfileById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getEmail()).isEqualTo("deryn@example.com");
        }

        @Test
        @DisplayName("Should throw exception when profile not found")
        void shouldThrowExceptionWhenProfileNotFound() {
            // Given
            when(profileRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> profileService.getProfileById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Profile not found with id: 999");
        }

        @Test
        @DisplayName("Should get profile by email")
        void shouldGetProfileByEmail() {
            // Given
            when(profileRepository.findByEmail("deryn@example.com")).thenReturn(Optional.of(testProfile));
            when(profileMapper.toDto(any(Profile.class))).thenReturn(testProfileDTO);

            // When
            ProfileDTO result = profileService.getProfileByEmail("deryn@example.com");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("deryn@example.com");
        }

        @Test
        @DisplayName("Should get all active profiles")
        void shouldGetAllActiveProfiles() {
            // Given
            List<Profile> profiles = Arrays.asList(testProfile);
            when(profileRepository.findByActiveTrue()).thenReturn(profiles);
            when(profileMapper.toDto(any(Profile.class))).thenReturn(testProfileDTO);

            // When
            List<ProfileDTO> result = profileService.getAllActiveProfiles();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).isActive()).isTrue();
        }

        @Test
        @DisplayName("Should get profile with all relations")
        void shouldGetProfileWithAllRelations() {
            // Given
            when(profileRepository.findById(1L)).thenReturn(Optional.of(testProfile));
            when(profileRepository.findByIdWithExperiences(1L)).thenReturn(Optional.of(testProfile));
            when(profileRepository.findByIdWithEducations(1L)).thenReturn(Optional.of(testProfile));
            when(profileRepository.findByIdWithSkills(1L)).thenReturn(Optional.of(testProfile));
            when(profileRepository.findByIdWithCertifications(1L)).thenReturn(Optional.of(testProfile));
            when(profileMapper.toDto(any(Profile.class))).thenReturn(testProfileDTO);

            // When
            ProfileDTO result = profileService.getProfileWithAllRelations(1L);

            // Then
            assertThat(result).isNotNull();
            verify(profileRepository).findById(1L);
            verify(profileRepository).findByIdWithExperiences(1L);
            verify(profileRepository).findByIdWithEducations(1L);
            verify(profileRepository).findByIdWithSkills(1L);
            verify(profileRepository).findByIdWithCertifications(1L);
        }
    }

    @Nested
    @DisplayName("Update Profile Tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update profile successfully")
        void shouldUpdateProfile() {
            // Given
            when(profileRepository.findById(1L)).thenReturn(Optional.of(testProfile));
            when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);
            when(profileMapper.toDto(any(Profile.class))).thenReturn(testProfileDTO);

            // When
            ProfileDTO result = profileService.updateProfile(1L, updateRequest);

            // Then
            assertThat(result).isNotNull();
            verify(profileRepository).findById(1L);
            verify(profileRepository).save(any(Profile.class));
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent profile")
        void shouldThrowExceptionWhenUpdatingNonExistentProfile() {
            // Given
            when(profileRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> profileService.updateProfile(999L, updateRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Profile not found with id: 999");

            verify(profileRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when changing email to existing one")
        void shouldThrowExceptionWhenEmailAlreadyTaken() {
            // Given
            Profile existingProfile = Profile.builder()
                    .id(2L)
                    .email("existing@example.com")
                    .build();

            testProfile.setEmail("old@example.com");
            updateRequest.setEmail("existing@example.com");

            when(profileRepository.findById(1L)).thenReturn(Optional.of(testProfile));
            when(profileRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingProfile));

            // When/Then
            assertThatThrownBy(() -> profileService.updateProfile(1L, updateRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessage("Email existing@example.com is already in use");
        }
    }

    @Nested
    @DisplayName("Delete Profile Tests")
    class DeleteProfileTests {

        @Test
        @DisplayName("Should delete profile successfully")
        void shouldDeleteProfile() {
            // Given
            when(profileRepository.existsById(1L)).thenReturn(true);

            // When
            profileService.deleteProfile(1L);

            // Then
            verify(profileRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent profile")
        void shouldThrowExceptionWhenDeletingNonExistentProfile() {
            // Given
            when(profileRepository.existsById(999L)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> profileService.deleteProfile(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Profile not found with id: 999");

            verify(profileRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should soft delete profile")
        void shouldSoftDeleteProfile() {
            // Given
            when(profileRepository.findById(1L)).thenReturn(Optional.of(testProfile));
            when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

            // When
            profileService.softDeleteProfile(1L);

            // Then
            assertThat(testProfile.isActive()).isFalse();
            verify(profileRepository).save(testProfile);
        }
    }
}