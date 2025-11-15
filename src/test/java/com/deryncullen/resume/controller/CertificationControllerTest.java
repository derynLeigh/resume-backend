package com.deryncullen.resume.controller;

import com.deryncullen.resume.dto.CertificationDTO;
import com.deryncullen.resume.exception.DuplicateResourceException;
import com.deryncullen.resume.exception.ResourceNotFoundException;
import com.deryncullen.resume.security.JwtService;
import com.deryncullen.resume.service.CertificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CertificationController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security for unit tests
@DisplayName("CertificationController Tests")
class CertificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CertificationService certificationService;

    // Mock the security beans that JwtAuthenticationFilter depends on
    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private CertificationDTO testCertificationDTO;

    @BeforeEach
    void setUp() {
        testCertificationDTO = CertificationDTO.builder()
                .id(1L)
                .name("AWS Solutions Architect")
                .issuingOrganization("Amazon Web Services")
                .credentialId("AWS-123456")
                .credentialUrl("https://aws.amazon.com/verify/AWS-123456")
                .dateObtained(LocalDate.of(2024, 1, 15))
                .expirationDate(LocalDate.of(2027, 1, 15))
                .doesNotExpire(false)
                .description("Professional level certification")
                .displayOrder(1)
                .expired(false)
                .expiringSoon(false)
                .build();
    }

    @Nested
    @DisplayName("POST /api/profiles/{profileId}/certifications")
    class AddCertificationTests {

        @Test
        @DisplayName("Should add certification successfully")
        @WithMockUser(roles = "ADMIN")
        void shouldAddCertificationSuccessfully() throws Exception {
            // Given
            Long profileId = 1L;
            CertificationDTO inputDTO = CertificationDTO.builder()
                    .name("AWS Solutions Architect")
                    .issuingOrganization("Amazon Web Services")
                    .dateObtained(LocalDate.of(2024, 1, 15))
                    .build();

            when(certificationService.addCertification(eq(profileId), any(CertificationDTO.class)))
                    .thenReturn(testCertificationDTO);

            // When & Then
            mockMvc.perform(post("/api/profiles/{profileId}/certifications", profileId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("AWS Solutions Architect"))
                    .andExpect(jsonPath("$.issuingOrganization").value("Amazon Web Services"));

            verify(certificationService).addCertification(eq(profileId), any(CertificationDTO.class));
        }

        @Test
        @DisplayName("Should return 404 when profile not found")
        void shouldReturn404WhenProfileNotFound() throws Exception {
            // Given
            Long profileId = 999L;
            CertificationDTO inputDTO = CertificationDTO.builder()
                    .name("AWS Solutions Architect")
                    .issuingOrganization("Amazon Web Services")
                    .dateObtained(LocalDate.of(2024, 1, 15))
                    .build();

            when(certificationService.addCertification(eq(profileId), any(CertificationDTO.class)))
                    .thenThrow(new ResourceNotFoundException("Profile not found"));

            // When & Then
            mockMvc.perform(post("/api/profiles/{profileId}/certifications", profileId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDTO)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 409 when duplicate certification")
        void shouldReturn409WhenDuplicateCertification() throws Exception {
            // Given
            Long profileId = 1L;
            CertificationDTO inputDTO = CertificationDTO.builder()
                    .name("AWS Solutions Architect")
                    .issuingOrganization("Amazon Web Services")
                    .dateObtained(LocalDate.of(2024, 1, 15))
                    .build();

            when(certificationService.addCertification(eq(profileId), any(CertificationDTO.class)))
                    .thenThrow(new DuplicateResourceException("Certification already exists"));

            // When & Then
            mockMvc.perform(post("/api/profiles/{profileId}/certifications", profileId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDTO)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("GET /api/profiles/{profileId}/certifications")
    class GetCertificationsTests {

        @Test
        @DisplayName("Should get all certifications for profile")
        void shouldGetAllCertifications() throws Exception {
            // Given
            Long profileId = 1L;
            List<CertificationDTO> certifications = Arrays.asList(testCertificationDTO);

            when(certificationService.getCertificationsByProfileId(profileId))
                    .thenReturn(certifications);

            // When & Then
            mockMvc.perform(get("/api/profiles/{profileId}/certifications", profileId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name").value("AWS Solutions Architect"));
        }

        @Test
        @DisplayName("Should get expired certifications")
        void shouldGetExpiredCertifications() throws Exception {
            // Given
            Long profileId = 1L;
            CertificationDTO expiredCert = CertificationDTO.builder()
                    .id(2L)
                    .name("Expired Cert")
                    .expired(true)
                    .build();
            List<CertificationDTO> expiredCerts = Arrays.asList(expiredCert);

            when(certificationService.getExpiredCertifications(profileId))
                    .thenReturn(expiredCerts);

            // When & Then
            mockMvc.perform(get("/api/profiles/{profileId}/certifications/expired", profileId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].expired").value(true));
        }

        @Test
        @DisplayName("Should get certifications expiring soon")
        void shouldGetCertificationsExpiringSoon() throws Exception {
            // Given
            Long profileId = 1L;
            CertificationDTO expiringSoonCert = CertificationDTO.builder()
                    .id(3L)
                    .name("Expiring Soon Cert")
                    .expiringSoon(true)
                    .build();
            List<CertificationDTO> expiringSoonCerts = Arrays.asList(expiringSoonCert);

            when(certificationService.getCertificationsExpiringSoon(profileId))
                    .thenReturn(expiringSoonCerts);

            // When & Then
            mockMvc.perform(get("/api/profiles/{profileId}/certifications/expiring-soon", profileId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].expiringSoon").value(true));
        }
    }

    @Nested
    @DisplayName("PUT /api/profiles/{profileId}/certifications/{certificationId}")
    class UpdateCertificationTests {

        @Test
        @DisplayName("Should update certification successfully")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateCertificationSuccessfully() throws Exception {
            // Given
            Long profileId = 1L;
            Long certId = 1L;
            CertificationDTO updateDTO = CertificationDTO.builder()
                    .description("Updated description")
                    .build();

            CertificationDTO updatedCert = CertificationDTO.builder()
                    .id(1L)
                    .name("AWS Solutions Architect")
                    .description("Updated description")
                    .build();

            when(certificationService.updateCertification(profileId, certId, updateDTO))
                    .thenReturn(updatedCert);

            // When & Then
            mockMvc.perform(put("/api/profiles/{profileId}/certifications/{certificationId}",
                            profileId, certId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.description").value("Updated description"));
        }

        @Test
        @DisplayName("Should return 404 when certification not found for update")
        void shouldReturn404WhenCertificationNotFoundForUpdate() throws Exception {
            // Given
            Long profileId = 1L;
            Long certId = 999L;
            CertificationDTO updateDTO = CertificationDTO.builder().build();

            when(certificationService.updateCertification(profileId, certId, updateDTO))
                    .thenThrow(new ResourceNotFoundException("Certification not found"));

            // When & Then
            mockMvc.perform(put("/api/profiles/{profileId}/certifications/{certificationId}",
                            profileId, certId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/profiles/{profileId}/certifications/{certificationId}")
    class DeleteCertificationTests {

        @Test
        @DisplayName("Should delete certification successfully")
        @WithMockUser(roles = "ADMIN")
        void shouldDeleteCertificationSuccessfully() throws Exception {
            // Given
            Long profileId = 1L;
            Long certId = 1L;

            doNothing().when(certificationService).deleteCertification(profileId, certId);

            // When & Then
            mockMvc.perform(delete("/api/profiles/{profileId}/certifications/{certificationId}",
                            profileId, certId))
                    .andExpect(status().isNoContent());

            verify(certificationService).deleteCertification(profileId, certId);
        }

        @Test
        @DisplayName("Should return 404 when certification not found for delete")
        void shouldReturn404WhenCertificationNotFoundForDelete() throws Exception {
            // Given
            Long profileId = 1L;
            Long certId = 999L;

            doThrow(new ResourceNotFoundException("Certification not found"))
                    .when(certificationService).deleteCertification(profileId, certId);

            // When & Then
            mockMvc.perform(delete("/api/profiles/{profileId}/certifications/{certificationId}",
                            profileId, certId))
                    .andExpect(status().isNotFound());
        }
    }
}