package com.deryncullen.resume.dto;

import com.deryncullen.resume.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Set<Role> roles;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
