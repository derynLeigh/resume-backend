package com.deryncullen.resume.service;

import com.deryncullen.resume.dto.SkillDTO;
import com.deryncullen.resume.exception.ResourceNotFoundException;
import com.deryncullen.resume.model.Skill;
import com.deryncullen.resume.model.Profile;
import com.deryncullen.resume.repository.SkillRepository;
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
@DisplayName("SkillService Tests")
class SkillServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ProfileMapper profileMapper;

    @InjectMocks
    private SkillService skillService;

    private Profile testProfile;
    private Skill testSkill;
    private SkillDTO testSkillDTO;

    @BeforeEach
    void setUp() {
        testProfile = Profile.builder()
                .id(1L)
                .firstName("Deryn")
                .lastName("Cullen")
                .email("deryn@example.com")
                .title("Technical Product Owner")
                .build();

        testSkill = Skill.builder()
                .id(1L)
                .name("Java")
                .category(Skill.SkillCategory.PROGRAMMING_LANGUAGE)
                .proficiencyLevel(Skill.ProficiencyLevel.ADVANCED)
                .yearsOfExperience(5)
                .primary(true)
                .profile(testProfile)
                .build();

        testSkillDTO = SkillDTO.builder()
                .id(1L)
                .name("Java")
                .category(Skill.SkillCategory.PROGRAMMING_LANGUAGE)
                .proficiencyLevel(Skill.ProficiencyLevel.ADVANCED)
                .yearsOfExperience(5)
                .primary(true)
                .build();
    }

    @Nested
    @DisplayName("Create Skill Tests")
    class CreateSkillTests {

        @Test
        @DisplayName("Should create skill successfully")
        void shouldCreateSkill() {
            // Given
            when(profileRepository.findById(1L)).thenReturn(Optional.of(testProfile));
            when(profileMapper.toEntity(any(SkillDTO.class))).thenReturn(testSkill);
            when(skillRepository.findMaxDisplayOrderByProfileId(1L)).thenReturn(Optional.of(5));
            when(skillRepository.save(any(Skill.class))).thenReturn(testSkill);
            when(profileMapper.toDto(any(Skill.class))).thenReturn(testSkillDTO);

            // When
            SkillDTO result = skillService.createSkill(1L, testSkillDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Java");
            verify(profileRepository).findById(1L);
            verify(skillRepository).save(any(Skill.class));
        }

        @Test
        @DisplayName("Should throw exception when profile not found")
        void shouldThrowExceptionWhenProfileNotFound() {
            // Given
            when(profileRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> skillService.createSkill(999L, testSkillDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Profile not found");

            verify(skillRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should set display order when not provided")
        void shouldSetDisplayOrderWhenNotProvided() {
            // Given
            testSkillDTO.setDisplayOrder(null);
            when(profileRepository.findById(1L)).thenReturn(Optional.of(testProfile));
            when(profileMapper.toEntity(any(SkillDTO.class))).thenReturn(testSkill);
            when(skillRepository.findMaxDisplayOrderByProfileId(1L)).thenReturn(Optional.of(5));
            when(skillRepository.save(any(Skill.class))).thenReturn(testSkill);
            when(profileMapper.toDto(any(Skill.class))).thenReturn(testSkillDTO);

            // When
            skillService.createSkill(1L, testSkillDTO);

            // Then
            verify(skillRepository).findMaxDisplayOrderByProfileId(1L);
            verify(skillRepository).save(argThat(skill -> skill.getDisplayOrder() == 6));
        }
    }

    @Nested
    @DisplayName("Get Skills Tests")
    class GetSkillsTests {

        @Test
        @DisplayName("Should get all skills for profile")
        void shouldGetAllSkillsForProfile() {
            // Given
            List<Skill> skills = Arrays.asList(testSkill);
            when(profileRepository.existsById(1L)).thenReturn(true);
            when(skillRepository.findByProfileIdOrderByDisplayOrder(1L)).thenReturn(skills);
            when(profileMapper.toDto(any(Skill.class))).thenReturn(testSkillDTO);

            // When
            List<SkillDTO> result = skillService.getSkillsByProfileId(1L);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Java");
            verify(skillRepository).findByProfileIdOrderByDisplayOrder(1L);
        }

        @Test
        @DisplayName("Should throw exception when profile not found")
        void shouldThrowExceptionWhenProfileNotFoundOnGet() {
            // Given
            when(profileRepository.existsById(999L)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> skillService.getSkillsByProfileId(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Profile not found");
        }

        @Test
        @DisplayName("Should get skill by ID")
        void shouldGetSkillById() {
            // Given
            when(skillRepository.findByIdAndProfileId(1L, 1L)).thenReturn(Optional.of(testSkill));
            when(profileMapper.toDto(any(Skill.class))).thenReturn(testSkillDTO);

            // When
            SkillDTO result = skillService.getSkillById(1L, 1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Java");
        }

        @Test
        @DisplayName("Should get primary skills only")
        void shouldGetPrimarySkillsOnly() {
            // Given
            List<Skill> primarySkills = Arrays.asList(testSkill);
            when(profileRepository.existsById(1L)).thenReturn(true);
            when(skillRepository.findByProfileIdAndPrimaryTrue(1L)).thenReturn(primarySkills);
            when(profileMapper.toDto(any(Skill.class))).thenReturn(testSkillDTO);

            // When
            List<SkillDTO> result = skillService.getPrimarySkills(1L);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).isPrimary()).isTrue();
        }
    }

    @Nested
    @DisplayName("Update Skill Tests")
    class UpdateSkillTests {

        @Test
        @DisplayName("Should update skill successfully")
        void shouldUpdateSkill() {
            // Given
            SkillDTO updateDTO = SkillDTO.builder()
                    .name("Python")
                    .yearsOfExperience(3)
                    .build();

            when(skillRepository.findByIdAndProfileId(1L, 1L)).thenReturn(Optional.of(testSkill));
            when(skillRepository.save(any(Skill.class))).thenReturn(testSkill);
            when(profileMapper.toDto(any(Skill.class))).thenReturn(testSkillDTO);

            // When
            SkillDTO result = skillService.updateSkill(1L, 1L, updateDTO);

            // Then
            assertThat(result).isNotNull();
            verify(skillRepository).save(any(Skill.class));
        }

        @Test
        @DisplayName("Should throw exception when skill not found")
        void shouldThrowExceptionWhenSkillNotFound() {
            // Given
            when(skillRepository.findByIdAndProfileId(999L, 1L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> skillService.updateSkill(1L, 999L, testSkillDTO))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Delete Skill Tests")
    class DeleteSkillTests {

        @Test
        @DisplayName("Should delete skill successfully")
        void shouldDeleteSkill() {
            // Given
            when(skillRepository.findByIdAndProfileId(1L, 1L)).thenReturn(Optional.of(testSkill));

            // When
            skillService.deleteSkill(1L, 1L);

            // Then
            verify(skillRepository).delete(testSkill);
        }

        @Test
        @DisplayName("Should throw exception when skill not found")
        void shouldThrowExceptionWhenSkillNotFoundOnDelete() {
            // Given
            when(skillRepository.findByIdAndProfileId(999L, 1L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> skillService.deleteSkill(1L, 999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Reorder Skills Tests")
    class ReorderSkillsTests {

        @Test
        @DisplayName("Should reorder skills successfully")
        void shouldReorderSkills() {
            // Given
            List<Long> orderedIds = Arrays.asList(3L, 1L, 2L);
            when(profileRepository.existsById(1L)).thenReturn(true);
            when(skillRepository.findByIdAndProfileId(anyLong(), eq(1L)))
                    .thenReturn(Optional.of(testSkill));

            // When
            skillService.reorderSkills(1L, orderedIds);

            // Then
            verify(skillRepository, times(3)).save(any(Skill.class));
        }
    }
}