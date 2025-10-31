package com.deryncullen.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileDTO {
    
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String location;
    private String linkedInUrl;
    private String githubUrl;
    private String websiteUrl;
    private String title;
    private String summary;
    private boolean active;
    
    private List<ExperienceDTO> experiences;
    private List<EducationDTO> educations;
    private List<SkillDTO> skills;
    private List<CertificationDTO> certifications;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed field
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
