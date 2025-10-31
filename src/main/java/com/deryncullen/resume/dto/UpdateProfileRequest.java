package com.deryncullen.resume.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {
    
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;
    
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;
    
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;
    
    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;
    
    @URL(message = "LinkedIn URL must be valid")
    @Size(max = 255, message = "LinkedIn URL must not exceed 255 characters")
    private String linkedInUrl;
    
    @URL(message = "GitHub URL must be valid")
    @Size(max = 255, message = "GitHub URL must not exceed 255 characters")
    private String githubUrl;
    
    @URL(message = "Website URL must be valid")
    @Size(max = 255, message = "Website URL must not exceed 255 characters")
    private String websiteUrl;
    
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @Size(max = 5000, message = "Summary must not exceed 5000 characters")
    private String summary;
    
    private Boolean active;
}
