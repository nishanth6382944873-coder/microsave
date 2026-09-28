package com.microsave.config;

import com.microsave.entity.Contribution;
import com.microsave.entity.Group;
import com.microsave.entity.Loan;
import com.microsave.entity.Member;
import com.microsave.entity.Repayment;
import com.microsave.repository.ContributionRepository;
import com.microsave.repository.GroupRepository;
import com.microsave.repository.LoanRepository;
import com.microsave.repository.MemberRepository;
import com.microsave.repository.RepaymentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * DataInitializer seeds sample data on application startup if the database is empty.
 * Matches the problem statement specifications:
 * - Group: Women Empowerment SHG
 * - Members: Priya Sharma, Anita Verma, Sunita Rao
 * - Contributions: Priya ₹5,000, Anita ₹4,000, Sunita ₹6,000
 * - Sample Loan: Anita Verma ₹5,000
 * - Sample Repayment: Anita Verma ₹2,000 (leaves ₹3,000 outstanding)
 */
@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final ContributionRepository contributionRepository;
    private final LoanRepository loanRepository;
    private final RepaymentRepository repaymentRepository;

    public DataInitializer(GroupRepository groupRepository,
                           MemberRepository memberRepository,
                           ContributionRepository contributionRepository,
                           LoanRepository loanRepository,
                           RepaymentRepository repaymentRepository) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.contributionRepository = contributionRepository;
        this.loanRepository = loanRepository;
        this.repaymentRepository = repaymentRepository;
    }

    @Override
    public void run(String... args) {
        if (groupRepository.count() == 0) {
            System.out.println(">>> Initializing sample data for MicroSave SHG Tracker...");

            // 1. Create Group
            Group group = new Group();
            group.setName("Women Empowerment SHG");
            group.setDescription("Local self-help group dedicated to women's financial independence and micro-savings.");
            group.setCreatedDate(LocalDate.now().minusMonths(3));
            Group savedGroup = groupRepository.save(group);

            // 2. Create Members
            Member priya = new Member();
            priya.setName("Priya Sharma");
            priya.setPhone("9876543210");
            priya.setEmail("priya.sharma@example.com");
            priya.setAddress("12 Gandhi Nagar, Sector 4");
            priya.setGroup(savedGroup);
            Member savedPriya = memberRepository.save(priya);

            Member anita = new Member();
            anita.setName("Anita Verma");
            anita.setPhone("9876543211");
            anita.setEmail("anita.verma@example.com");
            anita.setAddress("45 Market Road, Near Temple");
            anita.setGroup(savedGroup);
            Member savedAnita = memberRepository.save(anita);

            Member sunita = new Member();
            sunita.setName("Sunita Rao");
            sunita.setPhone("9876543212");
            sunita.setEmail("sunita.rao@example.com");
            sunita.setAddress("78 Lake View Colony");
            sunita.setGroup(savedGroup);
            Member savedSunita = memberRepository.save(sunita);

            // 3. Create Contributions
            // Total Contributions = 5,000 + 4,000 + 6,000 = 15,000
            Contribution c1 = new Contribution();
            c1.setMember(savedPriya);
            c1.setGroup(savedGroup);
            c1.setAmount(5000.0);
            c1.setContributionDate(LocalDate.now().minusWeeks(4));
            c1.setDescription("Monthly group savings deposit");
            contributionRepository.save(c1);

            Contribution c2 = new Contribution();
            c2.setMember(savedAnita);
            c2.setGroup(savedGroup);
            c2.setAmount(4000.0);
            c2.setContributionDate(LocalDate.now().minusWeeks(4));
            c2.setDescription("Monthly group savings deposit");
            contributionRepository.save(c2);

            Contribution c3 = new Contribution();
            c3.setMember(savedSunita);
            c3.setGroup(savedGroup);
            c3.setAmount(6000.0);
            c3.setContributionDate(LocalDate.now().minusWeeks(4));
            c3.setDescription("Monthly group savings deposit");
            contributionRepository.save(c3);

            // 4. Create Sample Loan (Anita Verma borrows ₹5,000 from available pool of ₹15,000)
            Loan loan = new Loan();
            loan.setMember(savedAnita);
            loan.setGroup(savedGroup);
            loan.setAmount(5000.0);
            loan.setOutstandingAmount(3000.0); // Will be ₹3,000 after ₹2,000 repayment below
            loan.setStatus("ACTIVE");
            loan.setLoanDate(LocalDate.now().minusWeeks(2));
            loan.setDescription("Small business inventory purchase");
            Loan savedLoan = loanRepository.save(loan);

            // 5. Create Sample Repayment (Anita Verma repays ₹2,000)
            Repayment repayment = new Repayment();
            repayment.setLoan(savedLoan);
            repayment.setAmount(2000.0);
            repayment.setRepaymentDate(LocalDate.now().minusDays(3));
            repayment.setDescription("First installment repayment");
            repaymentRepository.save(repayment);

            System.out.println(">>> Sample data initialized successfully!");
            System.out.println("    Group: Women Empowerment SHG");
            System.out.println("    Total Contributions: ₹15,000");
            System.out.println("    Active Loan: ₹5,000 (Outstanding: ₹3,000)");
            System.out.println("    Available Pool: ₹12,000");
        }
    }
}
