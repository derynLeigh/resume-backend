package com.deryncullen.resume.dto;

import com.deryncullen.resume.model.Skill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillDTO {
    
    private Long id;
    private String name;
    private Skill.SkillCategory category;
    private Skill.ProficiencyLevel proficiencyLevel;
    private Integer yearsOfExperience;
    private Integer displayOrder;
    private boolean primary;
    
    // For display purposes
    public String getCategoryDisplayName() {
        return category != null ? category.getDisplayName() : null;
    }
    
    public String getProficiencyDisplayName() {
        return proficiencyLevel != null ? proficiencyLevel.getDisplayName() : null;
    }
}
