package com.deryncullen.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificationDTO {
    
    private Long id;
    private String name;
    private String issuingOrganization;
    private String credentialId;
    private String credentialUrl;
    private LocalDate dateObtained;
    private LocalDate expirationDate;
    private boolean doesNotExpire;
    private String description;
    private Integer displayOrder;
    
    // Computed fields
    private boolean expired;
    private boolean expiringSoon;
}
