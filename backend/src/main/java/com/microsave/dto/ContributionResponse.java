package com.microsave.dto;

import java.time.LocalDate;

public class ContributionResponse {

    private Long id;
    private Long memberId;
    private String memberName;
    private Long groupId;
    private String groupName;
    private Double amount;
    private LocalDate contributionDate;
    private String description;

    public ContributionResponse() {
    }

    public ContributionResponse(Long id, Long memberId, String memberName, Long groupId, String groupName, Double amount, LocalDate contributionDate, String description) {
        this.id = id;
        this.memberId = memberId;
        this.memberName = memberName;
        this.groupId = groupId;
        this.groupName = groupName;
        this.amount = amount;
        this.contributionDate = contributionDate;
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
