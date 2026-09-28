package com.microsave.controller;

import com.microsave.dto.MemberRequest;
import com.microsave.dto.MemberResponse;
import com.microsave.dto.MemberSummaryResponse;
import com.microsave.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
@Tag(name = "Members", description = "Endpoints for managing SHG members and viewing member savings summaries")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    @Operation(summary = "Register a new member", description = "Adds a new member to a specified Self-Help Group")
    public ResponseEntity<MemberResponse> createMember(@Valid @RequestBody MemberRequest request) {
        MemberResponse created = memberService.createMember(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all members", description = "Retrieves all members, optionally filtered by groupId")
    public ResponseEntity<List<MemberResponse>> getAllMembers(@RequestParam(required = false) Long groupId) {
        if (groupId != null) {
            return ResponseEntity.ok(memberService.getMembersByGroupId(groupId));
        }
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get member by ID", description = "Retrieves a member's details by their ID")
    public ResponseEntity<?> getMemberById(@PathVariable Long id) {
        if (!memberService.memberExists(id)) {
            String msg = memberService.isMemberDeleted(id)
                    ? "Member with ID " + id + " has already been deleted."
                    : "Member with ID " + id + " has already been deleted or does not exist.";
            return ResponseEntity.ok(Map.of("message", msg));
        }
        return ResponseEntity.ok(memberService.getMemberResponseById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update member", description = "Updates an existing member's information")
    public ResponseEntity<?> updateMember(@PathVariable Long id, @Valid @RequestBody MemberRequest request) {
        if (!memberService.memberExists(id)) {
            String msg = memberService.isMemberDeleted(id)
                    ? "Member with ID " + id + " has already been deleted."
                    : "Member with ID " + id + " has already been deleted or does not exist.";
            return ResponseEntity.ok(Map.of("message", msg));
        }
        return ResponseEntity.ok(memberService.updateMember(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete member", description = "Deletes a member by their ID")
    public ResponseEntity<?> deleteMember(@PathVariable Long id) {
        if (!memberService.memberExists(id)) {
            String msg = memberService.isMemberDeleted(id)
                    ? "Member with ID " + id + " has already been deleted."
                    : "Member with ID " + id + " has already been deleted or does not exist.";
            return ResponseEntity.ok(Map.of("message", msg));
        }
        memberService.deleteMember(id);
        return ResponseEntity.ok(Map.of("message", "Member with ID " + id + " deleted successfully."));
    }

    @GetMapping("/{memberId}/summary")
    @Operation(summary = "Get member savings & loan summary", description = "Returns total savings, active loan amount, and outstanding loan for a member")
    public ResponseEntity<?> getMemberSummary(@PathVariable Long memberId) {
        if (!memberService.memberExists(memberId)) {
            String msg = memberService.isMemberDeleted(memberId)
                    ? "Member with ID " + memberId + " has already been deleted."
                    : "Member with ID " + memberId + " has already been deleted or does not exist.";
            return ResponseEntity.ok(Map.of("message", msg));
        }
        return ResponseEntity.ok(memberService.getMemberSummary(memberId));
    }
}
