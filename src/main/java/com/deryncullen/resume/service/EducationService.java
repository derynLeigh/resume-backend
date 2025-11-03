package com.deryncullen.resume.service;

import com.deryncullen.resume.dto.EducationDTO;
import com.deryncullen.resume.exception.ResourceNotFoundException;
import com.deryncullen.resume.model.Education;
import com.deryncullen.resume.model.Profile;
import com.deryncullen.resume.repository.EducationRepository;
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
public class EducationService {

    private final EducationRepository educationRepository;
    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;

    public EducationDTO createEducation(Long profileId, EducationDTO dto) {
        log.debug("Creating education for profile ID: {}", profileId);

        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Profile not found with id: %d", profileId)
                ));

        Education education = profileMapper.toEntity(dto);
        education.setProfile(profile);

        if (education.getDisplayOrder() == null) {
            int maxOrder = educationRepository.findMaxDisplayOrderByProfileId(profileId)
                    .orElse(0);
            education.setDisplayOrder(maxOrder + 1);
        }

        Education saved = educationRepository.save(education);
        log.info("Created education ID: {} for profile ID: {}", saved.getId(), profileId);

        return profileMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<EducationDTO> getEducationsByProfileId(Long profileId) {
        log.debug("Fetching education for profile ID: {}", profileId);

        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException(
                    String.format("Profile not found with id: %d", profileId)
            );
        }

        List<Education> educations = educationRepository.findByProfileIdOrderByGraduationDateDesc(profileId);

        return educations.stream()
                .map(profileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EducationDTO getEducationById(Long profileId, Long educationId) {
        log.debug("Fetching education ID: {} for profile ID: {}", educationId, profileId);

        Education education = educationRepository.findByIdAndProfileId(educationId, profileId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Education not found with id: %d for profile: %d", 
                                educationId, profileId)
                ));

        return profileMapper.toDto(education);
    }

    public EducationDTO updateEducation(Long profileId, Long educationId, EducationDTO dto) {
        log.debug("Updating education ID: {} for profile ID: {}", educationId, profileId);

        Education education = educationRepository.findByIdAndProfileId(educationId, profileId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Education not found with id: %d for profile: %d", 
                                educationId, profileId)
                ));

        // Update fields
        if (dto.getInstitutionName() != null) {
            education.setInstitutionName(dto.getInstitutionName());
        }
        if (dto.getDegree() != null) {
            education.setDegree(dto.getDegree());
        }
        if (dto.getFieldOfStudy() != null) {
            education.setFieldOfStudy(dto.getFieldOfStudy());
        }
        if (dto.getStartDate() != null) {
            education.setStartDate(dto.getStartDate());
        }
        if (dto.getGraduationDate() != null) {
            education.setGraduationDate(dto.getGraduationDate());
        }
        if (dto.getGrade() != null) {
            education.setGrade(dto.getGrade());
        }
        if (dto.getDescription() != null) {
            education.setDescription(dto.getDescription());
        }
        if (dto.getDisplayOrder() != null) {
            education.setDisplayOrder(dto.getDisplayOrder());
        }

        Education updated = educationRepository.save(education);
        log.info("Updated education ID: {}", educationId);

        return profileMapper.toDto(updated);
    }

    public void deleteEducation(Long profileId, Long educationId) {
        log.debug("Deleting education ID: {} for profile ID: {}", educationId, profileId);

        Education education = educationRepository.findByIdAndProfileId(educationId, profileId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Education not found with id: %d for profile: %d", 
                                educationId, profileId)
                ));

        educationRepository.delete(education);
        log.info("Deleted education ID: {}", educationId);
    }

    public void reorderEducation(Long profileId, List<Long> orderedIds) {
        log.debug("Reordering education for profile ID: {}", profileId);

        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException(
                    String.format("Profile not found with id: %d", profileId)
            );
        }

        for (int i = 0; i < orderedIds.size(); i++) {
            Long educationId = orderedIds.get(i);
            Education education = educationRepository.findByIdAndProfileId(educationId, profileId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            String.format("Education not found with id: %d for profile: %d", 
                                    educationId, profileId)
                    ));

            education.setDisplayOrder(i);
            educationRepository.save(education);
        }

        log.info("Reordered {} education entries for profile ID: {}", orderedIds.size(), profileId);
    }
}
