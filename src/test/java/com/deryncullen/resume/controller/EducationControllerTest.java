package com.deryncullen.resume.controller;

import com.deryncullen.resume.dto.EducationDTO;
import com.deryncullen.resume.exception.ResourceNotFoundException;
import com.deryncullen.resume.security.JwtService;
import com.deryncullen.resume.service.EducationService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EducationController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security for unit tests
@DisplayName("EducationController Tests")
class EducationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EducationService educationService;

    @MockBean
    private JwtService jwtService;

    private EducationDTO testEducationDTO;

    @BeforeEach
    void setUp() {
        testEducationDTO = EducationDTO.builder()
                .id(1L)
                .institutionName("University of Leeds")
                .degree("BSc Computer Science")
                .fieldOfStudy("Software Engineering")
                .startDate(LocalDate.of(2019, 9, 1))
                .graduationDate(LocalDate.of(2022, 6, 30))
                .grade("First Class Honours")
                .description("Focused on software engineering and distributed systems")
                .displayOrder(1)
                .build();
    }

    @Nested
    @DisplayName("POST /profiles/{profileId}/educations")
    class CreateEducationTests {

        @Test
        @DisplayName("Should create education and return 201")
        @WithMockUser(roles = "ADMIN")
        void shouldCreateEducation() throws Exception {
            // Given
            Long profileId = 1L;
            EducationDTO inputDTO = EducationDTO.builder()
                    .institutionName("University of Leeds")
                    .degree("BSc Computer Science")
                    .fieldOfStudy("Software Engineering")
                    .startDate(LocalDate.of(2019, 9, 1))
                    .graduationDate(LocalDate.of(2022, 6, 30))
                    .build();

            when(educationService.createEducation(eq(profileId), any(EducationDTO.class)))
                    .thenReturn(testEducationDTO);

            // When & Then
            mockMvc.perform(post("/profiles/{profileId}/educations", profileId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.institutionName").value("University of Leeds"))
                    .andExpect(jsonPath("$.degree").value("BSc Computer Science"))
                    .andExpect(jsonPath("$.fieldOfStudy").value("Software Engineering"));

            verify(educationService).createEducation(eq(profileId), any(EducationDTO.class));
        }

        @Test
        @DisplayName("Should return 404 when profile not found")
        void shouldReturn404WhenProfileNotFound() throws Exception {
            // Given
            Long profileId = 999L;
            EducationDTO inputDTO = EducationDTO.builder()
                    .institutionName("University of Leeds")
                    .degree("BSc Computer Science")
                    .build();

            when(educationService.createEducation(eq(profileId), any(EducationDTO.class)))
                    .thenThrow(new ResourceNotFoundException("Profile not found with id: " + profileId));

            // When & Then
            mockMvc.perform(post("/profiles/{profileId}/educations", profileId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDTO)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 for invalid input")
        void shouldReturn400ForInvalidInput() throws Exception {
            // Given
            Long profileId = 1L;
            Map<String, Object> invalidInput = new HashMap<>();
            // Missing required fields

            // When & Then
            mockMvc.perform(post("/profiles/{profileId}/educations", profileId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidInput)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /profiles/{profileId}/educations")
    class GetEducationsTests {

        @Test
        @DisplayName("Should get all educations for profile")
        void shouldGetAllEducations() throws Exception {
            // Given
            Long profileId = 1L;
            List<EducationDTO> educations = Arrays.asList(
                    testEducationDTO,
                    EducationDTO.builder()
                            .id(2L)
                            .institutionName("Coursera")
                            .degree("Professional Certificate")
                            .fieldOfStudy("Product Management")
                            .graduationDate(LocalDate.of(2023, 12, 15))
                            .displayOrder(2)
                            .build()
            );

            when(educationService.getEducationsByProfileId(profileId))
                    .thenReturn(educations);

            // When & Then
            mockMvc.perform(get("/profiles/{profileId}/educations", profileId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].institutionName").value("University of Leeds"))
                    .andExpect(jsonPath("$[1].institutionName").value("Coursera"));

            verify(educationService).getEducationsByProfileId(profileId);
        }

        @Test
        @DisplayName("Should return empty list when no educations exist")
        void shouldReturnEmptyListWhenNoEducations() throws Exception {
            // Given
            Long profileId = 1L;
            when(educationService.getEducationsByProfileId(profileId))
                    .thenReturn(Arrays.asList());

            // When & Then
            mockMvc.perform(get("/profiles/{profileId}/educations", profileId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should return 404 when profile not found")
        void shouldReturn404WhenProfileNotFoundForGet() throws Exception {
            // Given
            Long profileId = 999L;
            when(educationService.getEducationsByProfileId(profileId))
                    .thenThrow(new ResourceNotFoundException("Profile not found"));

            // When & Then
            mockMvc.perform(get("/profiles/{profileId}/educations", profileId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /profiles/{profileId}/educations/{educationId}")
    class GetEducationByIdTests {

        @Test
        @DisplayName("Should get education by ID")
        void shouldGetEducationById() throws Exception {
            // Given
            Long profileId = 1L;
            Long educationId = 1L;

            when(educationService.getEducationById(profileId, educationId))
                    .thenReturn(testEducationDTO);

            // When & Then
            mockMvc.perform(get("/profiles/{profileId}/educations/{educationId}",
                            profileId, educationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.institutionName").value("University of Leeds"))
                    .andExpect(jsonPath("$.degree").value("BSc Computer Science"));

            verify(educationService).getEducationById(profileId, educationId);
        }

        @Test
        @DisplayName("Should return 404 when education not found")
        void shouldReturn404WhenEducationNotFound() throws Exception {
            // Given
            Long profileId = 1L;
            Long educationId = 999L;

            when(educationService.getEducationById(profileId, educationId))
                    .thenThrow(new ResourceNotFoundException("Education not found"));

            // When & Then
            mockMvc.perform(get("/profiles/{profileId}/educations/{educationId}",
                            profileId, educationId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /profiles/{profileId}/educations/{educationId}")
    class UpdateEducationTests {

        @Test
        @DisplayName("Should update education successfully")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateEducation() throws Exception {
            // Given
            Long profileId = 1L;
            Long educationId = 1L;
            EducationDTO updateDTO = EducationDTO.builder()
                    .grade("First Class Honours with Distinction")
                    .description("Updated description")
                    .build();

            EducationDTO updatedEducation = EducationDTO.builder()
                    .id(1L)
                    .institutionName("University of Leeds")
                    .degree("BSc Computer Science")
                    .grade("First Class Honours with Distinction")
                    .description("Updated description")
                    .build();

            when(educationService.updateEducation(profileId, educationId, updateDTO))
                    .thenReturn(updatedEducation);

            // When & Then
            mockMvc.perform(put("/profiles/{profileId}/educations/{educationId}",
                            profileId, educationId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.grade").value("First Class Honours with Distinction"))
                    .andExpect(jsonPath("$.description").value("Updated description"));

            verify(educationService).updateEducation(profileId, educationId, updateDTO);
        }

        @Test
        @DisplayName("Should return 404 when education not found for update")
        void shouldReturn404WhenEducationNotFoundForUpdate() throws Exception {
            // Given
            Long profileId = 1L;
            Long educationId = 999L;
            EducationDTO updateDTO = EducationDTO.builder().build();

            when(educationService.updateEducation(profileId, educationId, updateDTO))
                    .thenThrow(new ResourceNotFoundException("Education not found"));

            // When & Then
            mockMvc.perform(put("/profiles/{profileId}/educations/{educationId}",
                            profileId, educationId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when graduation date before start date")
        void shouldReturn400WhenInvalidDates() throws Exception {
            // Given
            Long profileId = 1L;
            Long educationId = 1L;
            EducationDTO invalidDTO = EducationDTO.builder()
                    .startDate(LocalDate.of(2022, 9, 1))
                    .graduationDate(LocalDate.of(2020, 6, 30))
                    .build();

            when(educationService.updateEducation(eq(profileId), eq(educationId), any()))
                    .thenThrow(new IllegalArgumentException("Graduation date must be after start date"));

            // When & Then
            mockMvc.perform(put("/profiles/{profileId}/educations/{educationId}",
                            profileId, educationId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /profiles/{profileId}/educations/{educationId}")
    class DeleteEducationTests {

        @Test
        @DisplayName("Should delete education successfully")
        @WithMockUser(roles = "ADMIN")
        void shouldDeleteEducation() throws Exception {
            // Given
            Long profileId = 1L;
            Long educationId = 1L;

            doNothing().when(educationService).deleteEducation(profileId, educationId);

            // When & Then
            mockMvc.perform(delete("/profiles/{profileId}/educations/{educationId}",
                            profileId, educationId))
                    .andExpect(status().isNoContent());

            verify(educationService).deleteEducation(profileId, educationId);
        }

        @Test
        @DisplayName("Should return 404 when education not found for delete")
        void shouldReturn404WhenEducationNotFoundForDelete() throws Exception {
            // Given
            Long profileId = 1L;
            Long educationId = 999L;

            doThrow(new ResourceNotFoundException("Education not found"))
                    .when(educationService).deleteEducation(profileId, educationId);

            // When & Then
            mockMvc.perform(delete("/profiles/{profileId}/educations/{educationId}",
                            profileId, educationId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /profiles/{profileId}/educations/reorder")
    class ReorderEducationsTests {

        @Test
        @DisplayName("Should reorder educations successfully")
        @WithMockUser(roles = "ADMIN")
        void shouldReorderEducations() throws Exception {
            // Given
            Long profileId = 1L;
            List<Long> orderedIds = Arrays.asList(3L, 1L, 2L);
            Map<String, List<Long>> request = new HashMap<>();
            request.put("orderedIds", orderedIds);

            doNothing().when(educationService).reorderEducation(profileId, orderedIds);

            // When & Then
            mockMvc.perform(put("/profiles/{profileId}/educations/reorder", profileId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());

            verify(educationService).reorderEducation(profileId, orderedIds);
        }

        @Test
        @DisplayName("Should return 400 when orderedIds is missing")
        void shouldReturn400WhenOrderedIdsMissing() throws Exception {
            // Given
            Long profileId = 1L;
            Map<String, Object> emptyRequest = new HashMap<>();

            // When & Then
            mockMvc.perform(put("/profiles/{profileId}/educations/reorder", profileId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(emptyRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 when profile not found for reorder")
        void shouldReturn404WhenProfileNotFoundForReorder() throws Exception {
            // Given
            Long profileId = 999L;
            List<Long> orderedIds = Arrays.asList(1L, 2L);
            Map<String, List<Long>> request = new HashMap<>();
            request.put("orderedIds", orderedIds);

            doThrow(new ResourceNotFoundException("Profile not found"))
                    .when(educationService).reorderEducation(profileId, orderedIds);

            // When & Then
            mockMvc.perform(put("/profiles/{profileId}/educations/reorder", profileId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }
}