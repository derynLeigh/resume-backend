package com.deryncullen.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceDTO {
    
    private Long id;
    private String companyName;
    private String jobTitle;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean current;
    private String description;
    private List<String> achievements;
    private List<String> technologies;
    private Integer displayOrder;
    
    // Computed fields
    private String duration;
    private String formattedDateRange;
}
