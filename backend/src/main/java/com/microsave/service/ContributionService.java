package com.microsave.service;

import com.microsave.dto.ContributionRequest;
import com.microsave.dto.ContributionResponse;
import com.microsave.entity.Contribution;
import com.microsave.entity.Group;
import com.microsave.entity.Member;
import com.microsave.exception.BusinessRuleException;
import com.microsave.exception.ResourceNotFoundException;
import com.microsave.repository.ContributionRepository;
import com.microsave.repository.GroupRepository;
import com.microsave.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ContributionService {

    private final ContributionRepository contributionRepository;
    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;

    public ContributionService(ContributionRepository contributionRepository,
                               MemberRepository memberRepository,
                               GroupRepository groupRepository) {
        this.contributionRepository = contributionRepository;
        this.memberRepository = memberRepository;
        this.groupRepository = groupRepository;
    }

    public ContributionResponse recordContribution(ContributionRequest request) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new BusinessRuleException("Contribution amount must be greater than zero.");
        }

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member with ID " + request.getMemberId() + " not found."));

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group with ID " + request.getGroupId() + " not found."));

        Contribution contribution = new Contribution();
        contribution.setMember(member);
        contribution.setGroup(group);
        contribution.setAmount(request.getAmount());
        contribution.setContributionDate(request.getContributionDate() != null ? request.getContributionDate() : LocalDate.now());
        contribution.setDescription(request.getDescription());

        Contribution saved = contributionRepository.save(contribution);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ContributionResponse> getAllContributions() {
        return contributionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ContributionResponse> getContributionsByGroupId(Long groupId) {
        return contributionRepository.findByGroupId(groupId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ContributionResponse> getContributionsByMemberId(Long memberId) {
        return contributionRepository.findByMemberId(memberId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContributionResponse getContributionById(Long id) {
        Contribution contribution = contributionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution with ID " + id + " not found."));
        return mapToResponse(contribution);
    }

    public ContributionResponse updateContribution(Long id, ContributionRequest request) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new BusinessRuleException("Contribution amount must be greater than zero.");
        }

        Contribution contribution = contributionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution with ID " + id + " not found."));

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member with ID " + request.getMemberId() + " not found."));

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group with ID " + request.getGroupId() + " not found."));

        contribution.setMember(member);
        contribution.setGroup(group);
        contribution.setAmount(request.getAmount());
        if (request.getContributionDate() != null) {
            contribution.setContributionDate(request.getContributionDate());
        }
        contribution.setDescription(request.getDescription());

        Contribution updated = contributionRepository.save(contribution);
        return mapToResponse(updated);
    }

    public void deleteContribution(Long id) {
        Contribution contribution = contributionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution with ID " + id + " not found."));
        contributionRepository.delete(contribution);
    }

    public ContributionResponse mapToResponse(Contribution contribution) {
        return new ContributionResponse(
                contribution.getId(),
                contribution.getMember() != null ? contribution.getMember().getId() : null,
                contribution.getMember() != null ? contribution.getMember().getName() : null,
                contribution.getGroup() != null ? contribution.getGroup().getId() : null,
                contribution.getGroup() != null ? contribution.getGroup().getName() : null,
                contribution.getAmount(),
                contribution.getContributionDate(),
                contribution.getDescription()
        );
    }
}
