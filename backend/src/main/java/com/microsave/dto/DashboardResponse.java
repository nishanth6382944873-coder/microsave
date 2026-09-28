package com.microsave.dto;

public class DashboardResponse {

    private Long groupId;
    private String groupName;
    private long totalMembers;
    private Double totalContributions;
    private Double totalLoans;
    private Double totalOutstandingLoans;
    private Double availablePool;

    public DashboardResponse() {
    }

    public DashboardResponse(Long groupId, String groupName, long totalMembers, Double totalContributions, Double totalLoans, Double totalOutstandingLoans, Double availablePool) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.totalMembers = totalMembers;
        this.totalContributions = totalContributions;
        this.totalLoans = totalLoans;
        this.totalOutstandingLoans = totalOutstandingLoans;
        this.availablePool = availablePool;
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

    public long getTotalMembers() {
        return totalMembers;
    }

    public void setTotalMembers(long totalMembers) {
        this.totalMembers = totalMembers;
    }

    public Double getTotalContributions() {
        return totalContributions;
    }

    public void setTotalContributions(Double totalContributions) {
        this.totalContributions = totalContributions;
    }

    public Double getTotalLoans() {
        return totalLoans;
    }

    public void setTotalLoans(Double totalLoans) {
        this.totalLoans = totalLoans;
    }

    public Double getTotalOutstandingLoans() {
        return totalOutstandingLoans;
    }

    public void setTotalOutstandingLoans(Double totalOutstandingLoans) {
        this.totalOutstandingLoans = totalOutstandingLoans;
    }

    public Double getAvailablePool() {
        return availablePool;
    }

    public void setAvailablePool(Double availablePool) {
        this.availablePool = availablePool;
    }
}
