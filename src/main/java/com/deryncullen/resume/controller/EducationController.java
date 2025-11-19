package com.deryncullen.resume.controller;

import com.deryncullen.resume.dto.EducationDTO;
import com.deryncullen.resume.service.EducationService;
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
@RequestMapping("/profiles/{profileId}/educations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Education", description = "Education management API")
public class EducationController {

    private final EducationService educationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Create a new education", description = "Creates a new education record for a profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Education created successfully",
                    content = @Content(schema = @Schema(implementation = EducationDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Profile not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<EducationDTO> createEducation(
            @Parameter(description = "Profile ID") @PathVariable Long profileId,
            @Valid @RequestBody EducationDTO dto) {
        log.info("Creating education for profile ID: {}", profileId);
        EducationDTO created = educationService.createEducation(profileId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Get all educations for a profile",
            description = "Returns all education records for a profile, ordered by graduation date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Educations retrieved successfully",
                    content = @Content(schema = @Schema(implementation = EducationDTO.class))),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<List<EducationDTO>> getEducations(
            @Parameter(description = "Profile ID") @PathVariable Long profileId) {
        log.info("Fetching educations for profile ID: {}", profileId);
        List<EducationDTO> educations = educationService.getEducationsByProfileId(profileId);
        return ResponseEntity.ok(educations);
    }

    @GetMapping("/{educationId}")
    @Operation(summary = "Get education by ID", description = "Returns a specific education record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Education found",
                    content = @Content(schema = @Schema(implementation = EducationDTO.class))),
            @ApiResponse(responseCode = "404", description = "Education or profile not found")
    })
    public ResponseEntity<EducationDTO> getEducationById(
            @Parameter(description = "Profile ID") @PathVariable Long profileId,
            @Parameter(description = "Education ID") @PathVariable Long educationId) {
        log.info("Fetching education ID: {} for profile ID: {}", educationId, profileId);
        EducationDTO education = educationService.getEducationById(profileId, educationId);
        return ResponseEntity.ok(education);
    }

    @PutMapping("/{educationId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Update education", description = "Updates an existing education record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Education updated successfully",
                    content = @Content(schema = @Schema(implementation = EducationDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Education or profile not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<EducationDTO> updateEducation(
            @Parameter(description = "Profile ID") @PathVariable Long profileId,
            @Parameter(description = "Education ID") @PathVariable Long educationId,
            @Valid @RequestBody EducationDTO dto) {
        log.info("Updating education ID: {} for profile ID: {}", educationId, profileId);
        EducationDTO updated = educationService.updateEducation(profileId, educationId, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{educationId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Delete education", description = "Permanently deletes an education record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Education deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Education or profile not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteEducation(
            @Parameter(description = "Profile ID") @PathVariable Long profileId,
            @Parameter(description = "Education ID") @PathVariable Long educationId) {
        log.info("Deleting education ID: {} for profile ID: {}", educationId, profileId);
        educationService.deleteEducation(profileId, educationId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Reorder educations",
            description = "Updates the display order of education records")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Educations reordered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Profile not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> reorderEducations(
            @Parameter(description = "Profile ID") @PathVariable Long profileId,
            @RequestBody Map<String, List<Long>> request) {
        log.info("Reordering educations for profile ID: {}", profileId);
        List<Long> orderedIds = request.get("orderedIds");
        educationService.reorderEducation(profileId, orderedIds);
        return ResponseEntity.noContent().build();
    }
}