
package com.deryncullen.resume.controller;

import com.deryncullen.resume.dto.SkillDTO;
import com.deryncullen.resume.model.Skill;
import com.deryncullen.resume.security.JwtService;
import com.deryncullen.resume.service.SkillService;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SkillController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security for unit tests
@DisplayName("SkillController Tests")
class SkillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SkillService skillService;

    // Mock the security beans that JwtAuthenticationFilter depends on
    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private SkillDTO testSkillDTO;

    @BeforeEach
    void setUp() {
        testSkillDTO = SkillDTO.builder()
                .id(1L)
                .name("Java")
                .category(Skill.SkillCategory.PROGRAMMING_LANGUAGE)
                .proficiencyLevel(Skill.ProficiencyLevel.ADVANCED)
                .yearsOfExperience(5)
                .primary(true)
                .displayOrder(0)
                .build();
    }

    @Nested
    @DisplayName("Create Skill Tests")
    class CreateSkillTests {

        @Test
        @DisplayName("Should create skill and return 201")
        @WithMockUser(roles = "ADMIN")
        void shouldCreateSkill() throws Exception {
            // Given
            when(skillService.createSkill(eq(1L), any(SkillDTO.class)))
                    .thenReturn(testSkillDTO);

            // When/Then
            mockMvc.perform(post("/profiles/1/skills")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testSkillDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Java"))
                    .andExpect(jsonPath("$.category").value("PROGRAMMING_LANGUAGE"));

            verify(skillService).createSkill(eq(1L), any(SkillDTO.class));
        }
    }

    @Nested
    @DisplayName("Get Skills Tests")
    class GetSkillsTests {

        @Test
        @DisplayName("Should get all skills for profile")
        void shouldGetAllSkills() throws Exception {
            // Given
            List<SkillDTO> skills = Arrays.asList(testSkillDTO);
            when(skillService.getSkillsByProfileId(1L)).thenReturn(skills);

            // When/Then
            mockMvc.perform(get("/profiles/1/skills"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Java"));

            verify(skillService).getSkillsByProfileId(1L);
        }

        @Test
        @DisplayName("Should get skill by ID")
        void shouldGetSkillById() throws Exception {
            // Given
            when(skillService.getSkillById(1L, 1L)).thenReturn(testSkillDTO);

            // When/Then
            mockMvc.perform(get("/profiles/1/skills/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Java"));
        }

        @Test
        @DisplayName("Should get primary skills")
        void shouldGetPrimarySkills() throws Exception {
            // Given
            List<SkillDTO> primarySkills = Arrays.asList(testSkillDTO);
            when(skillService.getPrimarySkills(1L)).thenReturn(primarySkills);

            // When/Then
            mockMvc.perform(get("/profiles/1/skills/primary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].primary").value(true));
        }
    }

    @Nested
    @DisplayName("Update Skill Tests")
    class UpdateSkillTests {

        @Test
        @DisplayName("Should update skill")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateSkill() throws Exception {
            // Given
            when(skillService.updateSkill(eq(1L), eq(1L), any(SkillDTO.class)))
                    .thenReturn(testSkillDTO);

            // When/Then
            mockMvc.perform(put("/profiles/1/skills/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testSkillDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Java"));
        }
    }

    @Nested
    @DisplayName("Delete Skill Tests")
    class DeleteSkillTests {

        @Test
        @DisplayName("Should delete skill")
        @WithMockUser(roles = "ADMIN")
        void shouldDeleteSkill() throws Exception {
            // When/Then
            mockMvc.perform(delete("/profiles/1/skills/1"))
                    .andExpect(status().isNoContent());

            verify(skillService).deleteSkill(1L, 1L);
        }
    }
}