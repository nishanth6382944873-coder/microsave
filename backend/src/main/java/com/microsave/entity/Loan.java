package com.microsave.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Loan Entity represents an internal loan disbursed from the group pool to a member.
 */
@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(nullable = false)
    private Double amount;

    @Column(name = "outstanding_amount", nullable = false)
    private Double outstandingAmount;

    @Column(name = "loan_date", nullable = false)
    private LocalDate loanDate;

    /**
     * Loan Status: "ACTIVE" or "CLOSED"
     */
    @Column(nullable = false)
    private String status;

    private String description;

    public Loan() {
    }

    public Loan(Long id, Member member, Group group, Double amount, Double outstandingAmount, LocalDate loanDate, String status, String description) {
        this.id = id;
        this.member = member;
        this.group = group;
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

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
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

    @Override
    public String toString() {
        return "Loan{" +
                "id=" + id +
                ", member=" + (member != null ? member.getName() : null) +
                ", group=" + (group != null ? group.getName() : null) +
                ", amount=" + amount +
                ", outstandingAmount=" + outstandingAmount +
                ", loanDate=" + loanDate +
                ", status='" + status + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
