package com.deryncullen.resume.service;

import com.deryncullen.resume.dto.CertificationDTO;
import com.deryncullen.resume.dto.CreateProfileRequest;
import com.deryncullen.resume.dto.EducationDTO;
import com.deryncullen.resume.dto.ExperienceDTO;
import com.deryncullen.resume.dto.ProfileDTO;
import com.deryncullen.resume.dto.SkillDTO;
import com.deryncullen.resume.dto.UpdateProfileRequest;
import com.deryncullen.resume.model.Certification;
import com.deryncullen.resume.model.Education;
import com.deryncullen.resume.model.Experience;
import com.deryncullen.resume.model.Profile;
import com.deryncullen.resume.model.Skill;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-31T16:55:09+0000",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.jar, environment: Java 22 (Oracle Corporation)"
)
@Component
public class ProfileMapperImpl implements ProfileMapper {

    @Override
    public ExperienceDTO toDto(Experience experience) {
        if ( experience == null ) {
            return null;
        }

        ExperienceDTO.ExperienceDTOBuilder experienceDTO = ExperienceDTO.builder();

        experienceDTO.id( experience.getId() );
        experienceDTO.companyName( experience.getCompanyName() );
        experienceDTO.jobTitle( experience.getJobTitle() );
        experienceDTO.location( experience.getLocation() );
        experienceDTO.startDate( experience.getStartDate() );
        experienceDTO.endDate( experience.getEndDate() );
        experienceDTO.current( experience.isCurrent() );
        experienceDTO.description( experience.getDescription() );
        List<String> list = experience.getAchievements();
        if ( list != null ) {
            experienceDTO.achievements( new ArrayList<String>( list ) );
        }
        List<String> list1 = experience.getTechnologies();
        if ( list1 != null ) {
            experienceDTO.technologies( new ArrayList<String>( list1 ) );
        }
        experienceDTO.displayOrder( experience.getDisplayOrder() );

        experienceDTO.duration( experience.getDuration() );
        experienceDTO.formattedDateRange( experience.getFormattedDateRange() );

        return experienceDTO.build();
    }

    @Override
    public CertificationDTO toDto(Certification certification) {
        if ( certification == null ) {
            return null;
        }

        CertificationDTO.CertificationDTOBuilder certificationDTO = CertificationDTO.builder();

        certificationDTO.id( certification.getId() );
        certificationDTO.name( certification.getName() );
        certificationDTO.issuingOrganization( certification.getIssuingOrganization() );
        certificationDTO.credentialId( certification.getCredentialId() );
        certificationDTO.credentialUrl( certification.getCredentialUrl() );
        certificationDTO.dateObtained( certification.getDateObtained() );
        certificationDTO.expirationDate( certification.getExpirationDate() );
        certificationDTO.doesNotExpire( certification.isDoesNotExpire() );
        certificationDTO.description( certification.getDescription() );
        certificationDTO.displayOrder( certification.getDisplayOrder() );

        certificationDTO.expired( certification.isExpired() );
        certificationDTO.expiringSoon( certification.isExpiringSoon() );

        return certificationDTO.build();
    }

    @Override
    public EducationDTO toDto(Education education) {
        if ( education == null ) {
            return null;
        }

        EducationDTO.EducationDTOBuilder educationDTO = EducationDTO.builder();

        educationDTO.id( education.getId() );
        educationDTO.institutionName( education.getInstitutionName() );
        educationDTO.degree( education.getDegree() );
        educationDTO.fieldOfStudy( education.getFieldOfStudy() );
        educationDTO.startDate( education.getStartDate() );
        educationDTO.graduationDate( education.getGraduationDate() );
        educationDTO.grade( education.getGrade() );
        educationDTO.description( education.getDescription() );
        educationDTO.displayOrder( education.getDisplayOrder() );

        return educationDTO.build();
    }

    @Override
    public SkillDTO toDto(Skill skill) {
        if ( skill == null ) {
            return null;
        }

        SkillDTO.SkillDTOBuilder skillDTO = SkillDTO.builder();

        skillDTO.id( skill.getId() );
        skillDTO.name( skill.getName() );
        skillDTO.category( skill.getCategory() );
        skillDTO.proficiencyLevel( skill.getProficiencyLevel() );
        skillDTO.yearsOfExperience( skill.getYearsOfExperience() );
        skillDTO.displayOrder( skill.getDisplayOrder() );
        skillDTO.primary( skill.isPrimary() );

        return skillDTO.build();
    }

    @Override
    public ProfileDTO toDto(Profile profile) {
        if ( profile == null ) {
            return null;
        }

        ProfileDTO.ProfileDTOBuilder profileDTO = ProfileDTO.builder();

        profileDTO.experiences( toExperienceDtoList( profile.getExperiences() ) );
        profileDTO.educations( toEducationDtoList( profile.getEducations() ) );
        profileDTO.skills( toSkillDtoList( profile.getSkills() ) );
        profileDTO.certifications( toCertificationDtoList( profile.getCertifications() ) );
        profileDTO.id( profile.getId() );
        profileDTO.firstName( profile.getFirstName() );
        profileDTO.lastName( profile.getLastName() );
        profileDTO.email( profile.getEmail() );
        profileDTO.phone( profile.getPhone() );
        profileDTO.location( profile.getLocation() );
        profileDTO.linkedInUrl( profile.getLinkedInUrl() );
        profileDTO.githubUrl( profile.getGithubUrl() );
        profileDTO.websiteUrl( profile.getWebsiteUrl() );
        profileDTO.title( profile.getTitle() );
        profileDTO.summary( profile.getSummary() );
        profileDTO.active( profile.isActive() );
        profileDTO.createdAt( profile.getCreatedAt() );
        profileDTO.updatedAt( profile.getUpdatedAt() );

        return profileDTO.build();
    }

    @Override
    public Profile toEntity(CreateProfileRequest request) {
        if ( request == null ) {
            return null;
        }

        Profile.ProfileBuilder profile = Profile.builder();

        profile.firstName( request.getFirstName() );
        profile.lastName( request.getLastName() );
        profile.email( request.getEmail() );
        profile.phone( request.getPhone() );
        profile.location( request.getLocation() );
        profile.linkedInUrl( request.getLinkedInUrl() );
        profile.githubUrl( request.getGithubUrl() );
        profile.websiteUrl( request.getWebsiteUrl() );
        profile.title( request.getTitle() );
        profile.summary( request.getSummary() );

        profile.active( true );

        return profile.build();
    }

    @Override
    public void updateProfileFromRequest(Profile profile, UpdateProfileRequest request) {
        if ( request == null ) {
            return;
        }

        if ( request.getFirstName() != null ) {
            profile.setFirstName( request.getFirstName() );
        }
        if ( request.getLastName() != null ) {
            profile.setLastName( request.getLastName() );
        }
        if ( request.getEmail() != null ) {
            profile.setEmail( request.getEmail() );
        }
        if ( request.getPhone() != null ) {
            profile.setPhone( request.getPhone() );
        }
        if ( request.getLocation() != null ) {
            profile.setLocation( request.getLocation() );
        }
        if ( request.getLinkedInUrl() != null ) {
            profile.setLinkedInUrl( request.getLinkedInUrl() );
        }
        if ( request.getGithubUrl() != null ) {
            profile.setGithubUrl( request.getGithubUrl() );
        }
        if ( request.getWebsiteUrl() != null ) {
            profile.setWebsiteUrl( request.getWebsiteUrl() );
        }
        if ( request.getTitle() != null ) {
            profile.setTitle( request.getTitle() );
        }
        if ( request.getSummary() != null ) {
            profile.setSummary( request.getSummary() );
        }
        if ( request.getActive() != null ) {
            profile.setActive( request.getActive() );
        }
    }

    @Override
    public Experience toEntity(ExperienceDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Experience.ExperienceBuilder experience = Experience.builder();

        experience.companyName( dto.getCompanyName() );
        experience.jobTitle( dto.getJobTitle() );
        experience.location( dto.getLocation() );
        experience.startDate( dto.getStartDate() );
        experience.endDate( dto.getEndDate() );
        experience.current( dto.isCurrent() );
        experience.description( dto.getDescription() );
        List<String> list = dto.getAchievements();
        if ( list != null ) {
            experience.achievements( new ArrayList<String>( list ) );
        }
        List<String> list1 = dto.getTechnologies();
        if ( list1 != null ) {
            experience.technologies( new ArrayList<String>( list1 ) );
        }
        experience.displayOrder( dto.getDisplayOrder() );

        return experience.build();
    }

    @Override
    public Education toEntity(EducationDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Education.EducationBuilder education = Education.builder();

        education.institutionName( dto.getInstitutionName() );
        education.degree( dto.getDegree() );
        education.fieldOfStudy( dto.getFieldOfStudy() );
        education.startDate( dto.getStartDate() );
        education.graduationDate( dto.getGraduationDate() );
        education.grade( dto.getGrade() );
        education.description( dto.getDescription() );
        education.displayOrder( dto.getDisplayOrder() );

        return education.build();
    }

    @Override
    public Skill toEntity(SkillDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Skill.SkillBuilder skill = Skill.builder();

        skill.name( dto.getName() );
        skill.category( dto.getCategory() );
        skill.proficiencyLevel( dto.getProficiencyLevel() );
        skill.yearsOfExperience( dto.getYearsOfExperience() );
        skill.displayOrder( dto.getDisplayOrder() );
        skill.primary( dto.isPrimary() );

        return skill.build();
    }

    @Override
    public Certification toEntity(CertificationDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Certification.CertificationBuilder certification = Certification.builder();

        certification.name( dto.getName() );
        certification.issuingOrganization( dto.getIssuingOrganization() );
        certification.credentialId( dto.getCredentialId() );
        certification.credentialUrl( dto.getCredentialUrl() );
        certification.dateObtained( dto.getDateObtained() );
        certification.expirationDate( dto.getExpirationDate() );
        certification.doesNotExpire( dto.isDoesNotExpire() );
        certification.description( dto.getDescription() );
        certification.displayOrder( dto.getDisplayOrder() );

        return certification.build();
    }

    @Override
    public List<ProfileDTO> toDtoList(List<Profile> profiles) {
        if ( profiles == null ) {
            return null;
        }

        List<ProfileDTO> list = new ArrayList<ProfileDTO>( profiles.size() );
        for ( Profile profile : profiles ) {
            list.add( toDto( profile ) );
        }

        return list;
    }

    @Override
    public List<ExperienceDTO> toExperienceDtoList(List<Experience> experiences) {
        if ( experiences == null ) {
            return null;
        }

        List<ExperienceDTO> list = new ArrayList<ExperienceDTO>( experiences.size() );
        for ( Experience experience : experiences ) {
            list.add( toDto( experience ) );
        }

        return list;
    }

    @Override
    public List<EducationDTO> toEducationDtoList(List<Education> educations) {
        if ( educations == null ) {
            return null;
        }

        List<EducationDTO> list = new ArrayList<EducationDTO>( educations.size() );
        for ( Education education : educations ) {
            list.add( toDto( education ) );
        }

        return list;
    }

    @Override
    public List<SkillDTO> toSkillDtoList(List<Skill> skills) {
        if ( skills == null ) {
            return null;
        }

        List<SkillDTO> list = new ArrayList<SkillDTO>( skills.size() );
        for ( Skill skill : skills ) {
            list.add( toDto( skill ) );
        }

        return list;
    }

    @Override
    public List<CertificationDTO> toCertificationDtoList(List<Certification> certifications) {
        if ( certifications == null ) {
            return null;
        }

        List<CertificationDTO> list = new ArrayList<CertificationDTO>( certifications.size() );
        for ( Certification certification : certifications ) {
            list.add( toDto( certification ) );
        }

        return list;
    }
}
