package com.microsave.repository;

import com.microsave.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByGroupId(Long groupId);

    List<Loan> findByMemberId(Long memberId);

    List<Loan> findByStatus(String status);

    List<Loan> findByGroupIdAndStatus(Long groupId, String status);

    /**
     * Checks if a member has any loan with given status (e.g. "ACTIVE")
     */
    boolean existsByMemberIdAndStatus(Long memberId, String status);

    /**
     * Sum of all loan amounts disbursed for a group
     */
    @Query("SELECT COALESCE(SUM(l.amount), 0.0) FROM Loan l WHERE l.group.id = :groupId")
    Double sumAmountByGroupId(@Param("groupId") Long groupId);

    /**
     * Sum of outstanding loan amounts for a group (all active loans)
     */
    @Query("SELECT COALESCE(SUM(l.outstandingAmount), 0.0) FROM Loan l WHERE l.group.id = :groupId AND l.status = 'ACTIVE'")
    Double sumOutstandingAmountByGroupId(@Param("groupId") Long groupId);

    /**
     * Sum of active loan principal for a member
     */
    @Query("SELECT COALESCE(SUM(l.amount), 0.0) FROM Loan l WHERE l.member.id = :memberId AND l.status = 'ACTIVE'")
    Double sumActiveLoanAmountByMemberId(@Param("memberId") Long memberId);

    /**
     * Sum of current outstanding loan balance for a member
     */
    @Query("SELECT COALESCE(SUM(l.outstandingAmount), 0.0) FROM Loan l WHERE l.member.id = :memberId AND l.status = 'ACTIVE'")
    Double sumOutstandingAmountByMemberId(@Param("memberId") Long memberId);
}
