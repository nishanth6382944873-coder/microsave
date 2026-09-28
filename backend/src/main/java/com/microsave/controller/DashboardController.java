package com.microsave.controller;

import com.microsave.dto.DashboardResponse;
import com.microsave.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Endpoints for group financial summaries and pool calculations")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/{groupId}")
    @Operation(summary = "Get group dashboard summary", description = "Calculates total members, total savings, total loans, outstanding loans, and available group pool")
    public ResponseEntity<DashboardResponse> getDashboardSummary(@PathVariable Long groupId) {
        return ResponseEntity.ok(dashboardService.getDashboardSummary(groupId));
    }
}
