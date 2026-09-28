package com.microsave.repository;

import com.microsave.entity.Repayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepaymentRepository extends JpaRepository<Repayment, Long> {

    List<Repayment> findByLoanId(Long loanId);

    @Query("SELECT r FROM Repayment r WHERE r.loan.group.id = :groupId")
    List<Repayment> findByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT r FROM Repayment r ORDER BY r.repaymentDate DESC, r.id DESC")
    List<Repayment> findAllOrderByDateDesc();
}
