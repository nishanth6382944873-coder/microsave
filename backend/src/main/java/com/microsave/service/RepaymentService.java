package com.microsave.service;

import com.microsave.dto.RepaymentRequest;
import com.microsave.dto.RepaymentResponse;
import com.microsave.entity.Loan;
import com.microsave.entity.Repayment;
import com.microsave.exception.BusinessRuleException;
import com.microsave.exception.ResourceNotFoundException;
import com.microsave.repository.LoanRepository;
import com.microsave.repository.RepaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RepaymentService {

    private final RepaymentRepository repaymentRepository;
    private final LoanRepository loanRepository;

    public RepaymentService(RepaymentRepository repaymentRepository,
                            LoanRepository loanRepository) {
        this.repaymentRepository = repaymentRepository;
        this.loanRepository = loanRepository;
    }

    /**
     * Records a repayment strictly adhering to SHG rules:
     * 1. Find loan.
     * 2. Check loan status: only ACTIVE loans accept repayments (RULE 8).
     * 3. Check repayment amount > 0 (RULE 5).
     * 4. Check repayment <= outstanding loan (RULE 6).
     * 5. Subtract repayment from outstandingAmount.
     * 6. If outstandingAmount becomes 0: status automatically becomes CLOSED (RULE 7).
     * 7. Save loan.
     * 8. Save repayment.
     */
    public RepaymentResponse recordRepayment(RepaymentRequest request) {
        // 1. Find the loan
        Loan loan = loanRepository.findById(request.getLoanId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan with ID " + request.getLoanId() + " not found."));

        // RULE 8: Only ACTIVE loans can receive repayments
        if ("CLOSED".equalsIgnoreCase(loan.getStatus()) || loan.getOutstandingAmount() <= 0) {
            throw new BusinessRuleException("Loan is already fully repaid.");
        }

        // RULE 5: Repayment amount must be positive
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new BusinessRuleException("Repayment amount must be greater than zero.");
        }

        // RULE 6: Repayment cannot exceed outstanding loan
        if (request.getAmount() > loan.getOutstandingAmount()) {
            throw new BusinessRuleException("Repayment amount cannot exceed outstanding loan.");
        }

        // 5. Subtract repayment from outstandingAmount
        double remainingOutstanding = loan.getOutstandingAmount() - request.getAmount();

        // Round to 2 decimal places to avoid floating point imprecision
        remainingOutstanding = Math.round(remainingOutstanding * 100.0) / 100.0;
        loan.setOutstandingAmount(remainingOutstanding);

        // RULE 7: When outstandingAmount becomes 0, status must automatically become CLOSED
        if (remainingOutstanding <= 0.0) {
            loan.setOutstandingAmount(0.0);
            loan.setStatus("CLOSED");
        }

        // 7. Save updated loan
        loanRepository.save(loan);

        // 8. Save repayment record
        Repayment repayment = new Repayment();
        repayment.setLoan(loan);
        repayment.setAmount(request.getAmount());
        repayment.setRepaymentDate(request.getRepaymentDate() != null ? request.getRepaymentDate() : LocalDate.now());
        repayment.setDescription(request.getDescription());

        Repayment saved = repaymentRepository.save(repayment);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<RepaymentResponse> getAllRepayments() {
        return repaymentRepository.findAllOrderByDateDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RepaymentResponse> getRepaymentsByLoanId(Long loanId) {
        return repaymentRepository.findByLoanId(loanId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RepaymentResponse getRepaymentById(Long id) {
        Repayment repayment = repaymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Repayment with ID " + id + " not found."));
        return mapToResponse(repayment);
    }

    public RepaymentResponse updateRepayment(Long id, RepaymentRequest request) {
        Repayment repayment = repaymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Repayment with ID " + id + " not found."));

        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new BusinessRuleException("Repayment amount must be greater than zero.");
        }

        // Adjust loan outstanding: revert old repayment, apply new repayment
        Loan loan = repayment.getLoan();
        double restoredOutstanding = loan.getOutstandingAmount() + repayment.getAmount();

        if (request.getAmount() > restoredOutstanding) {
            throw new BusinessRuleException("Repayment amount cannot exceed outstanding loan.");
        }

        double newOutstanding = Math.round((restoredOutstanding - request.getAmount()) * 100.0) / 100.0;
        loan.setOutstandingAmount(newOutstanding);
        if (newOutstanding <= 0.0) {
            loan.setOutstandingAmount(0.0);
            loan.setStatus("CLOSED");
        } else {
            loan.setStatus("ACTIVE");
        }
        loanRepository.save(loan);

        repayment.setAmount(request.getAmount());
        if (request.getRepaymentDate() != null) {
            repayment.setRepaymentDate(request.getRepaymentDate());
        }
        repayment.setDescription(request.getDescription());

        Repayment updated = repaymentRepository.save(repayment);
        return mapToResponse(updated);
    }

    public void deleteRepayment(Long id) {
        Repayment repayment = repaymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Repayment with ID " + id + " not found."));

        // Revert loan balance
        Loan loan = repayment.getLoan();
        loan.setOutstandingAmount(loan.getOutstandingAmount() + repayment.getAmount());
        if (loan.getOutstandingAmount() > 0) {
            loan.setStatus("ACTIVE");
        }
        loanRepository.save(loan);

        repaymentRepository.delete(repayment);
    }

    public RepaymentResponse mapToResponse(Repayment repayment) {
        Loan loan = repayment.getLoan();
        return new RepaymentResponse(
                repayment.getId(),
                loan != null ? loan.getId() : null,
                (loan != null && loan.getMember() != null) ? loan.getMember().getId() : null,
                (loan != null && loan.getMember() != null) ? loan.getMember().getName() : null,
                loan != null ? loan.getAmount() : null,
                loan != null ? loan.getOutstandingAmount() : null,
                repayment.getAmount(),
                repayment.getRepaymentDate(),
                repayment.getDescription(),
                loan != null ? loan.getStatus() : null
        );
    }
}
