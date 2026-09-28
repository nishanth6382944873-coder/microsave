package com.microsave.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Contribution Entity represents a weekly or periodic savings contribution
 * deposited by a member into the group pool.
 */
@Entity
@Table(name = "contributions")
public class Contribution {

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

    @Column(name = "contribution_date", nullable = false)
    private LocalDate contributionDate;

    private String description;

    public Contribution() {
    }

    public Contribution(Long id, Member member, Group group, Double amount, LocalDate contributionDate, String description) {
        this.id = id;
        this.member = member;
        this.group = group;
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

    @Override
    public String toString() {
        return "Contribution{" +
                "id=" + id +
                ", member=" + (member != null ? member.getName() : null) +
                ", group=" + (group != null ? group.getName() : null) +
                ", amount=" + amount +
                ", contributionDate=" + contributionDate +
                ", description='" + description + '\'' +
                '}';
    }
}
