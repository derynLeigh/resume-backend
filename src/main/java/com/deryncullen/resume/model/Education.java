package com.deryncullen.resume.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "educations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "profile")
@EqualsAndHashCode(exclude = "profile")
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Institution name is required")
    @Size(max = 255)
    @Column(name = "institution_name", nullable = false)
    private String institutionName;

    @NotBlank(message = "Degree is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String degree;

    @Size(max = 255)
    @Column(name = "field_of_study")
    private String fieldOfStudy;

    @PastOrPresent(message = "Start date cannot be in the future")
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "graduation_date")
    private LocalDate graduationDate;

    @Size(max = 10)
    private String grade;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "display_order")
    private Integer displayOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // Validation method
    @AssertTrue(message = "Graduation date must be after start date")
    public boolean isGraduationDateValid() {
        if (graduationDate == null || startDate == null) {
            return true;
        }
        return graduationDate.isAfter(startDate) || graduationDate.isEqual(startDate);
    }
}