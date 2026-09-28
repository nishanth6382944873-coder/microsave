package com.microsave.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Repayment Entity represents a partial or full loan repayment by a member.
 */
@Entity
@Table(name = "repayments")
public class Repayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(nullable = false)
    private Double amount;

    @Column(name = "repayment_date", nullable = false)
    private LocalDate repaymentDate;

    private String description;

    public Repayment() {
    }

    public Repayment(Long id, Loan loan, Double amount, LocalDate repaymentDate, String description) {
        this.id = id;
        this.loan = loan;
        this.amount = amount;
        this.repaymentDate = repaymentDate;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Loan getLoan() {
        return loan;
    }

    public void setLoan(Loan loan) {
        this.loan = loan;
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

    @Override
    public String toString() {
        return "Repayment{" +
                "id=" + id +
                ", loanId=" + (loan != null ? loan.getId() : null) +
                ", amount=" + amount +
                ", repaymentDate=" + repaymentDate +
                ", description='" + description + '\'' +
                '}';
    }
}
