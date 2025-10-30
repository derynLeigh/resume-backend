package com.deryncullen.resume.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "certifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "profile")
@EqualsAndHashCode(exclude = "profile")
public class Certification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Certification name is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Issuing organization is required")
    @Size(max = 255)
    @Column(name = "issuing_organization", nullable = false)
    private String issuingOrganization;

    @Size(max = 100)
    @Column(name = "credential_id")
    private String credentialId;

    @URL(message = "Credential URL must be valid")
    @Size(max = 500)
    @Column(name = "credential_url")
    private String credentialUrl;

    @NotNull(message = "Date obtained is required")
    @PastOrPresent(message = "Date obtained cannot be in the future")
    @Column(name = "date_obtained", nullable = false)
    private LocalDate dateObtained;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "does_not_expire")
    @Builder.Default
    private boolean doesNotExpire = false;

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

    // Business logic methods
    public boolean isExpired() {
        if (doesNotExpire || expirationDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(expirationDate);
    }

    public boolean isExpiringSoon() {
        if (doesNotExpire || expirationDate == null) {
            return false;
        }
        return LocalDate.now().plusMonths(3).isAfter(expirationDate) && !isExpired();
    }

    // Validation method
    @AssertTrue(message = "Expiration date must be after date obtained")
    public boolean isExpirationDateValid() {
        if (doesNotExpire || expirationDate == null || dateObtained == null) {
            return true;
        }
        return expirationDate.isAfter(dateObtained);
    }
}