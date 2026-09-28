package com.microsave.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class ContributionRequest {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull(message = "Group ID is required")
    private Long groupId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Contribution amount must be greater than zero")
    private Double amount;

    private LocalDate contributionDate;

    private String description;

    public ContributionRequest() {
    }

    public ContributionRequest(Long memberId, Long groupId, Double amount, LocalDate contributionDate, String description) {
        this.memberId = memberId;
        this.groupId = groupId;
        this.amount = amount;
        this.contributionDate = contributionDate;
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

    public LocalDate getContributionDate() {
        return contributionDate;
    }

    public void setContributionDate(LocalDate contributionDate) {
        this.contributionDate = contributionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
