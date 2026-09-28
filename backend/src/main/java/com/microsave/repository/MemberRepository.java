package com.microsave.repository;

import com.microsave.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByGroupId(Long groupId);

    long countByGroupId(Long groupId);
}
