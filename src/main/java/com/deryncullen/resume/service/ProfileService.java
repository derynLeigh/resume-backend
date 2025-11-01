package com.deryncullen.resume.service;

import com.deryncullen.resume.dto.*;
import com.deryncullen.resume.exception.DuplicateResourceException;
import com.deryncullen.resume.exception.ResourceNotFoundException;
import com.deryncullen.resume.model.Profile;
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
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;

    /**
     * Create a new profile
     */
    public ProfileDTO createProfile(CreateProfileRequest request) {
        log.debug("Creating new profile with email: {}", request.getEmail());

        // Check if email already exists
        if (profileRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    String.format("Profile with email %s already exists", request.getEmail())
            );
        }

        Profile profile = profileMapper.toEntity(request);
        Profile savedProfile = profileRepository.save(profile);

        log.info("Created new profile with ID: {}", savedProfile.getId());
        return profileMapper.toDto(savedProfile);
    }

    /**
     * Get profile by ID
     */
    @Transactional(readOnly = true)
    public ProfileDTO getProfileById(Long id) {
        log.debug("Fetching profile with ID: {}", id);

        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Profile not found with id: %d", id)
                ));

        return profileMapper.toDto(profile);
    }

    /**
     * Get profile by email
     */
    @Transactional(readOnly = true)
    public ProfileDTO getProfileByEmail(String email) {
        log.debug("Fetching profile with email: {}", email);

        Profile profile = profileRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Profile not found with email: %s", email)
                ));

        return profileMapper.toDto(profile);
    }

    /**
     * Get all active profiles
     */
    @Transactional(readOnly = true)
    public List<ProfileDTO> getAllActiveProfiles() {
        log.debug("Fetching all active profiles");

        List<Profile> profiles = profileRepository.findByActiveTrue();

        return profiles.stream()
                .map(profileMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get profile with all relations (experiences, educations, skills, certifications) using separate queries to avoid Hibernate's MultipleBagFetchException
     */
    @Transactional(readOnly = true)
    public ProfileDTO getProfileWithAllRelations(Long id) {
        log.debug("Fetching profile with all relations for ID: {}", id);

        // First, fetch the base profile
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Profile not found with id: %d", id)
                ));

        // Then fetch each collection separately to avoid MultipleBagFetchException
        // These will be lazily loaded within the transaction
        profileRepository.findByIdWithExperiences(id);
        profileRepository.findByIdWithEducations(id);
        profileRepository.findByIdWithSkills(id);
        profileRepository.findByIdWithCertifications(id);

        // Now all collections are loaded and we can map to DTO
        return profileMapper.toDto(profile);
    }

    /**
     * Update profile
     */
    public ProfileDTO updateProfile(Long id, UpdateProfileRequest request) {
        log.debug("Updating profile with ID: {}", id);

        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Profile not found with id: %d", id)
                ));

        // Check if email is being changed and if new email already exists
        if (request.getEmail() != null &&
                !request.getEmail().equals(profile.getEmail()) &&
                profileRepository.findByEmail(request.getEmail()).isPresent()) {

            Profile existingProfile = profileRepository.findByEmail(request.getEmail()).get();
            if (!existingProfile.getId().equals(id)) {
                throw new DuplicateResourceException(
                        String.format("Email %s is already in use", request.getEmail())
                );
            }
        }

        profileMapper.updateProfileFromRequest(profile, request);
        Profile updatedProfile = profileRepository.save(profile);

        log.info("Updated profile with ID: {}", id);
        return profileMapper.toDto(updatedProfile);
    }

    /**
     * Delete profile
     */
    public void deleteProfile(Long id) {
        log.debug("Deleting profile with ID: {}", id);

        if (!profileRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    String.format("Profile not found with id: %d", id)
            );
        }

        profileRepository.deleteById(id);
        log.info("Deleted profile with ID: {}", id);
    }

    /**
     * Soft delete profile (set active = false)
     */
    public void softDeleteProfile(Long id) {
        log.debug("Soft deleting profile with ID: {}", id);

        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Profile not found with id: %d", id)
                ));

        profile.setActive(false);
        profileRepository.save(profile);

        log.info("Soft deleted profile with ID: {}", id);
    }
}