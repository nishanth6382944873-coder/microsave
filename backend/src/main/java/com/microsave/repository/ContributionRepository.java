package com.microsave.repository;

import com.microsave.entity.Contribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContributionRepository extends JpaRepository<Contribution, Long> {

    List<Contribution> findByGroupId(Long groupId);

    List<Contribution> findByMemberId(Long memberId);

    @Query("SELECT COALESCE(SUM(c.amount), 0.0) FROM Contribution c WHERE c.group.id = :groupId")
    Double sumAmountByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT COALESCE(SUM(c.amount), 0.0) FROM Contribution c WHERE c.member.id = :memberId")
    Double sumAmountByMemberId(@Param("memberId") Long memberId);
}
