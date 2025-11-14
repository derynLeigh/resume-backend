package com.deryncullen.resume.service;

import com.deryncullen.resume.dto.CertificationDTO;
import com.deryncullen.resume.exception.DuplicateResourceException;
import com.deryncullen.resume.exception.ResourceNotFoundException;
import com.deryncullen.resume.model.Certification;
import com.deryncullen.resume.model.Profile;
import com.deryncullen.resume.repository.CertificationRepository;
import com.deryncullen.resume.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CertificationService {

    private final CertificationRepository certificationRepository;
    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;

    /**
     * Add a new certification to a profile
     */
    public CertificationDTO addCertification(Long profileId, CertificationDTO certificationDTO) {
        log.debug("Adding certification to profile ID: {}", profileId);

        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with ID: " + profileId));

        // Check for duplicate certification
        if (certificationRepository.existsByProfileIdAndNameAndIssuingOrganization(
                profileId, certificationDTO.getName(), certificationDTO.getIssuingOrganization())) {
            throw new DuplicateResourceException(
                    String.format("Profile already has this certification: %s from %s",
                            certificationDTO.getName(), certificationDTO.getIssuingOrganization())
            );
        }

        Certification certification = profileMapper.toEntity(certificationDTO);
        certification.setProfile(profile);

        // Set display order if not provided
        if (certification.getDisplayOrder() == null) {
            long count = certificationRepository.countByProfileId(profileId);
            certification.setDisplayOrder((int) count + 1);
        }

        Certification saved = certificationRepository.save(certification);
        log.info("Added certification ID {} to profile ID {}", saved.getId(), profileId);

        return profileMapper.toDto(saved);
    }

    /**
     * Get all certifications for a profile
     */
    @Transactional(readOnly = true)
    public List<CertificationDTO> getCertificationsByProfileId(Long profileId) {
        log.debug("Fetching certifications for profile ID: {}", profileId);

        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException("Profile not found with ID: " + profileId);
        }

        List<Certification> certifications = certificationRepository
                .findByProfileIdOrderByDisplayOrderAscDateObtainedDesc(profileId);

        return profileMapper.toCertificationDtoList(certifications);
    }

    /**
     * Get expired certifications for a profile
     */
    @Transactional(readOnly = true)
    public List<CertificationDTO> getExpiredCertifications(Long profileId) {
        log.debug("Fetching expired certifications for profile ID: {}", profileId);

        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException("Profile not found with ID: " + profileId);
        }

        List<Certification> expiredCerts = certificationRepository
                .findExpired(profileId, LocalDate.now());

        return profileMapper.toCertificationDtoList(expiredCerts);
    }

    /**
     * Get certifications expiring soon (within 3 months)
     */
    @Transactional(readOnly = true)
    public List<CertificationDTO> getCertificationsExpiringSoon(Long profileId) {
        log.debug("Fetching expiring certifications for profile ID: {}", profileId);

        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException("Profile not found with ID: " + profileId);
        }

        LocalDate now = LocalDate.now();
        LocalDate threeMonthsFromNow = now.plusMonths(3);

        List<Certification> expiringSoonCerts = certificationRepository
                .findExpiringSoon(profileId, now, threeMonthsFromNow);

        return profileMapper.toCertificationDtoList(expiringSoonCerts);
    }

    /**
     * Get certifications by issuing organization
     */
    @Transactional(readOnly = true)
    public List<CertificationDTO> getCertificationsByOrganization(Long profileId, String organization) {
        log.debug("Fetching certifications from {} for profile ID: {}", organization, profileId);

        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException("Profile not found with ID: " + profileId);
        }

        List<Certification> certifications = certificationRepository
                .findByProfileIdAndIssuingOrganization(profileId, organization);

        return profileMapper.toCertificationDtoList(certifications);
    }

    /**
     * Update a certification
     */
    public CertificationDTO updateCertification(Long profileId, Long certificationId, CertificationDTO updateDTO) {
        log.debug("Updating certification ID {} for profile ID {}", certificationId, profileId);

        Certification certification = certificationRepository
                .findByIdAndProfileId(certificationId, profileId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Certification not found with ID %d for profile %d",
                                certificationId, profileId)
                ));

        profileMapper.updateCertificationFromDto(certification, updateDTO);

        // Validate expiration date if changed
        if (updateDTO.getExpirationDate() != null && updateDTO.getDateObtained() != null) {
            if (!updateDTO.isDoesNotExpire() &&
                    updateDTO.getExpirationDate().isBefore(updateDTO.getDateObtained())) {
                throw new IllegalArgumentException("Expiration date must be after date obtained");
            }
        }

        Certification saved = certificationRepository.save(certification);
        log.info("Updated certification ID {}", certificationId);

        return profileMapper.toDto(saved);
    }

    /**
     * Delete a certification
     */
    public void deleteCertification(Long profileId, Long certificationId) {
        log.debug("Deleting certification ID {} for profile ID {}", certificationId, profileId);

        Certification certification = certificationRepository
                .findByIdAndProfileId(certificationId, profileId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Certification not found with ID %d for profile %d",
                                certificationId, profileId)
                ));

        certificationRepository.delete(certification);
        log.info("Deleted certification ID {}", certificationId);
    }

    /**
     * Delete all certifications for a profile
     */
    public void deleteAllCertificationsByProfileId(Long profileId) {
        log.debug("Deleting all certifications for profile ID: {}", profileId);

        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException("Profile not found with ID: " + profileId);
        }

        certificationRepository.deleteByProfileId(profileId);
        log.info("Deleted all certifications for profile ID {}", profileId);
    }

    /**
     * Update display order for certifications
     */
    @Transactional
    public void updateCertificationOrder(Long profileId, List<Long> certificationIds) {
        log.debug("Updating certification order for profile ID: {}", profileId);

        for (int i = 0; i < certificationIds.size(); i++) {
            Long certId = certificationIds.get(i);
            Certification cert = certificationRepository
                    .findByIdAndProfileId(certId, profileId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Certification not found with ID: " + certId
                    ));
            cert.setDisplayOrder(i + 1);
            certificationRepository.save(cert);
        }

        log.info("Updated certification order for profile ID {}", profileId);
    }
}