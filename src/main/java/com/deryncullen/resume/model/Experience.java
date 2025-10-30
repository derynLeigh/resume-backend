package com.deryncullen.resume.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "experiences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "profile")
@EqualsAndHashCode(exclude = "profile")
public class Experience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Company name is required")
    @Size(max = 255)
    @Column(name = "company_name", nullable = false)
    private String companyName;

    @NotBlank(message = "Job title is required")
    @Size(max = 255)
    @Column(name = "job_title", nullable = false)
    private String jobTitle;

    @Size(max = 255)
    private String location;

    @NotNull(message = "Start date is required")
    @PastOrPresent(message = "Start date cannot be in the future")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_current", nullable = false)
    @Builder.Default
    private boolean current = false;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(
            name = "experience_achievements",
            joinColumns = @JoinColumn(name = "experience_id")
    )
    @Column(name = "achievement", columnDefinition = "TEXT")
    @OrderColumn(name = "display_order")
    @Builder.Default
    private List<String> achievements = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "experience_technologies",
            joinColumns = @JoinColumn(name = "experience_id")
    )
    @Column(name = "technology")
    @Builder.Default
    private List<String> technologies = new ArrayList<>();

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

    // Validation method for end date
    @AssertTrue(message = "End date must be after start date")
    public boolean isEndDateValid() {
        if (endDate == null || startDate == null) {
            return true;
        }
        return endDate.isAfter(startDate);
    }

    // Business logic methods
    public void setCurrent(boolean current) {
        this.current = current;
        if (current) {
            this.endDate = null;
        }
    }

    public String getDuration() {
        LocalDate end = current ? LocalDate.now() : endDate;
        if (startDate == null || end == null) {
            return "";
        }

        // Calculate the period treating the end date as inclusive
        // We add 1 day to include the end date in the calculation
        Period period = Period.between(startDate, end.plusDays(1));

        int years = period.getYears();
        int months = period.getMonths();
        int days = period.getDays();

        // If we have any days remaining, round up to the next month
        if (days > 0) {
            months++;
            // Handle month overflow
            if (months >= 12) {
                years++;
                months -= 12;
            }
        }

        StringBuilder duration = new StringBuilder();
        if (years > 0) {
            duration.append(years).append(years == 1 ? " year" : " years");
            if (months > 0) {
                duration.append(", ");
            }
        }
        if (months > 0) {
            duration.append(months).append(months == 1 ? " month" : " months");
        }

        return duration.toString();
    }

    public String getFormattedDateRange() {
        if (startDate == null) {
            return "";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        String start = startDate.format(formatter);
        String end = current ? "Present" : (endDate != null ? endDate.format(formatter) : "");

        return start + " - " + end;
    }

    public String getTechnologiesAsString() {
        return technologies.stream()
                .collect(Collectors.joining(", "));
    }

    public void addAchievement(String achievement) {
        if (achievements == null) {
            achievements = new ArrayList<>();
        }
        achievements.add(achievement);
    }

    public void removeAchievement(String achievement) {
        if (achievements != null) {
            achievements.remove(achievement);
        }
    }

    public void addTechnology(String technology) {
        if (technologies == null) {
            technologies = new ArrayList<>();
        }
        technologies.add(technology);
    }

    public void removeTechnology(String technology) {
        if (technologies != null) {
            technologies.remove(technology);
        }
    }

    @PrePersist
    protected void onCreate() {
        if (current && endDate != null) {
            endDate = null;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (current && endDate != null) {
            endDate = null;
        }
    }
}