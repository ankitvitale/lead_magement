package com.ShreeNagariCRM.Service;



import com.ShreeNagariCRM.DTO.baseDto.ApiResponse;

import com.ShreeNagariCRM.DTO.follow_up.*;
import com.ShreeNagariCRM.Entity.*;
import com.ShreeNagariCRM.Entity.EmployeeActivityLog.ActivityAction;
import com.ShreeNagariCRM.Entity.enums.Priority;
import com.ShreeNagariCRM.Entity.enums.Role;
import com.ShreeNagariCRM.Repository.*;
import com.ShreeNagariCRM.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FollowUpService {

    private final FollowUpRepository followUpRepository;
    private final LeadRepository leadsRepository;
    private final DynamicDataRepository dynamicDataRepository;
    private final UserRepository userRepository;
    private final FollowUpTransferLogRepository transferLogRepository;
    private final ActivityLogService activityLogService;

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────────────────────
    public ApiResponse<FollowUpResponse> create(FollowUpRequest request,
                                                User currentUser) {

        // ── Validate lead source ──────────────────────────────────────────────
        if (request.getLeadId() == null && request.getDynamicDataId() == null) {
            throw new IllegalArgumentException(
                    "Either leadId or dynamicDataId must be provided.");
        }
        if (request.getLeadId() != null && request.getDynamicDataId() != null) {
            throw new IllegalArgumentException(
                    "Provide EITHER leadId OR dynamicDataId — not both.");
        }

        // ── Resolve lead ──────────────────────────────────────────────────────
        Leads lead = null;
        if (request.getLeadId() != null) {
            lead = leadsRepository.findById(request.getLeadId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Lead not found: " + request.getLeadId()));
        }

        DynamicData dynamicData = null;
        if (request.getDynamicDataId() != null) {
            dynamicData = dynamicDataRepository.findById(request.getDynamicDataId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Dynamic lead not found: " + request.getDynamicDataId()));
        }

        // ── Resolve agent ─────────────────────────────────────────────────────
        // ADMIN  → can assign to anyone via request.agentId
        //          if no agentId given → use lead's assigned agent
        // USER   → always assigned to themselves — ignore request.agentId
        User agent;
        if (isAdmin(currentUser)) {
            if (request.getAgentId() != null) {
                agent = userRepository.findById(request.getAgentId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Agent not found: " + request.getAgentId()));
            } else if (lead != null && lead.getAssignedEmp() != null) {
                agent = lead.getAssignedEmp();
            } else if (dynamicData != null
                    && dynamicData.getAssignedEmployee() != null) {
                agent = dynamicData.getAssignedEmployee();
            } else {
                agent = currentUser; // fallback
            }
        } else {
            // Employee always creates for themselves
            agent = currentUser;
        }

        // ── Build & save ──────────────────────────────────────────────────────
        FollowUp followUp = FollowUp.builder()
                .lead(lead)
                .dynamicData(dynamicData)
                .assignedTo(agent)
                .scheduledDate(request.getScheduledDate())
                .scheduledTime(request.getScheduledTime())
                .type(request.getType())
                .priority(request.getPriority() != null
                        ? request.getPriority()
                        : Priority.MEDIUM)
                .notes(request.getNotes())
                .done(false)
                .build();

        FollowUp saved = followUpRepository.save(followUp);

        // ── Log activity ──────────────────────────────────────────────────────
        activityLogService.log(
                currentUser,
                ActivityAction.FOLLOW_UP_CREATED,
                EmployeeActivityLog.ActivityModule.FOLLOW_UP,
                saved.getFollowUpId(),
                "Created follow-up for " + saved.getLeadDisplayName()
                        + " scheduled on " + saved.getScheduledDate()
                        + " | Type: " + saved.getType()
                        + " | Assigned to: " + agent.getName());

        log.info("FollowUp created — id={} by={} assignedTo={}",
                saved.getFollowUpId(),
                currentUser.getName(),
                agent.getName());

        return ApiResponse.success(
                "Follow-up created successfully.",
                convertToResponse(saved));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET ALL
    // ADMIN  → all follow-ups
    // USER   → only their own
    // ─────────────────────────────────────────────────────────────────────────
    public ApiResponse<List<FollowUpResponse>> getAll(User currentUser) {

        List<FollowUp> list = isAdmin(currentUser)
                ? followUpRepository.findAll()
                : followUpRepository.findByAssignedTo_Id(currentUser.getId());

        return ApiResponse.success(
                "Follow-ups fetched successfully.",
                list.stream().map(this::convertToResponse)
                        .collect(Collectors.toList()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET BY ID
    // ─────────────────────────────────────────────────────────────────────────
    public ApiResponse<FollowUpResponse> getById(Long id, User currentUser) {

        FollowUp followUp = followUpRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "FollowUp not found: " + id));

        // Employee can only see their own
        if (!isAdmin(currentUser)
                && !followUp.getAssignedTo().getId().equals(currentUser.getId())) {
            throw new SecurityException(
                    "You are not allowed to view this follow-up.");
        }

        return ApiResponse.success(
                "Follow-up fetched successfully.",
                convertToResponse(followUp));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET TODAY
    // ADMIN  → all today's follow-ups
    // USER   → only their today's follow-ups
    // ─────────────────────────────────────────────────────────────────────────
    public ApiResponse<List<FollowUpResponse>> getToday(User currentUser) {

        List<FollowUp> list = isAdmin(currentUser)
                ? followUpRepository.findByScheduledDateAndDoneFalse(
                LocalDate.now())
                : followUpRepository.findByAssignedToIdAndScheduledDateAndDoneFalse(
                currentUser.getId(), LocalDate.now());

        return ApiResponse.success(
                "Today's follow-ups fetched successfully.",
                list.stream().map(this::convertToResponse)
                        .collect(Collectors.toList()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET OVERDUE
    // ADMIN  → all overdue
    // USER   → only their overdue
    // ─────────────────────────────────────────────────────────────────────────
    public ApiResponse<List<FollowUpResponse>> getOverdue(User currentUser) {

        List<FollowUp> list = isAdmin(currentUser)
                ? followUpRepository.findOverdue(LocalDate.now())
                : followUpRepository.findOverdueByAssignedToId(
                LocalDate.now(), currentUser.getId());

        return ApiResponse.success(
                "Overdue follow-ups fetched successfully.",
                list.stream().map(this::convertToResponse)
                        .collect(Collectors.toList()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET UPCOMING
    // ADMIN  → all upcoming
    // USER   → only their upcoming
    // ─────────────────────────────────────────────────────────────────────────
    public ApiResponse<List<FollowUpResponse>> getUpcoming(User currentUser) {

        List<FollowUp> list = isAdmin(currentUser)
                ? followUpRepository.findUpcoming(LocalDate.now())
                : followUpRepository.findUpcomingByAssignedToId(
                LocalDate.now(), currentUser.getId());

        return ApiResponse.success(
                "Upcoming follow-ups fetched successfully.",
                list.stream().map(this::convertToResponse)
                        .collect(Collectors.toList()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET BY LEAD
    // ─────────────────────────────────────────────────────────────────────────
    public ApiResponse<List<FollowUpResponse>> getByLeadId(Long leadId,
                                                           User currentUser) {
        List<FollowUp> list = isAdmin(currentUser)
                ? followUpRepository.findByLeadId(leadId)
                : followUpRepository.findByLeadIdAndAssignedToId(
                leadId, currentUser.getId());

        return ApiResponse.success(
                "Follow-ups for lead fetched successfully.",
                list.stream().map(this::convertToResponse)
                        .collect(Collectors.toList()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET BY DYNAMIC LEAD
    // ─────────────────────────────────────────────────────────────────────────
    public ApiResponse<List<FollowUpResponse>> getByDynamicDataId(
            Long dynamicDataId, User currentUser) {

        List<FollowUp> list = isAdmin(currentUser)
                ? followUpRepository.findByDynamicDataId(dynamicDataId)
                : followUpRepository.findByDynamicDataIdAndAssignedToId(
                dynamicDataId, currentUser.getId());

        return ApiResponse.success(
                "Follow-ups for dynamic lead fetched successfully.",
                list.stream().map(this::convertToResponse)
                        .collect(Collectors.toList()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────────────────
    public ApiResponse<FollowUpResponse> update(Long id,
                                                FollowUpRequest request,
                                                User currentUser) {
        FollowUp followUp = followUpRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "FollowUp not found: " + id));

        // Employee can only update their own
        if (!isAdmin(currentUser)
                && !followUp.getAssignedTo().getId().equals(currentUser.getId())) {
            throw new SecurityException(
                    "You are not allowed to update this follow-up.");
        }

        if (request.getScheduledDate() != null)
            followUp.setScheduledDate(request.getScheduledDate());
        if (request.getScheduledTime() != null)
            followUp.setScheduledTime(request.getScheduledTime());
        if (request.getType() != null)
            followUp.setType(request.getType());
        if (request.getPriority() != null)
            followUp.setPriority(request.getPriority());
        if (request.getNotes() != null)
            followUp.setNotes(request.getNotes());

        FollowUp updated = followUpRepository.save(followUp);

        // ── Log activity ──────────────────────────────────────────────────────
        activityLogService.log(
                currentUser,
                ActivityAction.FOLLOW_UP_UPDATED,
                EmployeeActivityLog.ActivityModule.FOLLOW_UP,
                id,
                "Updated follow-up for " + updated.getLeadDisplayName()
                        + " | New date: " + updated.getScheduledDate());

        return ApiResponse.success(
                "Follow-up updated successfully.",
                convertToResponse(updated));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MARK DONE
    // ─────────────────────────────────────────────────────────────────────────
    public ApiResponse<FollowUpResponse> markDone(Long id,
                                                  String outcome,
                                                  User currentUser) {
        FollowUp followUp = followUpRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "FollowUp not found: " + id));

        // Employee can only mark their own as done
        if (!isAdmin(currentUser)
                && !followUp.getAssignedTo().getId().equals(currentUser.getId())) {
            throw new SecurityException(
                    "You are not allowed to mark this follow-up as done.");
        }

        followUp.setDone(true);
        followUp.setDoneAt(LocalDateTime.now());
        followUp.setOutcome(outcome);

        FollowUp saved = followUpRepository.save(followUp);

        // ── Log activity ──────────────────────────────────────────────────────
        activityLogService.log(
                currentUser,
                ActivityAction.FOLLOW_UP_DONE,
                EmployeeActivityLog.ActivityModule.FOLLOW_UP,
                id,
                "Marked follow-up done for " + saved.getLeadDisplayName()
                        + " | Outcome: " + (outcome != null ? outcome : "No outcome added"));

        return ApiResponse.success(
                "Follow-up marked as done.",
                convertToResponse(saved));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TRANSFER — ADMIN ONLY
    // ─────────────────────────────────────────────────────────────────────────
    public ApiResponse<FollowUpResponse> transfer(Long followUpId,
                                                  FollowUpTransferRequest request,
                                                  User adminUser) {

        // Only admin can transfer
        if (!isAdmin(adminUser)) {
            throw new SecurityException(
                    "Only ADMIN can transfer follow-ups.");
        }

        FollowUp followUp = followUpRepository.findById(followUpId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "FollowUp not found: " + followUpId));

        User toEmployee = userRepository.findById(request.getToEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found: " + request.getToEmployeeId()));

        User fromEmployee = followUp.getAssignedTo();

        // ── Save transfer log BEFORE changing agent ───────────────────────────
        FollowUpTransferLog transferLog = FollowUpTransferLog.builder()
                .followUp(followUp)
                .fromEmployee(fromEmployee)
                .fromEmployeeName(fromEmployee != null
                        ? fromEmployee.getName() : "Unassigned")
                .toEmployee(toEmployee)
                .toEmployeeName(toEmployee.getName())
                .transferredBy(adminUser)
                .transferredByName(adminUser.getName())
                .reason(request.getReason())
                .build();

        transferLogRepository.save(transferLog);

        // ── Now change the agent ──────────────────────────────────────────────
        followUp.setAssignedTo(toEmployee);
        FollowUp saved = followUpRepository.save(followUp);

        // ── Log activity for BOTH employees ───────────────────────────────────
        if (fromEmployee != null) {
            activityLogService.log(
                    fromEmployee,
                    ActivityAction.FOLLOW_UP_TRANSFERRED,
                    EmployeeActivityLog.ActivityModule.FOLLOW_UP,
                    followUpId,
                    "Follow-up transferred FROM you TO "
                            + toEmployee.getName()
                            + " | Reason: " + request.getReason()
                            + " | By Admin: " + adminUser.getName());
        }

        activityLogService.log(
                toEmployee,
                ActivityAction.FOLLOW_UP_TRANSFERRED,
                EmployeeActivityLog.ActivityModule.FOLLOW_UP,
                followUpId,
                "Follow-up transferred TO you FROM "
                        + (fromEmployee != null ? fromEmployee.getName() : "Unassigned")
                        + " | Reason: " + request.getReason()
                        + " | By Admin: " + adminUser.getName());

        log.info("FollowUp {} transferred from {} to {} by admin {} | reason={}",
                followUpId,
                fromEmployee != null ? fromEmployee.getName() : "none",
                toEmployee.getName(),
                adminUser.getName(),
                request.getReason());

        return ApiResponse.success(
                "Follow-up transferred to " + toEmployee.getName()
                        + " successfully.",
                convertToResponse(saved));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET TRANSFER HISTORY — ADMIN ONLY
    // ─────────────────────────────────────────────────────────────────────────
    public ApiResponse<List<FollowUpTransferLogResponse>> getTransferHistory(
            Long followUpId, User adminUser) {

        if (!isAdmin(adminUser)) {
            throw new SecurityException(
                    "Only ADMIN can view transfer history.");
        }

        List<FollowUpTransferLogResponse> list = transferLogRepository
                .findByFollowUpFollowUpIdOrderByTransferredAtDesc(followUpId)
                .stream()
                .map(this::convertTransferLog)
                .collect(Collectors.toList());

        return ApiResponse.success(
                "Transfer history fetched successfully.", list);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE — ADMIN ONLY
    // ─────────────────────────────────────────────────────────────────────────
    public ApiResponse<Void> delete(Long id, User currentUser) {

        if (!isAdmin(currentUser)) {
            throw new SecurityException(
                    "Only ADMIN can delete follow-ups.");
        }

        if (!followUpRepository.existsById(id)) {
            throw new ResourceNotFoundException("FollowUp not found: " + id);
        }

        followUpRepository.deleteById(id);

        activityLogService.log(
                currentUser,
                ActivityAction.DELETED,
                EmployeeActivityLog.ActivityModule.FOLLOW_UP,
                id,
                "Deleted follow-up id=" + id);

        return ApiResponse.success(
                "Follow-up deleted successfully.", null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────
    private boolean isAdmin(User user) {
        return user.getRole() == Role.ADMIN;
    }

    private FollowUpResponse convertToResponse(FollowUp f) {
        return FollowUpResponse.builder()
                .followUpId(f.getFollowUpId())
                .leadId(f.getLead() != null
                        ? f.getLead().getId() : null)
                .dynamicDataId(f.getDynamicData() != null
                        ? f.getDynamicData().getId() : null)
                .isDynamicLead(f.getDynamicData() != null)
                .leadDisplayName(f.getLeadDisplayName())
                .leadPhone(f.getLeadPhone())
                .leadEmail(f.getLeadEmail())
                .agentId(f.getAssignedTo() != null
                        ? f.getAssignedTo().getId() : null)
                .agentName(f.getAssignedTo() != null
                        ? f.getAssignedTo().getName() : null)
                .scheduledDate(f.getScheduledDate())
                .scheduledTime(f.getScheduledTime())
                .type(f.getType())
                .priority(f.getPriority())
                .notes(f.getNotes())
                .outcome(f.getOutcome())
                .done(f.getDone())
                .doneAt(f.getDoneAt())
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }

    private FollowUpTransferLogResponse convertTransferLog(
            FollowUpTransferLog log) {
        return FollowUpTransferLogResponse.builder()
                .id(log.getId())
                .followUpId(log.getFollowUp().getFollowUpId())
                .leadName(log.getFollowUp().getLeadDisplayName())
                .fromEmployeeId(log.getFromEmployee() != null
                        ? log.getFromEmployee().getId() : null)
                .fromEmployeeName(log.getFromEmployeeName())
                .toEmployeeId(log.getToEmployee() != null
                        ? log.getToEmployee().getId() : null)
                .toEmployeeName(log.getToEmployeeName())
                .transferredById(log.getTransferredBy().getId())
                .transferredByName(log.getTransferredByName())
                .reason(log.getReason())
                .transferredAt(log.getTransferredAt())
                .build();
    }
}