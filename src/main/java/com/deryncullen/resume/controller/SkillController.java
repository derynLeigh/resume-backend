package com.deryncullen.resume.controller;

import com.deryncullen.resume.dto.SkillDTO;
import com.deryncullen.resume.service.SkillService;
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
@RequestMapping("/profiles/{profileId}/skills")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Skill", description = "Skill management API")
public class SkillController {

    private final SkillService skillService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Create a new skill", description = "Creates a new skill for a profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Skill created successfully",
                    content = @Content(schema = @Schema(implementation = SkillDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Profile not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<SkillDTO> createSkill(
            @Parameter(description = "Profile ID") @PathVariable Long profileId,
            @Valid @RequestBody SkillDTO dto) {
        log.info("Creating skill for profile ID: {}", profileId);
        SkillDTO created = skillService.createSkill(profileId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    @GetMapping
    public ResponseEntity<List<SkillDTO>> getSkills(
            @Parameter(description = "Profile ID") @PathVariable Long profileId) {
        log.info("Fetching skills for profile ID: {}", profileId);
        List<SkillDTO> skills = skillService.getSkillsByProfileId(profileId);
        return ResponseEntity.ok(skills);
    }
    @GetMapping("/primary")
    @Operation(summary = "Get primary skills",
            description = "Returns only primary work skills for a profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Primary skills retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SkillDTO.class))),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<List<SkillDTO>> getPrimarySkills(
            @Parameter(description = "Profile ID") @PathVariable Long profileId) {
        log.info("Fetching primary skills for profile ID: {}", profileId);
        List<SkillDTO> skills = skillService.getPrimarySkills(profileId);
        return ResponseEntity.ok(skills);
    }

    @GetMapping("/{skillId}")
    @Operation(summary = "Get skill by ID", description = "Returns a specific work skill")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Skill found",
                    content = @Content(schema = @Schema(implementation = SkillDTO.class))),
            @ApiResponse(responseCode = "404", description = "Skill or profile not found")
    })
    public ResponseEntity<SkillDTO> getSkillById(
            @Parameter(description = "Profile ID") @PathVariable Long profileId,
            @Parameter(description = "Skill ID") @PathVariable Long skillId) {
        log.info("Fetching skill ID: {} for profile ID: {}", skillId, profileId);
        SkillDTO skill = skillService.getSkillById(profileId, skillId);
        return ResponseEntity.ok(skill);
    }

    @PutMapping("/{skillId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Update skill", description = "Updates an existing work skill")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Skill updated successfully",
                    content = @Content(schema = @Schema(implementation = SkillDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Skill or profile not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<SkillDTO> updateSkill(
            @Parameter(description = "Profile ID") @PathVariable Long profileId,
            @Parameter(description = "Skill ID") @PathVariable Long skillId,
            @Valid @RequestBody SkillDTO dto) {
        log.info("Updating skill ID: {} for profile ID: {}", skillId, profileId);
        SkillDTO updated = skillService.updateSkill(profileId, skillId, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{skillId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Delete skill", description = "Permanently deletes a work skill")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Skill deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Skill or profile not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteSkill(
            @Parameter(description = "Profile ID") @PathVariable Long profileId,
            @Parameter(description = "Skill ID") @PathVariable Long skillId) {
        log.info("Deleting skill ID: {} for profile ID: {}", skillId, profileId);
        skillService.deleteSkill(profileId, skillId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Reorder skills",
            description = "Updates the display order of skills")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Skills reordered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Profile not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> reorderSkills(
            @Parameter(description = "Profile ID") @PathVariable Long profileId,
            @RequestBody Map<String, List<Long>> request) {
        log.info("Reordering skills for profile ID: {}", profileId);
        List<Long> orderedIds = request.get("orderedIds");
        skillService.reorderSkills(profileId, orderedIds);
        return ResponseEntity.noContent().build();
    }
}