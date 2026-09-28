package com.microsave.dto;

import java.time.LocalDate;

public class RepaymentResponse {

    private Long id;
    private Long loanId;
    private Long memberId;
    private String memberName;
    private Double originalLoanAmount;
    private Double outstandingAmount;
    private Double amount;
    private LocalDate repaymentDate;
    private String description;
    private String loanStatus;

    public RepaymentResponse() {
    }

    public RepaymentResponse(Long id, Long loanId, Long memberId, String memberName, Double originalLoanAmount, Double outstandingAmount, Double amount, LocalDate repaymentDate, String description, String loanStatus) {
        this.id = id;
        this.loanId = loanId;
        this.memberId = memberId;
        this.memberName = memberName;
        this.originalLoanAmount = originalLoanAmount;
        this.outstandingAmount = outstandingAmount;
        this.amount = amount;
        this.repaymentDate = repaymentDate;
        this.description = description;
        this.loanStatus = loanStatus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public Double getOriginalLoanAmount() {
        return originalLoanAmount;
    }

    public void setOriginalLoanAmount(Double originalLoanAmount) {
        this.originalLoanAmount = originalLoanAmount;
    }

    public Double getOutstandingAmount() {
        return outstandingAmount;
    }

    public void setOutstandingAmount(Double outstandingAmount) {
        this.outstandingAmount = outstandingAmount;
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

    public String getLoanStatus() {
        return loanStatus;
    }

    public void setLoanStatus(String loanStatus) {
        this.loanStatus = loanStatus;
    }
}
