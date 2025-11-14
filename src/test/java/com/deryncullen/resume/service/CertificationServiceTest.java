package com.deryncullen.resume.service;

import com.deryncullen.resume.dto.CertificationDTO;
import com.deryncullen.resume.exception.DuplicateResourceException;
import com.deryncullen.resume.exception.ResourceNotFoundException;
import com.deryncullen.resume.model.Certification;
import com.deryncullen.resume.model.Profile;
import com.deryncullen.resume.repository.CertificationRepository;
import com.deryncullen.resume.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CertificationService Tests")
class CertificationServiceTest {

    @Mock
    private CertificationRepository certificationRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ProfileMapper profileMapper;

    @InjectMocks
    private CertificationService certificationService;

    private Profile testProfile;
    private Certification testCertification;
    private CertificationDTO testCertificationDTO;

    @BeforeEach
    void setUp() {
        testProfile = Profile.builder()
                .id(1L)
                .firstName("Deryn")
                .lastName("Cullen")
                .email("deryn@example.com")
                .build();

        testCertification = Certification.builder()
                .id(1L)
                .name("AWS Solutions Architect")
                .issuingOrganization("Amazon Web Services")
                .credentialId("AWS-123456")
                .credentialUrl("https://aws.amazon.com/verify/AWS-123456")
                .dateObtained(LocalDate.now().minusMonths(6))
                .expirationDate(LocalDate.now().plusYears(2))
                .doesNotExpire(false)
                .description("Professional level certification")
                .displayOrder(1)
                .profile(testProfile)
                .build();

        testCertificationDTO = CertificationDTO.builder()
                .id(1L)
                .name("AWS Solutions Architect")
                .issuingOrganization("Amazon Web Services")
                .credentialId("AWS-123456")
                .credentialUrl("https://aws.amazon.com/verify/AWS-123456")
                .dateObtained(LocalDate.now().minusMonths(6))
                .expirationDate(LocalDate.now().plusYears(2))
                .doesNotExpire(false)
                .description("Professional level certification")
                .displayOrder(1)
                .expired(false)
                .expiringSoon(false)
                .build();
    }

    @Nested
    @DisplayName("Add Certification Tests")
    class AddCertificationTests {

        @Test
        @DisplayName("Should add certification to profile successfully")
        void shouldAddCertificationSuccessfully() {
            // Given
            Long profileId = 1L;
            CertificationDTO inputDTO = CertificationDTO.builder()
                    .name("AWS Solutions Architect")
                    .issuingOrganization("Amazon Web Services")
                    .dateObtained(LocalDate.now().minusMonths(6))
                    .build();

            when(profileRepository.findById(profileId)).thenReturn(Optional.of(testProfile));
            when(certificationRepository.existsByProfileIdAndNameAndIssuingOrganization(
                    profileId, inputDTO.getName(), inputDTO.getIssuingOrganization()))
                    .thenReturn(false);
            when(profileMapper.toEntity(inputDTO)).thenReturn(testCertification);
            when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);
            when(profileMapper.toDto(testCertification)).thenReturn(testCertificationDTO);

            // When
            CertificationDTO result = certificationService.addCertification(profileId, inputDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("AWS Solutions Architect");
            assertThat(result.getIssuingOrganization()).isEqualTo("Amazon Web Services");
            verify(certificationRepository).save(any(Certification.class));
        }

        @Test
        @DisplayName("Should throw exception when profile not found")
        void shouldThrowExceptionWhenProfileNotFound() {
            // Given
            Long profileId = 999L;
            CertificationDTO inputDTO = CertificationDTO.builder()
                    .name("AWS Solutions Architect")
                    .issuingOrganization("Amazon Web Services")
                    .build();

            when(profileRepository.findById(profileId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> certificationService.addCertification(profileId, inputDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Profile not found");
        }

        @Test
        @DisplayName("Should throw exception when duplicate certification exists")
        void shouldThrowExceptionWhenDuplicateCertification() {
            // Given
            Long profileId = 1L;
            CertificationDTO inputDTO = CertificationDTO.builder()
                    .name("AWS Solutions Architect")
                    .issuingOrganization("Amazon Web Services")
                    .build();

            when(profileRepository.findById(profileId)).thenReturn(Optional.of(testProfile));
            when(certificationRepository.existsByProfileIdAndNameAndIssuingOrganization(
                    profileId, inputDTO.getName(), inputDTO.getIssuingOrganization()))
                    .thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> certificationService.addCertification(profileId, inputDTO))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("already has this certification");
        }
    }

    @Nested
    @DisplayName("Get Certifications Tests")
    class GetCertificationsTests {

        @Test
        @DisplayName("Should get all certifications for profile")
        void shouldGetAllCertificationsForProfile() {
            // Given
            Long profileId = 1L;
            List<Certification> certifications = Arrays.asList(testCertification);
            List<CertificationDTO> certificationDTOs = Arrays.asList(testCertificationDTO);

            when(profileRepository.existsById(profileId)).thenReturn(true);
            when(certificationRepository.findByProfileIdOrderByDisplayOrderAscDateObtainedDesc(profileId))
                    .thenReturn(certifications);
            when(profileMapper.toCertificationDtoList(certifications)).thenReturn(certificationDTOs);

            // When
            List<CertificationDTO> result = certificationService.getCertificationsByProfileId(profileId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("AWS Solutions Architect");
        }

        @Test
        @DisplayName("Should get expired certifications")
        void shouldGetExpiredCertifications() {
            // Given
            Long profileId = 1L;
            Certification expiredCert = Certification.builder()
                    .id(2L)
                    .name("Expired Cert")
                    .issuingOrganization("Test Org")
                    .dateObtained(LocalDate.now().minusYears(3))
                    .expirationDate(LocalDate.now().minusMonths(1))
                    .doesNotExpire(false)
                    .build();

            List<Certification> expiredCerts = Arrays.asList(expiredCert);
            List<CertificationDTO> expiredDTOs = Arrays.asList(
                    CertificationDTO.builder()
                            .id(2L)
                            .name("Expired Cert")
                            .expired(true)
                            .build()
            );

            when(profileRepository.existsById(profileId)).thenReturn(true);
            when(certificationRepository.findExpired(
                    eq(profileId), any(LocalDate.class)))
                    .thenReturn(expiredCerts);
            when(profileMapper.toCertificationDtoList(expiredCerts)).thenReturn(expiredDTOs);

            // When
            List<CertificationDTO> result = certificationService.getExpiredCertifications(profileId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).isExpired()).isTrue();
        }

        @Test
        @DisplayName("Should get certifications expiring soon")
        void shouldGetCertificationsExpiringSoon() {
            // Given
            Long profileId = 1L;
            Certification expiringSoonCert = Certification.builder()
                    .id(3L)
                    .name("Expiring Soon Cert")
                    .issuingOrganization("Test Org")
                    .expirationDate(LocalDate.now().plusMonths(2))
                    .doesNotExpire(false)
                    .build();

            List<Certification> expiringSoonCerts = Arrays.asList(expiringSoonCert);
            List<CertificationDTO> expiringSoonDTOs = Arrays.asList(
                    CertificationDTO.builder()
                            .id(3L)
                            .name("Expiring Soon Cert")
                            .expiringSoon(true)
                            .build()
            );

            when(profileRepository.existsById(profileId)).thenReturn(true);
            when(certificationRepository.findExpiringSoon(
                    eq(profileId), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(expiringSoonCerts);
            when(profileMapper.toCertificationDtoList(expiringSoonCerts)).thenReturn(expiringSoonDTOs);

            // When
            List<CertificationDTO> result = certificationService.getCertificationsExpiringSoon(profileId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).isExpiringSoon()).isTrue();
        }
    }

    @Nested
    @DisplayName("Update Certification Tests")
    class UpdateCertificationTests {

        @Test
        @DisplayName("Should update certification successfully")
        void shouldUpdateCertificationSuccessfully() {
            // Given
            Long profileId = 1L;
            Long certId = 1L;
            CertificationDTO updateDTO = CertificationDTO.builder()
                    .name("AWS Solutions Architect - Professional")
                    .description("Updated description")
                    .build();

            when(certificationRepository.findByIdAndProfileId(certId, profileId))
                    .thenReturn(Optional.of(testCertification));
            when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);
            when(profileMapper.toDto(testCertification)).thenReturn(testCertificationDTO);

            // When
            CertificationDTO result = certificationService.updateCertification(profileId, certId, updateDTO);

            // Then
            assertThat(result).isNotNull();
            verify(profileMapper).updateCertificationFromDto(testCertification, updateDTO);
            verify(certificationRepository).save(testCertification);
        }

        @Test
        @DisplayName("Should throw exception when certification not found for update")
        void shouldThrowExceptionWhenCertificationNotFoundForUpdate() {
            // Given
            Long profileId = 1L;
            Long certId = 999L;
            CertificationDTO updateDTO = CertificationDTO.builder().build();

            when(certificationRepository.findByIdAndProfileId(certId, profileId))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> certificationService.updateCertification(profileId, certId, updateDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Certification not found");
        }
    }

    @Nested
    @DisplayName("Delete Certification Tests")
    class DeleteCertificationTests {

        @Test
        @DisplayName("Should delete certification successfully")
        void shouldDeleteCertificationSuccessfully() {
            // Given
            Long profileId = 1L;
            Long certId = 1L;

            when(certificationRepository.findByIdAndProfileId(certId, profileId))
                    .thenReturn(Optional.of(testCertification));

            // When
            certificationService.deleteCertification(profileId, certId);

            // Then
            verify(certificationRepository).delete(testCertification);
        }

        @Test
        @DisplayName("Should throw exception when certification not found for delete")
        void shouldThrowExceptionWhenCertificationNotFoundForDelete() {
            // Given
            Long profileId = 1L;
            Long certId = 999L;

            when(certificationRepository.findByIdAndProfileId(certId, profileId))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> certificationService.deleteCertification(profileId, certId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Certification not found");
        }

        @Test
        @DisplayName("Should delete all certifications for profile")
        void shouldDeleteAllCertificationsForProfile() {
            // Given
            Long profileId = 1L;

            when(profileRepository.existsById(profileId)).thenReturn(true);

            // When
            certificationService.deleteAllCertificationsByProfileId(profileId);

            // Then
            verify(certificationRepository).deleteByProfileId(profileId);
        }
    }
}