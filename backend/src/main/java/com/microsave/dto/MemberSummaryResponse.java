package com.microsave.dto;

public class MemberSummaryResponse {

    private Long memberId;
    private String memberName;
    private Double totalSavings;
    private Double activeLoanAmount;
    private Double outstandingLoan;

    public MemberSummaryResponse() {
    }

    public MemberSummaryResponse(Long memberId, String memberName, Double totalSavings, Double activeLoanAmount, Double outstandingLoan) {
        this.memberId = memberId;
        this.memberName = memberName;
        this.totalSavings = totalSavings;
        this.activeLoanAmount = activeLoanAmount;
        this.outstandingLoan = outstandingLoan;
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

    public Double getTotalSavings() {
        return totalSavings;
    }

    public void setTotalSavings(Double totalSavings) {
        this.totalSavings = totalSavings;
    }

    public Double getActiveLoanAmount() {
        return activeLoanAmount;
    }

    public void setActiveLoanAmount(Double activeLoanAmount) {
        this.activeLoanAmount = activeLoanAmount;
    }

    public Double getOutstandingLoan() {
        return outstandingLoan;
    }

    public void setOutstandingLoan(Double outstandingLoan) {
        this.outstandingLoan = outstandingLoan;
    }
}
