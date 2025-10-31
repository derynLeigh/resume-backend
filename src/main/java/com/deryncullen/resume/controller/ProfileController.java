package com.deryncullen.resume.controller;

import com.deryncullen.resume.dto.*;
import com.deryncullen.resume.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Profile", description = "Profile management API")
public class ProfileController {
    
    private final ProfileService profileService;
    
    @PostMapping
    @Operation(summary = "Create a new profile", description = "Creates a new resume profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Profile created successfully",
            content = @Content(schema = @Schema(implementation = ProfileDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Profile with email already exists")
    })
    public ResponseEntity<ProfileDTO> createProfile(@Valid @RequestBody CreateProfileRequest request) {
        log.info("Creating new profile for: {} {}", request.getFirstName(), request.getLastName());
        ProfileDTO profile = profileService.createProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(profile);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get profile by ID", description = "Returns a profile by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile found",
            content = @Content(schema = @Schema(implementation = ProfileDTO.class))),
        @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<ProfileDTO> getProfileById(
            @Parameter(description = "Profile ID") @PathVariable Long id) {
        log.info("Fetching profile with ID: {}", id);
        ProfileDTO profile = profileService.getProfileById(id);
        return ResponseEntity.ok(profile);
    }
    
    @GetMapping("/email/{email}")
    @Operation(summary = "Get profile by email", description = "Returns a profile by email address")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile found",
            content = @Content(schema = @Schema(implementation = ProfileDTO.class))),
        @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<ProfileDTO> getProfileByEmail(
            @Parameter(description = "Email address") @PathVariable String email) {
        log.info("Fetching profile with email: {}", email);
        ProfileDTO profile = profileService.getProfileByEmail(email);
        return ResponseEntity.ok(profile);
    }
    
    @GetMapping("/active")
    @Operation(summary = "Get all active profiles", description = "Returns all active profiles")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active profiles retrieved",
            content = @Content(schema = @Schema(implementation = ProfileDTO.class)))
    })
    public ResponseEntity<List<ProfileDTO>> getAllActiveProfiles() {
        log.info("Fetching all active profiles");
        List<ProfileDTO> profiles = profileService.getAllActiveProfiles();
        return ResponseEntity.ok(profiles);
    }
    
    @GetMapping("/{id}/full")
    @Operation(summary = "Get profile with all relations", 
               description = "Returns a profile with all experiences, education, skills, and certifications")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile found with all relations",
            content = @Content(schema = @Schema(implementation = ProfileDTO.class))),
        @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<ProfileDTO> getProfileWithAllRelations(
            @Parameter(description = "Profile ID") @PathVariable Long id) {
        log.info("Fetching profile with all relations for ID: {}", id);
        ProfileDTO profile = profileService.getProfileWithAllRelations(id);
        return ResponseEntity.ok(profile);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update profile", description = "Updates an existing profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully",
            content = @Content(schema = @Schema(implementation = ProfileDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Profile not found"),
        @ApiResponse(responseCode = "409", description = "Email already in use")
    })
    public ResponseEntity<ProfileDTO> updateProfile(
            @Parameter(description = "Profile ID") @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequest request) {
        log.info("Updating profile with ID: {}", id);
        ProfileDTO profile = profileService.updateProfile(id, request);
        return ResponseEntity.ok(profile);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete profile", description = "Permanently deletes a profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Profile deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<Void> deleteProfile(
            @Parameter(description = "Profile ID") @PathVariable Long id) {
        log.info("Deleting profile with ID: {}", id);
        profileService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate profile", description = "Soft deletes a profile by setting active to false")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Profile deactivated successfully"),
        @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<Void> deactivateProfile(
            @Parameter(description = "Profile ID") @PathVariable Long id) {
        log.info("Deactivating profile with ID: {}", id);
        profileService.softDeleteProfile(id);
        return ResponseEntity.noContent().build();
    }
}
