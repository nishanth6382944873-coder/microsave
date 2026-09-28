package com.microsave.controller;

import com.microsave.dto.ContributionRequest;
import com.microsave.dto.ContributionResponse;
import com.microsave.service.ContributionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contributions")
@Tag(name = "Contributions", description = "Endpoints for recording and tracking weekly savings contributions")
public class ContributionController {

    private final ContributionService contributionService;

    public ContributionController(ContributionService contributionService) {
        this.contributionService = contributionService;
    }

    @PostMapping
    @Operation(summary = "Record a savings contribution", description = "Records a savings contribution by a member into the group pool")
    public ResponseEntity<ContributionResponse> recordContribution(@Valid @RequestBody ContributionRequest request) {
        ContributionResponse recorded = contributionService.recordContribution(request);
        return new ResponseEntity<>(recorded, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all contributions", description = "Retrieves all savings contributions, with optional filtering by groupId or memberId")
    public ResponseEntity<List<ContributionResponse>> getAllContributions(
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) Long memberId) {
        if (groupId != null) {
            return ResponseEntity.ok(contributionService.getContributionsByGroupId(groupId));
        }
        if (memberId != null) {
            return ResponseEntity.ok(contributionService.getContributionsByMemberId(memberId));
        }
        return ResponseEntity.ok(contributionService.getAllContributions());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get contribution by ID", description = "Retrieves a single contribution record by its ID")
    public ResponseEntity<ContributionResponse> getContributionById(@PathVariable Long id) {
        return ResponseEntity.ok(contributionService.getContributionById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update contribution", description = "Updates details of an existing contribution")
    public ResponseEntity<ContributionResponse> updateContribution(@PathVariable Long id, @Valid @RequestBody ContributionRequest request) {
        return ResponseEntity.ok(contributionService.updateContribution(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete contribution", description = "Deletes a contribution record by its ID")
    public ResponseEntity<Void> deleteContribution(@PathVariable Long id) {
        contributionService.deleteContribution(id);
        return ResponseEntity.noContent().build();
    }
}
