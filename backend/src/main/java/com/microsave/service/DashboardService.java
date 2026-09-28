package com.microsave.service;

import com.microsave.dto.DashboardResponse;
import com.microsave.entity.Group;
import com.microsave.exception.ResourceNotFoundException;
import com.microsave.repository.ContributionRepository;
import com.microsave.repository.GroupRepository;
import com.microsave.repository.LoanRepository;
import com.microsave.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final ContributionRepository contributionRepository;
    private final LoanRepository loanRepository;

    public DashboardService(GroupRepository groupRepository,
                            MemberRepository memberRepository,
                            ContributionRepository contributionRepository,
                            LoanRepository loanRepository) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.contributionRepository = contributionRepository;
        this.loanRepository = loanRepository;
    }

    /**
     * Aggregates and returns summary statistics for a given SHG group.
     * RULE 1: Available Pool = Total Contributions - Total Outstanding Loans
     */
    public DashboardResponse getDashboardSummary(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group with ID " + groupId + " not found."));

        long totalMembers = memberRepository.countByGroupId(groupId);

        Double totalContributions = contributionRepository.sumAmountByGroupId(groupId);
        if (totalContributions == null) totalContributions = 0.0;

        Double totalLoans = loanRepository.sumAmountByGroupId(groupId);
        if (totalLoans == null) totalLoans = 0.0;

        Double totalOutstandingLoans = loanRepository.sumOutstandingAmountByGroupId(groupId);
        if (totalOutstandingLoans == null) totalOutstandingLoans = 0.0;

        // RULE 1: Group available pool
        double availablePool = totalContributions - totalOutstandingLoans;
        availablePool = Math.round(availablePool * 100.0) / 100.0;

        return new DashboardResponse(
                group.getId(),
                group.getName(),
                totalMembers,
                totalContributions,
                totalLoans,
                totalOutstandingLoans,
                availablePool
        );
    }
}
