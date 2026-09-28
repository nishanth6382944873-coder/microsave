package com.microsave.controller;

import com.microsave.dto.LoanRequest;
import com.microsave.dto.LoanResponse;
import com.microsave.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
@Tag(name = "Loans", description = "Endpoints for disbursing and tracking internal SHG loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping
    @Operation(summary = "Disburse a new loan", description = "Disburses a loan to an eligible member, verifying available pool and active loan rules")
    public ResponseEntity<LoanResponse> disburseLoan(@Valid @RequestBody LoanRequest request) {
        LoanResponse disbursed = loanService.disburseLoan(request);
        return new ResponseEntity<>(disbursed, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all loans", description = "Retrieves all loans, with optional filtering by groupId or memberId")
    public ResponseEntity<List<LoanResponse>> getAllLoans(
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) Long memberId) {
        if (groupId != null) {
            return ResponseEntity.ok(loanService.getLoansByGroupId(groupId));
        }
        if (memberId != null) {
            return ResponseEntity.ok(loanService.getLoansByMemberId(memberId));
        }
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get loan by ID", description = "Retrieves details of a specific loan by its ID")
    public ResponseEntity<LoanResponse> getLoanById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }

    @GetMapping("/pool/{groupId}")
    @Operation(summary = "Get group available pool", description = "Calculates and returns the available pool for loan disbursement in a group")
    public ResponseEntity<Map<String, Object>> getAvailablePool(@PathVariable Long groupId) {
        Double pool = loanService.calculateAvailablePool(groupId);
        return ResponseEntity.ok(Map.of(
                "groupId", groupId,
                "availablePool", pool
        ));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update loan", description = "Updates details of an existing loan")
    public ResponseEntity<LoanResponse> updateLoan(@PathVariable Long id, @Valid @RequestBody LoanRequest request) {
        return ResponseEntity.ok(loanService.updateLoan(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete loan", description = "Deletes a loan by its ID")
    public ResponseEntity<Void> deleteLoan(@PathVariable Long id) {
        loanService.deleteLoan(id);
        return ResponseEntity.noContent().build();
    }
}
