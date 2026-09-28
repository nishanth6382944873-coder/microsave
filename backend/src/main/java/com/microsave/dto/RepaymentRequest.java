package com.microsave.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class RepaymentRequest {

    @NotNull(message = "Loan ID is required")
    private Long loanId;

    @NotNull(message = "Repayment amount is required")
    @Positive(message = "Repayment amount must be greater than zero")
    private Double amount;

    private LocalDate repaymentDate;

    private String description;

    public RepaymentRequest() {
    }

    public RepaymentRequest(Long loanId, Double amount, LocalDate repaymentDate, String description) {
        this.loanId = loanId;
        this.amount = amount;
        this.repaymentDate = repaymentDate;
        this.description = description;
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public LocalDate getRepaymentDate() {
        return repaymentDate;
    }

    public void setRepaymentDate(LocalDate repaymentDate) {
        this.repaymentDate = repaymentDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
