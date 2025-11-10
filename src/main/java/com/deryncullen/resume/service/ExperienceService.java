package com.deryncullen.resume.service;

import com.deryncullen.resume.dto.ExperienceDTO;
import com.deryncullen.resume.exception.ResourceNotFoundException;
import com.deryncullen.resume.model.Experience;
import com.deryncullen.resume.model.Profile;
import com.deryncullen.resume.repository.ExperienceRepository;
import com.deryncullen.resume.repository.ProfileRepository;
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
public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;

    /**
     * Create a new experience for a profile
     */
    public ExperienceDTO createExperience(Long profileId, ExperienceDTO dto) {
        log.debug("Creating experience for profile ID: {}", profileId);

        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Profile not found with id: %d", profileId)
                ));

        Experience experience = profileMapper.toEntity(dto);
        experience.setProfile(profile);

        // Set display order to be last if not specified
        if (experience.getDisplayOrder() == null) {
            int maxOrder = experienceRepository.findMaxDisplayOrderByProfileId(profileId)
                    .orElse(0);
            experience.setDisplayOrder(maxOrder + 1);
        }

        Experience saved = experienceRepository.save(experience);
        log.info("Created experience ID: {} for profile ID: {}", saved.getId(), profileId);

        return profileMapper.toDto(saved);
    }

    /**
     * Get all experiences for a profile
     */
    @Transactional(readOnly = true)
    public List<ExperienceDTO> getExperiencesByProfileId(Long profileId) {
        log.debug("Fetching experiences for profile ID: {}", profileId);

        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException(
                    String.format("Profile not found with id: %d", profileId)
            );
        }

        List<Experience> experiences = experienceRepository.findByProfileIdOrderByStartDateDesc(profileId);

        return experiences.stream()
                .map(profileMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get a single experience by ID
     */
    @Transactional(readOnly = true)
    public ExperienceDTO getExperienceById(Long profileId, Long experienceId) {
        log.debug("Fetching experience ID: {} for profile ID: {}", experienceId, profileId);

        Experience experience = experienceRepository.findByIdAndProfileId(experienceId, profileId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Experience not found with id: %d for profile: %d", 
                                experienceId, profileId)
                ));

        return profileMapper.toDto(experience);
    }

    /**
     * Update an experience
     */
    public ExperienceDTO updateExperience(Long profileId, Long experienceId, ExperienceDTO dto) {
        log.debug("Updating experience ID: {} for profile ID: {}", experienceId, profileId);

        Experience experience = experienceRepository.findByIdAndProfileId(experienceId, profileId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Experience not found with id: %d for profile: %d", 
                                experienceId, profileId)
                ));

        // Update fields
        if (dto.getCompanyName() != null) {
            experience.setCompanyName(dto.getCompanyName());
        }
        if (dto.getJobTitle() != null) {
            experience.setJobTitle(dto.getJobTitle());
        }
        if (dto.getLocation() != null) {
            experience.setLocation(dto.getLocation());
        }
        if (dto.getStartDate() != null) {
            experience.setStartDate(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            experience.setEndDate(dto.getEndDate());
        }
        experience.setCurrent(dto.isCurrent());
        if (dto.getDescription() != null) {
            experience.setDescription(dto.getDescription());
        }
        if (dto.getAchievements() != null) {
            experience.setAchievements(dto.getAchievements());
        }
        if (dto.getTechnologies() != null) {
            experience.setTechnologies(dto.getTechnologies());
        }
        if (dto.getDisplayOrder() != null) {
            experience.setDisplayOrder(dto.getDisplayOrder());
        }

        Experience updated = experienceRepository.save(experience);
        log.info("Updated experience ID: {}", experienceId);

        return profileMapper.toDto(updated);
    }

    /**
     * Delete an experience
     */
    public void deleteExperience(Long profileId, Long experienceId) {
        log.debug("Deleting experience ID: {} for profile ID: {}", experienceId, profileId);

        Experience experience = experienceRepository.findByIdAndProfileId(experienceId, profileId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Experience not found with id: %d for profile: %d", 
                                experienceId, profileId)
                ));

        experienceRepository.delete(experience);
        log.info("Deleted experience ID: {}", experienceId);
    }

    /**
     * Reorder experiences
     */
    public void reorderExperiences(Long profileId, List<Long> orderedIds) {
        log.debug("Reordering experiences for profile ID: {}", profileId);

        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException(
                    String.format("Profile not found with id: %d", profileId)
            );
        }

        for (int i = 0; i < orderedIds.size(); i++) {
            Long experienceId = orderedIds.get(i);
            Experience experience = experienceRepository.findByIdAndProfileId(experienceId, profileId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            String.format("Experience not found with id: %d for profile: %d", 
                                    experienceId, profileId)
                    ));

            experience.setDisplayOrder(i);
            experienceRepository.save(experience);
        }

        log.info("Reordered {} experiences for profile ID: {}", orderedIds.size(), profileId);
    }

    /**
     * Get current experiences only
     */
    @Transactional(readOnly = true)
    public List<ExperienceDTO> getCurrentExperiences(Long profileId) {
        log.debug("Fetching current experiences for profile ID: {}", profileId);

        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException(
                    String.format("Profile not found with id: %d", profileId)
            );
        }

        List<Experience> experiences = experienceRepository.findByProfileIdAndCurrentTrue(profileId);

        return experiences.stream()
                .map(profileMapper::toDto)
                .collect(Collectors.toList());
    }
}
