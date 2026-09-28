package com.microsave;

import com.microsave.dto.*;
import com.microsave.entity.Group;
import com.microsave.exception.BusinessRuleException;
import com.microsave.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = MicroSaveApplication.class)
@ActiveProfiles("test")
@Transactional
public class MicroSaveBusinessRulesTest {

    @Autowired
    private GroupService groupService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ContributionService contributionService;

    @Autowired
    private LoanService loanService;

    @Autowired
    private RepaymentService repaymentService;

    @Autowired
    private DashboardService dashboardService;

    private Long testGroupId;
    private Long memberId1;
    private Long memberId2;

    @BeforeEach
    void setUp() {
        // 1. Create Test Group
        GroupRequest groupRequest = new GroupRequest("Test SHG Group", "Test Description", LocalDate.now());
        Group group = groupService.createGroup(groupRequest);
        testGroupId = group.getId();

        // 2. Create Two Test Members
        MemberRequest m1 = new MemberRequest("Kavita Devi", "9870000001", "kavita@test.com", "House 10", testGroupId);
        MemberResponse res1 = memberService.createMember(m1);
        memberId1 = res1.getId();

        MemberRequest m2 = new MemberRequest("Meena Bai", "9870000002", "meena@test.com", "House 20", testGroupId);
        MemberResponse res2 = memberService.createMember(m2);
        memberId2 = res2.getId();
    }

    @Test
    @DisplayName("Test 1 & 2: Group and Member creation works")
    void testGroupAndMemberCreation() {
        assertNotNull(testGroupId);
        assertNotNull(memberId1);
        assertNotNull(memberId2);
        assertEquals(2, memberService.getMembersByGroupId(testGroupId).size());
    }

    @Test
    @DisplayName("Test 3: Record savings contribution increases total savings")
    void testRecordContribution() {
        ContributionRequest c1 = new ContributionRequest(memberId1, testGroupId, 10000.0, LocalDate.now(), "Weekly savings");
        ContributionResponse res = contributionService.recordContribution(c1);

        assertNotNull(res.getId());
        assertEquals(10000.0, res.getAmount());

        Double availablePool = loanService.calculateAvailablePool(testGroupId);
        assertEquals(10000.0, availablePool);
    }

    @Test
    @DisplayName("Test 4: Disburse valid loan within available pool")
    void testDisburseValidLoan() {
        // Add 15,000 savings
        contributionService.recordContribution(new ContributionRequest(memberId1, testGroupId, 15000.0, LocalDate.now(), "Savings"));

        // Borrow 8,000 (<= 15,000)
        LoanRequest loanReq = new LoanRequest(memberId1, testGroupId, 8000.0, LocalDate.now(), "Emergency medical");
        LoanResponse loanRes = loanService.disburseLoan(loanReq);

        assertNotNull(loanRes.getId());
        assertEquals(8000.0, loanRes.getAmount());
        assertEquals(8000.0, loanRes.getOutstandingAmount());
        assertEquals("ACTIVE", loanRes.getStatus());

        // Pool should now be: 15,000 - 8,000 = 7,000
        Double poolAfter = loanService.calculateAvailablePool(testGroupId);
        assertEquals(7000.0, poolAfter);
    }

    @Test
    @DisplayName("Test 5: Reject loan request exceeding group available pool")
    void testRejectLoanExceedingAvailablePool() {
        // Add 5,000 savings
        contributionService.recordContribution(new ContributionRequest(memberId1, testGroupId, 5000.0, LocalDate.now(), "Savings"));

        // Request 8,000 (> 5,000 available pool)
        LoanRequest loanReq = new LoanRequest(memberId1, testGroupId, 8000.0, LocalDate.now(), "Farm supplies");

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> {
            loanService.disburseLoan(loanReq);
        });

        assertEquals("Loan amount exceeds the available group pool.", ex.getMessage());
    }

    @Test
    @DisplayName("Test 6: Reject second active loan for member who already has an active loan")
    void testRejectSecondActiveLoanForSameMember() {
        // Add 20,000 pool
        contributionService.recordContribution(new ContributionRequest(memberId1, testGroupId, 20000.0, LocalDate.now(), "Savings"));

        // First loan of 5,000
        loanService.disburseLoan(new LoanRequest(memberId1, testGroupId, 5000.0, LocalDate.now(), "First loan"));

        // Attempt second loan for memberId1 while first is ACTIVE
        LoanRequest secondLoanReq = new LoanRequest(memberId1, testGroupId, 3000.0, LocalDate.now(), "Second loan");

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> {
            loanService.disburseLoan(secondLoanReq);
        });

        assertEquals("Member already has an active unpaid loan.", ex.getMessage());
    }

    @Test
    @DisplayName("Test 7: Record valid partial loan repayment")
    void testRecordValidRepayment() {
        contributionService.recordContribution(new ContributionRequest(memberId1, testGroupId, 20000.0, LocalDate.now(), "Savings"));
        LoanResponse loan = loanService.disburseLoan(new LoanRequest(memberId1, testGroupId, 10000.0, LocalDate.now(), "Loan"));

        // Repay 4,000
        RepaymentRequest repReq = new RepaymentRequest(loan.getId(), 4000.0, LocalDate.now(), "Installment 1");
        RepaymentResponse repRes = repaymentService.recordRepayment(repReq);

        assertNotNull(repRes.getId());
        assertEquals(4000.0, repRes.getAmount());

        // Check updated loan
        LoanResponse updatedLoan = loanService.getLoanById(loan.getId());
        assertEquals(6000.0, updatedLoan.getOutstandingAmount());
        assertEquals("ACTIVE", updatedLoan.getStatus());

        // Available pool = 20,000 - 6,000 = 14,000
        Double pool = loanService.calculateAvailablePool(testGroupId);
        assertEquals(14000.0, pool);
    }

    @Test
    @DisplayName("Test 8: Reject repayment greater than outstanding loan")
    void testRejectRepaymentExceedingOutstanding() {
        contributionService.recordContribution(new ContributionRequest(memberId1, testGroupId, 20000.0, LocalDate.now(), "Savings"));
        LoanResponse loan = loanService.disburseLoan(new LoanRequest(memberId1, testGroupId, 5000.0, LocalDate.now(), "Loan"));

        // Try repaying 6,000 on a 5,000 loan
        RepaymentRequest repReq = new RepaymentRequest(loan.getId(), 6000.0, LocalDate.now(), "Overpayment");

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> {
            repaymentService.recordRepayment(repReq);
        });

        assertEquals("Repayment amount cannot exceed outstanding loan.", ex.getMessage());
    }

    @Test
    @DisplayName("Test 9: Automatically close loan when fully repaid")
    void testAutomaticallyCloseFullyRepaidLoan() {
        contributionService.recordContribution(new ContributionRequest(memberId1, testGroupId, 20000.0, LocalDate.now(), "Savings"));
        LoanResponse loan = loanService.disburseLoan(new LoanRequest(memberId1, testGroupId, 5000.0, LocalDate.now(), "Loan"));

        // Repay full 5,000
        RepaymentRequest repReq = new RepaymentRequest(loan.getId(), 5000.0, LocalDate.now(), "Full repayment");
        repaymentService.recordRepayment(repReq);

        // Loan must now be CLOSED and outstanding 0
        LoanResponse closedLoan = loanService.getLoanById(loan.getId());
        assertEquals(0.0, closedLoan.getOutstandingAmount());
        assertEquals("CLOSED", closedLoan.getStatus());

        // Further repayment on CLOSED loan should be rejected
        RepaymentRequest extraRep = new RepaymentRequest(loan.getId(), 100.0, LocalDate.now(), "Extra");
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> {
            repaymentService.recordRepayment(extraRep);
        });
        assertEquals("Loan is already fully repaid.", ex.getMessage());

        // Since loan is CLOSED, member can now take a new loan!
        LoanResponse secondLoan = loanService.disburseLoan(new LoanRequest(memberId1, testGroupId, 2000.0, LocalDate.now(), "New Loan"));
        assertNotNull(secondLoan.getId());
        assertEquals("ACTIVE", secondLoan.getStatus());
    }

    @Test
    @DisplayName("Test 10: View member savings and loan summary")
    void testMemberSummary() {
        contributionService.recordContribution(new ContributionRequest(memberId1, testGroupId, 7000.0, LocalDate.now(), "Savings 1"));
        contributionService.recordContribution(new ContributionRequest(memberId1, testGroupId, 3000.0, LocalDate.now(), "Savings 2"));

        LoanResponse loan = loanService.disburseLoan(new LoanRequest(memberId1, testGroupId, 4000.0, LocalDate.now(), "Loan"));
        repaymentService.recordRepayment(new RepaymentRequest(loan.getId(), 1500.0, LocalDate.now(), "Repayment"));

        MemberSummaryResponse summary = memberService.getMemberSummary(memberId1);
        assertEquals("Kavita Devi", summary.getMemberName());
        assertEquals(10000.0, summary.getTotalSavings()); // 7000 + 3000
        assertEquals(4000.0, summary.getActiveLoanAmount());
        assertEquals(2500.0, summary.getOutstandingLoan()); // 4000 - 1500
    }

    @Test
    @DisplayName("Test 11: View group dashboard metrics")
    void testGroupDashboardSummary() {
        contributionService.recordContribution(new ContributionRequest(memberId1, testGroupId, 12000.0, LocalDate.now(), "Savings"));
        contributionService.recordContribution(new ContributionRequest(memberId2, testGroupId, 8000.0, LocalDate.now(), "Savings"));

        LoanResponse loan = loanService.disburseLoan(new LoanRequest(memberId1, testGroupId, 6000.0, LocalDate.now(), "Loan"));
        repaymentService.recordRepayment(new RepaymentRequest(loan.getId(), 2000.0, LocalDate.now(), "Repay"));

        DashboardResponse dashboard = dashboardService.getDashboardSummary(testGroupId);

        assertEquals("Test SHG Group", dashboard.getGroupName());
        assertEquals(2, dashboard.getTotalMembers());
        assertEquals(20000.0, dashboard.getTotalContributions()); // 12000 + 8000
        assertEquals(6000.0, dashboard.getTotalLoans());
        assertEquals(4000.0, dashboard.getTotalOutstandingLoans()); // 6000 - 2000
        assertEquals(16000.0, dashboard.getAvailablePool()); // 20000 - 4000
    }

    @Test
    @DisplayName("Test 12: Delete member cascades and removes contributions, loans, and repayments cleanly")
    void testDeleteMemberWithCascade() {
        contributionService.recordContribution(new ContributionRequest(memberId1, testGroupId, 5000.0, LocalDate.now(), "Deposit"));
        LoanResponse loan = loanService.disburseLoan(new LoanRequest(memberId1, testGroupId, 3000.0, LocalDate.now(), "Loan"));
        repaymentService.recordRepayment(new RepaymentRequest(loan.getId(), 1000.0, LocalDate.now(), "Part Pay"));

        // Delete member1
        assertDoesNotThrow(() -> memberService.deleteMember(memberId1));

        // Member should no longer exist
        assertThrows(com.microsave.exception.ResourceNotFoundException.class, () -> memberService.getMemberResponseById(memberId1));

        // Contributions and loans for member1 should be gone
        assertEquals(0, contributionService.getContributionsByMemberId(memberId1).size());
        assertEquals(0, loanService.getLoansByMemberId(memberId1).size());
    }
}
