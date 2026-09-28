package com.microsave.controller;

import com.microsave.dto.RepaymentRequest;
import com.microsave.dto.RepaymentResponse;
import com.microsave.service.RepaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repayments")
@Tag(name = "Repayments", description = "Endpoints for recording and tracking loan repayments")
public class RepaymentController {

    private final RepaymentService repaymentService;

    public RepaymentController(RepaymentService repaymentService) {
        this.repaymentService = repaymentService;
    }

    @PostMapping
    @Operation(summary = "Record a loan repayment", description = "Records a repayment towards an active loan, reducing the outstanding balance")
    public ResponseEntity<RepaymentResponse> recordRepayment(@Valid @RequestBody RepaymentRequest request) {
        RepaymentResponse recorded = repaymentService.recordRepayment(request);
        return new ResponseEntity<>(recorded, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all repayments", description = "Retrieves all repayment records, optionally filtered by loanId")
    public ResponseEntity<List<RepaymentResponse>> getAllRepayments(@RequestParam(required = false) Long loanId) {
        if (loanId != null) {
            return ResponseEntity.ok(repaymentService.getRepaymentsByLoanId(loanId));
        }
        return ResponseEntity.ok(repaymentService.getAllRepayments());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get repayment by ID", description = "Retrieves details of a specific repayment by its ID")
    public ResponseEntity<RepaymentResponse> getRepaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(repaymentService.getRepaymentById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update repayment", description = "Updates details of an existing repayment and adjusts loan balance accordingly")
    public ResponseEntity<RepaymentResponse> updateRepayment(@PathVariable Long id, @Valid @RequestBody RepaymentRequest request) {
        return ResponseEntity.ok(repaymentService.updateRepayment(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete repayment", description = "Deletes a repayment and restores the corresponding loan balance")
    public ResponseEntity<Void> deleteRepayment(@PathVariable Long id) {
        repaymentService.deleteRepayment(id);
        return ResponseEntity.noContent().build();
    }
}
