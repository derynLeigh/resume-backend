package com.deryncullen.resume.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"experiences", "educations", "skills", "certifications"})
@EqualsAndHashCode(exclude = {"experiences", "educations", "skills", "certifications"})
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255)
    @Column(nullable = false, unique = true)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 255)
    private String location;

    @URL(message = "LinkedIn URL must be valid")
    @Size(max = 255)
    @Column(name = "linkedin_url")
    private String linkedInUrl;

    @URL(message = "GitHub URL must be valid")
    @Size(max = 255)
    @Column(name = "github_url")
    private String githubUrl;

    @URL(message = "Website URL must be valid")
    @Size(max = 255)
    @Column(name = "website_url")
    private String websiteUrl;

    @NotBlank(message = "Professional title is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String summary;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("startDate DESC")
    @Builder.Default
    private List<Experience> experiences = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("graduationDate DESC")
    @Builder.Default
    private List<Education> educations = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder")
    @Builder.Default
    private List<Skill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("dateObtained DESC")
    @Builder.Default
    private List<Certification> certifications = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods for managing relationships
    public void addExperience(Experience experience) {
        experiences.add(experience);
        experience.setProfile(this);
    }

    public void removeExperience(Experience experience) {
        experiences.remove(experience);
        experience.setProfile(null);
    }

    public void addEducation(Education education) {
        educations.add(education);
        education.setProfile(this);
    }

    public void removeEducation(Education education) {
        educations.remove(education);
        education.setProfile(null);
    }

    public void addSkill(Skill skill) {
        skills.add(skill);
        skill.setProfile(this);
    }

    public void removeSkill(Skill skill) {
        skills.remove(skill);
        skill.setProfile(null);
    }

    public void addCertification(Certification certification) {
        certifications.add(certification);
        certification.setProfile(this);
    }

    public void removeCertification(Certification certification) {
        certifications.remove(certification);
        certification.setProfile(null);
    }
}