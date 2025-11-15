package com.deryncullen.resume.controller;

import com.deryncullen.resume.dto.CertificationDTO;
import com.deryncullen.resume.service.CertificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
@RequestMapping("/api/profiles/{profileId}/certifications")
@Tag(name = "Certification", description = "Certification management endpoints")
@RequiredArgsConstructor
@Slf4j
public class CertificationController {

    private final CertificationService certificationService;

    @PostMapping
    @Operation(summary = "Add certification to profile", description = "Creates a new certification for the specified profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Certification created successfully",
                    content = @Content(schema = @Schema(implementation = CertificationDTO.class))),
            @ApiResponse(responseCode = "404", description = "Profile not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate certification"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<CertificationDTO> addCertification(
            @Parameter(description = "Profile ID") @PathVariable Long profileId,
            @Parameter(description = "Certification data") @Valid @RequestBody CertificationDTO certificationDTO) {
        log.info("Adding certification to profile ID: {}", profileId);
        CertificationDTO created = certificationService.addCertification(profileId, certificationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Get all certifications", description = "Returns all certifications for the specified profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certifications retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CertificationDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<List<CertificationDTO>> getCertifications(
            @Parameter(description = "Profile ID") @PathVariable Long profileId) {
        log.info("Fetching certifications for profile ID: {}", profileId);
        List<CertificationDTO> certifications = certificationService.getCertificationsByProfileId(profileId);
        return ResponseEntity.ok(certifications);
    }

    @GetMapping("/expired")
    @Operation(summary = "Get expired certifications", description = "Returns expired certifications for the specified profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expired certifications retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CertificationDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<List<CertificationDTO>> getExpiredCertifications(
            @Parameter(description = "Profile ID") @PathVariable Long profileId) {
        log.info("Fetching expired certifications for profile ID: {}", profileId);
        List<CertificationDTO> expired = certificationService.getExpiredCertifications(profileId);
        return ResponseEntity.ok(expired);
    }

    @GetMapping("/expiring-soon")
    @Operation(summary = "Get certifications expiring soon",
            description = "Returns certifications expiring within the next 3 months for the specified profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expiring certifications retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CertificationDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<List<CertificationDTO>> getExpiringSoonCertifications(
            @Parameter(description = "Profile ID") @PathVariable Long profileId) {
        log.info("Fetching expiring certifications for profile ID: {}", profileId);
        List<CertificationDTO> expiringSoon = certificationService.getCertificationsExpiringSoon(profileId);
        return ResponseEntity.ok(expiringSoon);
    }

    @GetMapping("/organization/{organization}")
    @Operation(summary = "Get certifications by organization",
            description = "Returns certifications issued by a specific organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certifications retrieved by organization",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CertificationDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<List<CertificationDTO>> getCertificationsByOrganization(
            @Parameter(description = "Profile ID") @PathVariable Long profileId,
            @Parameter(description = "Issuing Organization") @PathVariable String organization) {
        log.info("Fetching certifications from {} for profile ID: {}", organization, profileId);
        List<CertificationDTO> certifications = certificationService.getCertificationsByOrganization(profileId, organization);
        return ResponseEntity.ok(certifications);
    }

    @PutMapping("/{certificationId}")
    @Operation(summary = "Update certification", description = "Updates an existing certification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certification updated successfully",
                    content = @Content(schema = @Schema(implementation = CertificationDTO.class))),
            @ApiResponse(responseCode = "404", description = "Certification or profile not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<CertificationDTO> updateCertification(
            @Parameter(description = "Profile ID") @PathVariable Long profileId,
            @Parameter(description = "Certification ID") @PathVariable Long certificationId,
            @Parameter(description = "Updated certification data") @Valid @RequestBody CertificationDTO certificationDTO) {
        log.info("Updating certification ID {} for profile ID {}", certificationId, profileId);
        CertificationDTO updated = certificationService.updateCertification(profileId, certificationId, certificationDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{certificationId}")
    @Operation(summary = "Delete certification", description = "Removes a certification from the profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Certification deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Certification or profile not found")
    })
    public ResponseEntity<Void> deleteCertification(
            @Parameter(description = "Profile ID") @PathVariable Long profileId,
            @Parameter(description = "Certification ID") @PathVariable Long certificationId) {
        log.info("Deleting certification ID {} for profile ID {}", certificationId, profileId);
        certificationService.deleteCertification(profileId, certificationId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Delete all certifications", description = "Removes all certifications from the profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "All certifications deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<Void> deleteAllCertifications(
            @Parameter(description = "Profile ID") @PathVariable Long profileId) {
        log.info("Deleting all certifications for profile ID: {}", profileId);
        certificationService.deleteAllCertificationsByProfileId(profileId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/order")
    @Operation(summary = "Update certification display order",
            description = "Updates the display order for multiple certifications")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order updated successfully"),
            @ApiResponse(responseCode = "404", description = "Certification or profile not found")
    })
    public ResponseEntity<Void> updateCertificationOrder(
            @Parameter(description = "Profile ID") @PathVariable Long profileId,
            @Parameter(description = "Ordered list of certification IDs") @RequestBody List<Long> certificationIds) {
        log.info("Updating certification order for profile ID: {}", profileId);
        certificationService.updateCertificationOrder(profileId, certificationIds);
        return ResponseEntity.noContent().build();
    }
}