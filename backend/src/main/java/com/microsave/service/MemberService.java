package com.microsave.service;

import com.microsave.dto.MemberRequest;
import com.microsave.dto.MemberResponse;
import com.microsave.dto.MemberSummaryResponse;
import com.microsave.entity.Group;
import com.microsave.entity.Member;
import com.microsave.exception.ResourceNotFoundException;
import com.microsave.repository.ContributionRepository;
import com.microsave.repository.GroupRepository;
import com.microsave.repository.LoanRepository;
import com.microsave.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;
    private final ContributionRepository contributionRepository;
    private final LoanRepository loanRepository;

    public MemberService(MemberRepository memberRepository,
                         GroupRepository groupRepository,
                         ContributionRepository contributionRepository,
                         LoanRepository loanRepository) {
        this.memberRepository = memberRepository;
        this.groupRepository = groupRepository;
        this.contributionRepository = contributionRepository;
        this.loanRepository = loanRepository;
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
    public MemberResponse getMemberResponseById(Long id) {
        Member member = getMemberEntityById(id);
        return mapToResponse(member);
    }

    @Transactional(readOnly = true)
    public Member getMemberEntityById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member with ID " + id + " not found."));
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
        memberRepository.delete(member);
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
