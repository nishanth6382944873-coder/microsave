package com.microsave.dto;

import java.time.LocalDate;

public class LoanResponse {

    private Long id;
    private Long memberId;
    private String memberName;
    private Long groupId;
    private String groupName;
    private Double amount;
    private Double outstandingAmount;
    private LocalDate loanDate;
    private String status;
    private String description;

    public LoanResponse() {
    }

    public LoanResponse(Long id, Long memberId, String memberName, Long groupId, String groupName, Double amount, Double outstandingAmount, LocalDate loanDate, String status, String description) {
        this.id = id;
        this.memberId = memberId;
        this.memberName = memberName;
        this.groupId = groupId;
        this.groupName = groupName;
        this.amount = amount;
        this.outstandingAmount = outstandingAmount;
        this.loanDate = loanDate;
        this.status = status;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getOutstandingAmount() {
        return outstandingAmount;
    }

    public void setOutstandingAmount(Double outstandingAmount) {
        this.outstandingAmount = outstandingAmount;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
