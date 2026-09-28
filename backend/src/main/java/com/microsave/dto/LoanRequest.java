package com.microsave.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class LoanRequest {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull(message = "Group ID is required")
    private Long groupId;

    @NotNull(message = "Loan amount is required")
    @Positive(message = "Loan amount must be greater than zero")
    private Double amount;

    private LocalDate loanDate;

    private String description;

    public LoanRequest() {
    }

    public LoanRequest(Long memberId, Long groupId, Double amount, LocalDate loanDate, String description) {
        this.memberId = memberId;
        this.groupId = groupId;
        this.amount = amount;
        this.loanDate = loanDate;
        this.description = description;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
