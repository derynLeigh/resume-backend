package com.deryncullen.resume.controller;

import com.deryncullen.resume.dto.ExperienceDTO;
import com.deryncullen.resume.service.ExperienceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/profiles/{profileId}/experiences")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Experience", description = "Experience management API")
public class ExperienceController {

    private final ExperienceService experienceService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Create a new experience", description = "Creates a new work experience for a profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Experience created successfully",
            content = @Content(schema = @Schema(implementation = ExperienceDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Profile not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ExperienceDTO> createExperience(
            @Parameter(description = "Profile ID") @PathVariable Long profileId,
            @Valid @RequestBody ExperienceDTO dto) {
        log.info("Creating experience for profile ID: {}", profileId);
        ExperienceDTO created = experienceService.createExperience(profileId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Get all experiences for a profile", 
               description = "Returns all work experiences for a profile, ordered by start date")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Experiences retrieved successfully",
            content = @Content(schema = @Schema(implementation = ExperienceDTO.class))),
        @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<List<ExperienceDTO>> getExperiences(
            @Parameter(description = "Profile ID") @PathVariable Long profileId) {
        log.info("Fetching experiences for profile ID: {}", profileId);
        List<ExperienceDTO> experiences = experienceService.getExperiencesByProfileId(profileId);
        return ResponseEntity.ok(experiences);
    }

    @GetMapping("/current")
    @Operation(summary = "Get current experiences", 
               description = "Returns only current work experiences for a profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Current experiences retrieved successfully",
            content = @Content(schema = @Schema(implementation = ExperienceDTO.class))),
        @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<List<ExperienceDTO>> getCurrentExperiences(
            @Parameter(description = "Profile ID") @PathVariable Long profileId) {
        log.info("Fetching current experiences for profile ID: {}", profileId);
        List<ExperienceDTO> experiences = experienceService.getCurrentExperiences(profileId);
        return ResponseEntity.ok(experiences);
    }

    @GetMapping("/{experienceId}")
    @Operation(summary = "Get experience by ID", description = "Returns a specific work experience")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Experience found",
            content = @Content(schema = @Schema(implementation = ExperienceDTO.class))),
        @ApiResponse(responseCode = "404", description = "Experience or profile not found")
    })
    public ResponseEntity<ExperienceDTO> getExperienceById(
            @Parameter(description = "Profile ID") @PathVariable Long profileId,
            @Parameter(description = "Experience ID") @PathVariable Long experienceId) {
        log.info("Fetching experience ID: {} for profile ID: {}", experienceId, profileId);
        ExperienceDTO experience = experienceService.getExperienceById(profileId, experienceId);
        return ResponseEntity.ok(experience);
    }

    @PutMapping("/{experienceId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Update experience", description = "Updates an existing work experience")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Experience updated successfully",
            content = @Content(schema = @Schema(implementation = ExperienceDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Experience or profile not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ExperienceDTO> updateExperience(
            @Parameter(description = "Profile ID") @PathVariable Long profileId,
            @Parameter(description = "Experience ID") @PathVariable Long experienceId,
            @Valid @RequestBody ExperienceDTO dto) {
        log.info("Updating experience ID: {} for profile ID: {}", experienceId, profileId);
        ExperienceDTO updated = experienceService.updateExperience(profileId, experienceId, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{experienceId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Delete experience", description = "Permanently deletes a work experience")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Experience deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Experience or profile not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteExperience(
            @Parameter(description = "Profile ID") @PathVariable Long profileId,
            @Parameter(description = "Experience ID") @PathVariable Long experienceId) {
        log.info("Deleting experience ID: {} for profile ID: {}", experienceId, profileId);
        experienceService.deleteExperience(profileId, experienceId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Reorder experiences", 
               description = "Updates the display order of experiences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Experiences reordered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Profile not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> reorderExperiences(
            @Parameter(description = "Profile ID") @PathVariable Long profileId,
            @RequestBody Map<String, List<Long>> request) {
        log.info("Reordering experiences for profile ID: {}", profileId);
        List<Long> orderedIds = request.get("orderedIds");
        experienceService.reorderExperiences(profileId, orderedIds);
        return ResponseEntity.noContent().build();
    }
}
