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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // Mock the security beans that JwtAuthenticationFilter depends on
    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private EducationDTO testEducationDTO;

    @BeforeEach
    void setUp() {
        testEducationDTO = EducationDTO.builder()
                .id(1L)
                .institutionName("University of Example")
                .degree("Bachelor of Science")
                .fieldOfStudy("Computer Science")
                .startDate(LocalDate.of(2015, 9, 1))
                .graduationDate(LocalDate.of(2019, 6, 30))
                .grade("First Class Honours")
                .description("Focused on software engineering and algorithms")
                .displayOrder(0)
                .build();
    }

    @Nested
    @DisplayName("Create Education Tests")
    class CreateEducationTests {

        @Test
        @DisplayName("Should create education and return 201")
        @WithMockUser(roles = "ADMIN")
        void shouldCreateEducation() throws Exception {
            // Given
            when(educationService.createEducation(eq(1L), any(EducationDTO.class)))
                    .thenReturn(testEducationDTO);

            // When/Then
            mockMvc.perform(post("/profiles/1/educations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testEducationDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.institutionName").value("University of Example"))
                    .andExpect(jsonPath("$.degree").value("Bachelor of Science"))
                    .andExpect(jsonPath("$.fieldOfStudy").value("Computer Science"));

            verify(educationService).createEducation(eq(1L), any(EducationDTO.class));
        }

        @Test
        @DisplayName("Should return 400 for invalid input")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400ForInvalidInput() throws Exception {
            // Given - Empty request body (invalid JSON)
            String invalidJson = "{}";

            // Mock service to throw validation exception for invalid data
            when(educationService.createEducation(eq(1L), any(EducationDTO.class)))
                    .thenThrow(new IllegalArgumentException("Invalid education data"));

            // When/Then
            mockMvc.perform(post("/profiles/1/educations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 when profile not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenProfileNotFound() throws Exception {
            // Given
            when(educationService.createEducation(eq(999L), any(EducationDTO.class)))
                    .thenThrow(new ResourceNotFoundException("Profile not found with id: 999"));

            // When/Then
            mockMvc.perform(post("/profiles/999/educations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testEducationDTO)))
                    .andExpect(status().isNotFound());

            verify(educationService).createEducation(eq(999L), any(EducationDTO.class));
        }
    }

    @Nested
    @DisplayName("Get Educations Tests")
    class GetEducationsTests {

        @Test
        @DisplayName("Should get all educations for profile")
        void shouldGetAllEducations() throws Exception {
            // Given
            EducationDTO education2 = EducationDTO.builder()
                    .id(2L)
                    .institutionName("Another University")
                    .degree("Master of Science")
                    .fieldOfStudy("Software Engineering")
                    .startDate(LocalDate.of(2019, 9, 1))
                    .graduationDate(LocalDate.of(2021, 6, 30))
                    .displayOrder(1)
                    .build();

            List<EducationDTO> educations = Arrays.asList(testEducationDTO, education2);
            when(educationService.getEducationsByProfileId(1L)).thenReturn(educations);

            // When/Then
            mockMvc.perform(get("/profiles/1/educations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].institutionName").value("University of Example"))
                    .andExpect(jsonPath("$[1].institutionName").value("Another University"));

            verify(educationService).getEducationsByProfileId(1L);
        }

        @Test
        @DisplayName("Should return empty list when profile has no educations")
        void shouldReturnEmptyListWhenNoEducations() throws Exception {
            // Given
            when(educationService.getEducationsByProfileId(1L)).thenReturn(Arrays.asList());

            // When/Then
            mockMvc.perform(get("/profiles/1/educations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Should return 404 when profile not found")
        void shouldReturn404WhenProfileNotFound() throws Exception {
            // Given
            when(educationService.getEducationsByProfileId(999L))
                    .thenThrow(new ResourceNotFoundException("Profile not found with id: 999"));

            // When/Then
            mockMvc.perform(get("/profiles/999/educations"))
                    .andExpect(status().isNotFound());

            verify(educationService).getEducationsByProfileId(999L);
        }
    }

    @Nested
    @DisplayName("Get Education By Id Tests")
    class GetEducationByIdTests {

        @Test
        @DisplayName("Should get education by ID")
        void shouldGetEducationById() throws Exception {
            // Given
            when(educationService.getEducationById(1L, 1L)).thenReturn(testEducationDTO);

            // When/Then
            mockMvc.perform(get("/profiles/1/educations/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.institutionName").value("University of Example"))
                    .andExpect(jsonPath("$.degree").value("Bachelor of Science"));

            verify(educationService).getEducationById(1L, 1L);
        }

        @Test
        @DisplayName("Should return 404 when education not found")
        void shouldReturn404WhenEducationNotFound() throws Exception {
            // Given
            when(educationService.getEducationById(1L, 999L))
                    .thenThrow(new ResourceNotFoundException("Education not found"));

            // When/Then
            mockMvc.perform(get("/profiles/1/educations/999"))
                    .andExpect(status().isNotFound());

            verify(educationService).getEducationById(1L, 999L);
        }
    }

    @Nested
    @DisplayName("Update Education Tests")
    class UpdateEducationTests {

        @Test
        @DisplayName("Should update education successfully")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateEducation() throws Exception {
            // Given
            EducationDTO updatedEducation = EducationDTO.builder()
                    .id(1L)
                    .institutionName("University of Example")
                    .degree("Bachelor of Science with Honours")
                    .fieldOfStudy("Computer Science")
                    .startDate(LocalDate.of(2015, 9, 1))
                    .graduationDate(LocalDate.of(2019, 6, 30))
                    .grade("First Class Honours")
                    .build();

            when(educationService.updateEducation(eq(1L), eq(1L), any(EducationDTO.class)))
                    .thenReturn(updatedEducation);

            // When/Then
            mockMvc.perform(put("/profiles/1/educations/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedEducation)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.degree").value("Bachelor of Science with Honours"));

            verify(educationService).updateEducation(eq(1L), eq(1L), any(EducationDTO.class));
        }

        @Test
        @DisplayName("Should return 404 when education not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenEducationNotFound() throws Exception {
            // Given
            when(educationService.updateEducation(eq(1L), eq(999L), any(EducationDTO.class)))
                    .thenThrow(new ResourceNotFoundException("Education not found"));

            // When/Then
            mockMvc.perform(put("/profiles/1/educations/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testEducationDTO)))
                    .andExpect(status().isNotFound());

            verify(educationService).updateEducation(eq(1L), eq(999L), any(EducationDTO.class));
        }

        @Test
        @DisplayName("Should return 400 for invalid date range")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400ForInvalidDateRange() throws Exception {
            // Given - Graduation date before start date
            EducationDTO invalidEducation = EducationDTO.builder()
                    .institutionName("Test University")
                    .degree("BSc")
                    .startDate(LocalDate.of(2019, 6, 30))
                    .graduationDate(LocalDate.of(2015, 9, 1))
                    .build();

            when(educationService.updateEducation(eq(1L), eq(1L), any(EducationDTO.class)))
                    .thenThrow(new IllegalArgumentException("Graduation date must be after start date"));

            // When/Then
            mockMvc.perform(put("/profiles/1/educations/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidEducation)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Delete Education Tests")
    class DeleteEducationTests {

        @Test
        @DisplayName("Should delete education successfully")
        @WithMockUser(roles = "ADMIN")
        void shouldDeleteEducation() throws Exception {
            // Given
            doNothing().when(educationService).deleteEducation(1L, 1L);

            // When/Then
            mockMvc.perform(delete("/profiles/1/educations/1"))
                    .andExpect(status().isNoContent());

            verify(educationService).deleteEducation(1L, 1L);
        }

        @Test
        @DisplayName("Should return 404 when education not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenEducationNotFound() throws Exception {
            // Given
            doThrow(new ResourceNotFoundException("Education not found"))
                    .when(educationService).deleteEducation(1L, 999L);

            // When/Then
            mockMvc.perform(delete("/profiles/1/educations/999"))
                    .andExpect(status().isNotFound());

            verify(educationService).deleteEducation(1L, 999L);
        }
    }

    @Nested
    @DisplayName("Reorder Educations Tests")
    class ReorderEducationsTests {

        @Test
        @DisplayName("Should reorder educations successfully")
        @WithMockUser(roles = "ADMIN")
        void shouldReorderEducations() throws Exception {
            // Given
            List<Long> orderedIds = Arrays.asList(3L, 1L, 2L);
            Map<String, List<Long>> requestBody = new HashMap<>();
            requestBody.put("orderedIds", orderedIds);

            doNothing().when(educationService).reorderEducation(eq(1L), anyList());

            // When/Then
            mockMvc.perform(put("/profiles/1/educations/reorder")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isNoContent());

            verify(educationService).reorderEducation(eq(1L), anyList());
        }

        @Test
        @DisplayName("Should return 400 when orderedIds is missing from request")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenOrderedIdsMissing() throws Exception {
            // Given - Empty map (no orderedIds key)
            Map<String, List<Long>> emptyRequest = new HashMap<>();

            // The service will receive null for orderedIds
            doThrow(new IllegalArgumentException("orderedIds cannot be null"))
                    .when(educationService).reorderEducation(eq(1L), isNull());

            // When/Then
            mockMvc.perform(put("/profiles/1/educations/reorder")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(emptyRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 when profile not found for reorder")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenProfileNotFoundForReorder() throws Exception {
            // Given
            List<Long> orderedIds = Arrays.asList(1L, 2L, 3L);
            Map<String, List<Long>> requestBody = new HashMap<>();
            requestBody.put("orderedIds", orderedIds);

            doThrow(new ResourceNotFoundException("Profile not found with id: 999"))
                    .when(educationService).reorderEducation(eq(999L), anyList());

            // When/Then
            mockMvc.perform(put("/profiles/999/educations/reorder")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isNotFound());

            verify(educationService).reorderEducation(eq(999L), anyList());
        }
    }

    @Nested
    @DisplayName("Date Validation Tests")
    class DateValidationTests {

        @Test
        @DisplayName("Should accept valid date range")
        @WithMockUser(roles = "ADMIN")
        void shouldAcceptValidDateRange() throws Exception {
            // Given
            EducationDTO validEducation = EducationDTO.builder()
                    .institutionName("Test University")
                    .degree("BSc")
                    .startDate(LocalDate.of(2015, 9, 1))
                    .graduationDate(LocalDate.of(2019, 6, 30))
                    .build();

            when(educationService.createEducation(eq(1L), any(EducationDTO.class)))
                    .thenReturn(validEducation);

            // When/Then
            mockMvc.perform(post("/profiles/1/educations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validEducation)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Should handle education without graduation date (ongoing)")
        @WithMockUser(roles = "ADMIN")
        void shouldHandleOngoingEducation() throws Exception {
            // Given - Education still in progress
            EducationDTO ongoingEducation = EducationDTO.builder()
                    .institutionName("Current University")
                    .degree("PhD")
                    .fieldOfStudy("Computer Science")
                    .startDate(LocalDate.of(2022, 9, 1))
                    .graduationDate(null) // Still studying
                    .build();

            when(educationService.createEducation(eq(1L), any(EducationDTO.class)))
                    .thenReturn(ongoingEducation);

            // When/Then
            mockMvc.perform(post("/profiles/1/educations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ongoingEducation)))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("Profile Association Tests")
    class ProfileAssociationTests {

        @Test
        @DisplayName("Should get educations for specific profile only")
        void shouldGetEducationsForSpecificProfile() throws Exception {
            // Given - Profile 1 has educations
            when(educationService.getEducationsByProfileId(1L))
                    .thenReturn(Arrays.asList(testEducationDTO));

            // When/Then - Request for profile 1
            mockMvc.perform(get("/profiles/1/educations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));

            // Request for profile 2 (different profile)
            when(educationService.getEducationsByProfileId(2L))
                    .thenReturn(Arrays.asList());

            mockMvc.perform(get("/profiles/2/educations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Should not allow accessing education from wrong profile")
        @WithMockUser(roles = "ADMIN")
        void shouldNotAllowCrossProfileAccess() throws Exception {
            // Given - Try to update education 1 which belongs to profile 1, but via profile 2
            when(educationService.updateEducation(eq(2L), eq(1L), any(EducationDTO.class)))
                    .thenThrow(new ResourceNotFoundException("Education not found for this profile"));

            // When/Then
            mockMvc.perform(put("/profiles/2/educations/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testEducationDTO)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Display Order Tests")
    class DisplayOrderTests {

        @Test
        @DisplayName("Should return educations in display order")
        void shouldReturnEducationsInOrder() throws Exception {
            // Given - Multiple educations with specific order
            EducationDTO education1 = EducationDTO.builder()
                    .id(1L)
                    .institutionName("First Education")
                    .degree("BSc")
                    .displayOrder(0)
                    .build();

            EducationDTO education2 = EducationDTO.builder()
                    .id(2L)
                    .institutionName("Second Education")
                    .degree("MSc")
                    .displayOrder(1)
                    .build();

            // Service returns them in order
            when(educationService.getEducationsByProfileId(1L))
                    .thenReturn(Arrays.asList(education1, education2));

            // When/Then
            mockMvc.perform(get("/profiles/1/educations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].displayOrder").value(0))
                    .andExpect(jsonPath("$[1].displayOrder").value(1))
                    .andExpect(jsonPath("$[0].institutionName").value("First Education"))
                    .andExpect(jsonPath("$[1].institutionName").value("Second Education"));
        }
    }

    @Nested
    @DisplayName("Content Validation Tests")
    class ContentValidationTests {

        @Test
        @DisplayName("Should accept education with all optional fields")
        @WithMockUser(roles = "ADMIN")
        void shouldAcceptFullEducation() throws Exception {
            // Given - Education with all fields populated
            EducationDTO fullEducation = EducationDTO.builder()
                    .institutionName("Complete University")
                    .degree("Bachelor of Science")
                    .fieldOfStudy("Computer Science")
                    .startDate(LocalDate.of(2015, 9, 1))
                    .graduationDate(LocalDate.of(2019, 6, 30))
                    .grade("First Class Honours")
                    .description("Comprehensive description of the education")
                    .displayOrder(0)
                    .build();

            when(educationService.createEducation(eq(1L), any(EducationDTO.class)))
                    .thenReturn(fullEducation);

            // When/Then
            mockMvc.perform(post("/profiles/1/educations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(fullEducation)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.institutionName").exists())
                    .andExpect(jsonPath("$.degree").exists())
                    .andExpect(jsonPath("$.fieldOfStudy").exists())
                    .andExpect(jsonPath("$.grade").exists())
                    .andExpect(jsonPath("$.description").exists());
        }

        @Test
        @DisplayName("Should accept education with only required fields")
        @WithMockUser(roles = "ADMIN")
        void shouldAcceptMinimalEducation() throws Exception {
            // Given - Education with only required fields
            EducationDTO minimalEducation = EducationDTO.builder()
                    .institutionName("Minimal University")
                    .degree("BSc")
                    .build();

            when(educationService.createEducation(eq(1L), any(EducationDTO.class)))
                    .thenReturn(minimalEducation);

            // When/Then
            mockMvc.perform(post("/profiles/1/educations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(minimalEducation)))
                    .andExpect(status().isCreated());
        }
    }
}