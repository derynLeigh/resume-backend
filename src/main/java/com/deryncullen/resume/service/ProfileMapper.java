package com.deryncullen.resume.service;

import com.deryncullen.resume.dto.*;
import com.deryncullen.resume.model.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProfileMapper {
    
    // Profile mappings
    @Mapping(target = "duration", expression = "java(experience.getDuration())")
    @Mapping(target = "formattedDateRange", expression = "java(experience.getFormattedDateRange())")
    ExperienceDTO toDto(Experience experience);
    
    @Mapping(target = "expired", expression = "java(certification.isExpired())")
    @Mapping(target = "expiringSoon", expression = "java(certification.isExpiringSoon())")
    CertificationDTO toDto(Certification certification);
    
    EducationDTO toDto(Education education);
    
    SkillDTO toDto(Skill skill);
    
    @Mapping(target = "experiences", source = "experiences")
    @Mapping(target = "educations", source = "educations")
    @Mapping(target = "skills", source = "skills")
    @Mapping(target = "certifications", source = "certifications")
    ProfileDTO toDto(Profile profile);
    
    // Create/Update mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "experiences", ignore = true)
    @Mapping(target = "educations", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "certifications", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "active", constant = "true")
    Profile toEntity(CreateProfileRequest request);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "experiences", ignore = true)
    @Mapping(target = "educations", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "certifications", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateProfileFromRequest(@MappingTarget Profile profile, UpdateProfileRequest request);
    
    // Experience mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Experience toEntity(ExperienceDTO dto);
    
    // Education mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Education toEntity(EducationDTO dto);
    
    // Skill mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Skill toEntity(SkillDTO dto);
    
    // Certification mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Certification toEntity(CertificationDTO dto);
    
    // List mappings
    List<ProfileDTO> toDtoList(List<Profile> profiles);
    List<ExperienceDTO> toExperienceDtoList(List<Experience> experiences);
    List<EducationDTO> toEducationDtoList(List<Education> educations);
    List<SkillDTO> toSkillDtoList(List<Skill> skills);
    List<CertificationDTO> toCertificationDtoList(List<Certification> certifications);
}
