package com.microsave.service;

import com.microsave.dto.LoanRequest;
import com.microsave.dto.LoanResponse;
import com.microsave.entity.Group;
import com.microsave.entity.Loan;
import com.microsave.entity.Member;
import com.microsave.exception.BusinessRuleException;
import com.microsave.exception.ResourceNotFoundException;
import com.microsave.repository.ContributionRepository;
import com.microsave.repository.GroupRepository;
import com.microsave.repository.LoanRepository;
import com.microsave.repository.MemberRepository;
import com.microsave.repository.RepaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LoanService {

    private final LoanRepository loanRepository;
    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;
    private final ContributionRepository contributionRepository;
    private final RepaymentRepository repaymentRepository;

    public LoanService(LoanRepository loanRepository,
                       MemberRepository memberRepository,
                       GroupRepository groupRepository,
                       ContributionRepository contributionRepository,
                       RepaymentRepository repaymentRepository) {
        this.loanRepository = loanRepository;
        this.memberRepository = memberRepository;
        this.groupRepository = groupRepository;
        this.contributionRepository = contributionRepository;
        this.repaymentRepository = repaymentRepository;
    }

    /**
     * Disburses a new loan strictly following the SHG business rules:
     * 1. Check member exists.
     * 2. Check group exists.
     * 3. Check requested amount > 0.
     * 4. Check whether member already has an ACTIVE unpaid loan (RULE 2).
     * 5. Calculate group available pool: Pool = Total Contributions - Total Outstanding Loans (RULE 1).
     * 6. Check loan amount <= available pool (RULE 3).
     * 7. Disburse loan with status = ACTIVE and outstandingAmount = amount.
     */
    public LoanResponse disburseLoan(LoanRequest request) {
        // Validation: RULE 4 - Loan amount must be positive
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new BusinessRuleException("Loan amount must be greater than zero.");
        }

        // 1. Verify Member exists
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member with ID " + request.getMemberId() + " not found."));

        // 2. Verify Group exists
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group with ID " + request.getGroupId() + " not found."));

        // RULE 2: A member with an active unpaid loan cannot take another loan.
        boolean hasActiveLoan = loanRepository.existsByMemberIdAndStatus(member.getId(), "ACTIVE");
        if (hasActiveLoan) {
            throw new BusinessRuleException("Member already has an active unpaid loan.");
        }

        // RULE 1: Calculate Available Pool = Total Contributions - Total Outstanding Loans
        Double availablePool = calculateAvailablePool(group.getId());

        // RULE 3: A new loan cannot exceed the group's available pool.
        if (request.getAmount() > availablePool) {
            throw new BusinessRuleException("Loan amount exceeds the available group pool.");
        }

        // Create new Loan
        Loan loan = new Loan();
        loan.setMember(member);
        loan.setGroup(group);
        loan.setAmount(request.getAmount());
        loan.setOutstandingAmount(request.getAmount()); // Initial outstanding equals requested amount
        loan.setStatus("ACTIVE"); // Initial status is ACTIVE
        loan.setLoanDate(request.getLoanDate() != null ? request.getLoanDate() : LocalDate.now());
        loan.setDescription(request.getDescription());

        Loan saved = loanRepository.save(loan);
        return mapToResponse(saved);
    }

    /**
     * Calculates available pool for a group.
     * Available Pool = Total Contributions - Total Outstanding Loans
     */
    @Transactional(readOnly = true)
    public Double calculateAvailablePool(Long groupId) {
        Double totalContributions = contributionRepository.sumAmountByGroupId(groupId);
        Double totalOutstandingLoans = loanRepository.sumOutstandingAmountByGroupId(groupId);

        double contributions = totalContributions != null ? totalContributions : 0.0;
        double outstanding = totalOutstandingLoans != null ? totalOutstandingLoans : 0.0;

        return contributions - outstanding;
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> getAllLoans() {
        return loanRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> getLoansByGroupId(Long groupId) {
        return loanRepository.findByGroupId(groupId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> getLoansByMemberId(Long memberId) {
        return loanRepository.findByMemberId(memberId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LoanResponse getLoanById(Long id) {
        Loan loan = getLoanEntityById(id);
        return mapToResponse(loan);
    }

    @Transactional(readOnly = true)
    public Loan getLoanEntityById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan with ID " + id + " not found."));
    }

    public LoanResponse updateLoan(Long id, LoanRequest request) {
        Loan loan = getLoanEntityById(id);

        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new BusinessRuleException("Loan amount must be greater than zero.");
        }

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member with ID " + request.getMemberId() + " not found."));

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group with ID " + request.getGroupId() + " not found."));

        loan.setMember(member);
        loan.setGroup(group);
        loan.setAmount(request.getAmount());
        if (request.getLoanDate() != null) {
            loan.setLoanDate(request.getLoanDate());
        }
        loan.setDescription(request.getDescription());

        Loan updated = loanRepository.save(loan);
        return mapToResponse(updated);
    }

    public void deleteLoan(Long id) {
        Loan loan = getLoanEntityById(id);
        List<com.microsave.entity.Repayment> repayments = repaymentRepository.findByLoanId(id);
        if (!repayments.isEmpty()) {
            repaymentRepository.deleteAll(repayments);
        }
        loanRepository.delete(loan);
    }

    public LoanResponse mapToResponse(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getMember() != null ? loan.getMember().getId() : null,
                loan.getMember() != null ? loan.getMember().getName() : null,
                loan.getGroup() != null ? loan.getGroup().getId() : null,
                loan.getGroup() != null ? loan.getGroup().getName() : null,
                loan.getAmount(),
                loan.getOutstandingAmount(),
                loan.getLoanDate(),
                loan.getStatus(),
                loan.getDescription()
        );
    }
}
