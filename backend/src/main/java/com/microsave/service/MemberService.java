package com.microsave.service;

import com.microsave.dto.MemberRequest;
import com.microsave.dto.MemberResponse;
import com.microsave.dto.MemberSummaryResponse;
import com.microsave.entity.Contribution;
import com.microsave.entity.Group;
import com.microsave.entity.Loan;
import com.microsave.entity.Member;
import com.microsave.entity.Repayment;
import com.microsave.exception.ResourceNotFoundException;
import com.microsave.repository.ContributionRepository;
import com.microsave.repository.GroupRepository;
import com.microsave.repository.LoanRepository;
import com.microsave.repository.MemberRepository;
import com.microsave.repository.RepaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;
    private final ContributionRepository contributionRepository;
    private final LoanRepository loanRepository;
    private final RepaymentRepository repaymentRepository;

    // Track recently deleted member IDs so queries for them report they are already deleted
    private final Set<Long> deletedMemberIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public MemberService(MemberRepository memberRepository,
                         GroupRepository groupRepository,
                         ContributionRepository contributionRepository,
                         LoanRepository loanRepository,
                         RepaymentRepository repaymentRepository) {
        this.memberRepository = memberRepository;
        this.groupRepository = groupRepository;
        this.contributionRepository = contributionRepository;
        this.loanRepository = loanRepository;
        this.repaymentRepository = repaymentRepository;
    }

    public MemberResponse createMember(MemberRequest request) {
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group with ID " + request.getGroupId() + " not found."));

        Member member = new Member();
        member.setName(request.getName());
        member.setPhone(request.getPhone());
        member.setEmail(request.getEmail());
        member.setAddress(request.getAddress());
        member.setGroup(group);

        Member savedMember = memberRepository.save(member);
        deletedMemberIds.remove(savedMember.getId());
        return mapToResponse(savedMember);
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> getMembersByGroupId(Long groupId) {
        return memberRepository.findByGroupId(groupId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean memberExists(Long id) {
        return memberRepository.existsById(id);
    }

    public boolean isMemberDeleted(Long id) {
        return deletedMemberIds.contains(id);
    }

    @Transactional(readOnly = true)
    public MemberResponse getMemberResponseById(Long id) {
        Member member = getMemberEntityById(id);
        return mapToResponse(member);
    }

    @Transactional(readOnly = true)
    public Member getMemberEntityById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> {
                    if (deletedMemberIds.contains(id)) {
                        return new ResourceNotFoundException("Member with ID " + id + " has already been deleted.");
                    }
                    return new ResourceNotFoundException("Member with ID " + id + " has already been deleted or does not exist.");
                });
    }

    public MemberResponse updateMember(Long id, MemberRequest request) {
        Member member = getMemberEntityById(id);

        if (!member.getGroup().getId().equals(request.getGroupId())) {
            Group newGroup = groupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("Group with ID " + request.getGroupId() + " not found."));
            member.setGroup(newGroup);
        }

        member.setName(request.getName());
        member.setPhone(request.getPhone());
        member.setEmail(request.getEmail());
        member.setAddress(request.getAddress());

        Member updatedMember = memberRepository.save(member);
        return mapToResponse(updatedMember);
    }

    public void deleteMember(Long id) {
        Member member = getMemberEntityById(id);

        // 1. Delete all repayments linked to this member's loans
        List<Loan> memberLoans = loanRepository.findByMemberId(id);
        for (Loan loan : memberLoans) {
            List<Repayment> repayments = repaymentRepository.findByLoanId(loan.getId());
            if (!repayments.isEmpty()) {
                repaymentRepository.deleteAll(repayments);
            }
        }

        // 2. Delete all loans taken by this member
        if (!memberLoans.isEmpty()) {
            loanRepository.deleteAll(memberLoans);
        }

        // 3. Delete all contributions made by this member
        List<Contribution> memberContributions = contributionRepository.findByMemberId(id);
        if (!memberContributions.isEmpty()) {
            contributionRepository.deleteAll(memberContributions);
        }

        // 4. Delete the member
        memberRepository.delete(member);
        deletedMemberIds.add(id);
    }

    /**
     * Returns personal savings and loan summary for a given member.
     */
    @Transactional(readOnly = true)
    public MemberSummaryResponse getMemberSummary(Long memberId) {
        Member member = getMemberEntityById(memberId);
        Double totalSavings = contributionRepository.sumAmountByMemberId(memberId);
        Double activeLoanAmount = loanRepository.sumActiveLoanAmountByMemberId(memberId);
        Double outstandingLoan = loanRepository.sumOutstandingAmountByMemberId(memberId);

        return new MemberSummaryResponse(
                member.getId(),
                member.getName(),
                totalSavings != null ? totalSavings : 0.0,
                activeLoanAmount != null ? activeLoanAmount : 0.0,
                outstandingLoan != null ? outstandingLoan : 0.0
        );
    }

    public MemberResponse mapToResponse(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getPhone(),
                member.getEmail(),
                member.getAddress(),
                member.getGroup() != null ? member.getGroup().getId() : null,
                member.getGroup() != null ? member.getGroup().getName() : null
        );
    }
}
