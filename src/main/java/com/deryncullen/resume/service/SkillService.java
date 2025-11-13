package com.deryncullen.resume.service;

import com.deryncullen.resume.dto.SkillDTO;
import com.deryncullen.resume.exception.ResourceNotFoundException;
import com.deryncullen.resume.model.Profile;
import com.deryncullen.resume.model.Skill;
import com.deryncullen.resume.repository.ProfileRepository;
import com.deryncullen.resume.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SkillService {

    private final SkillRepository skillRepository;
    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;

    public SkillDTO createSkill(long profileId, SkillDTO dto) {
        log.debug("Creating skill for profile ID: {}", profileId);

        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Profile not found with id: %d", profileId)
                ));

        Skill skill = profileMapper.toEntity(dto);
        skill.setProfile(profile);

        // Set display order to be last if not specified
        if (skill.getDisplayOrder() == null) {
            int maxOrder = skillRepository.findMaxDisplayOrderByProfileId(profileId)
                    .orElse(0);
            skill.setDisplayOrder(maxOrder + 1);
        }

        Skill saved = skillRepository.save(skill);
        log.info("Created skill ID: {} for profile ID: {}", saved.getId(), profileId);

        return profileMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<SkillDTO> getSkillsByProfileId(long profileId) {
        log.debug("Fetching skills for profile ID: {}", profileId);

        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException(
                    String.format("Profile not found with id: %d", profileId)
            );
        }

        List<Skill> skills = skillRepository.findByProfileIdOrderByDisplayOrder(profileId);

        return skills.stream()
                .map(profileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SkillDTO getSkillById(long profileId, long skillId) {
        log.debug("Fetching skill ID: {} for profile ID: {}", skillId, profileId);

        Skill skill = skillRepository.findByIdAndProfileId(skillId, profileId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Skill not found with id: %d for profile: %d",
                                skillId, profileId)
                ));

        return profileMapper.toDto(skill);
    }

    @Transactional(readOnly = true)
    public List<SkillDTO> getPrimarySkills(long profileId) {
        log.debug("Fetching primary skills for profile ID: {}", profileId);

        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException(
                    String.format("Profile not found with id: %d", profileId)
            );
        }

        List<Skill> skills = skillRepository.findByProfileIdAndPrimaryTrue(profileId);

        return skills.stream()
                .map(profileMapper::toDto)
                .collect(Collectors.toList());
    }

    public SkillDTO updateSkill(long profileId, long skillId, SkillDTO dto) {
        log.debug("Updating skill ID: {} for profile ID: {}", skillId, profileId);

        Skill skill = skillRepository.findByIdAndProfileId(skillId, profileId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Skill not found with id: %d for profile: %d",
                                skillId, profileId)
                ));

        // Update fields
        if (dto.getName() != null) {
            skill.setName(dto.getName());
        }
        if (dto.getCategory() != null) {
            skill.setCategory(dto.getCategory());
        }
        if (dto.getProficiencyLevel() != null) {
            skill.setProficiencyLevel(dto.getProficiencyLevel());
        }
        if (dto.getYearsOfExperience() != null) {
            skill.setYearsOfExperience(dto.getYearsOfExperience());
        }

        Skill updated = skillRepository.save(skill);
        log.info("Updated skill ID: {} for profile ID: {}", updated.getId(), profileId);


        return profileMapper.toDto(updated);
    }

    public void deleteSkill(long profileId, long skillId) {
        log.debug("Deleting skill ID: {} for profile ID: {}", skillId, profileId);

        Skill skill = skillRepository.findByIdAndProfileId(skillId, profileId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Skill not found with id: %d for profile: %d",
                                skillId, profileId)
                ));

        skillRepository.delete(skill);
        log.info("Deleted skill ID: {}", skillId);
    }

    public void reorderSkills(long profileId, List<Long> orderedIds) {
        log.debug("Reordering skills for profile ID: {}", profileId);

        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException(
                    String.format("Profile not found with id: %d", profileId)
            );
        }

        for (int i = 0; i < orderedIds.size(); i++) {
            Long skillId = orderedIds.get(i);
            Skill skill = skillRepository.findByIdAndProfileId(skillId, profileId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            String.format("Skill not found with id: %d for profile: %d",
                                    skillId, profileId)
                    ));

            skill.setDisplayOrder(i);
            skillRepository.save(skill);
        }

        log.info("Reordered {} skills for profile ID: {}", orderedIds.size(), profileId);
    }
}