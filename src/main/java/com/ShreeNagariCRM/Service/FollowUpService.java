package com.ShreeNagariCRM.Service;


import com.ShreeNagariCRM.Entity.DynamicData;
import com.ShreeNagariCRM.Entity.FollowUp;
import com.ShreeNagariCRM.Entity.Leads;
import com.ShreeNagariCRM.Entity.User;
import com.ShreeNagariCRM.Entity.enums.FollowUpType;
import com.ShreeNagariCRM.Entity.enums.Priority;
import com.ShreeNagariCRM.Repository.DynamicDataRepository;
import com.ShreeNagariCRM.Repository.FollowUpRepository;
import com.ShreeNagariCRM.Repository.LeadRepository;
import com.ShreeNagariCRM.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FollowUpService {

    private final FollowUpRepository followUpRepository;
    private final LeadRepository leadsRepository;
    private final DynamicDataRepository dynamicDataRepository;
    private final UserRepository employeeRepository;


    // ─────────────────────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a follow-up for EITHER a standard lead OR a dynamic data row.
     *
     * Rules:
     *  - Exactly one of leadId / dynamicDataId must be non-null.
     *  -employeeId is optional; if null and the lead has an assignedemployee, thatemployee is used.
     */
    public FollowUp create(
            Long leadId,
            Long dynamicDataId,
            Long employeeId,
            LocalDate scheduledDate,
            java.time.LocalTime scheduledTime,
            FollowUpType type,
            Priority priority,
            String notes) {

        // ── Validate: exactly one lead source ─────────────────────────────────
        if (leadId == null && dynamicDataId == null) {
            throw new IllegalArgumentException("Either leadId or dynamicDataId must be provided.");
        }
        if (leadId != null && dynamicDataId != null) {
            throw new IllegalArgumentException("Provide EITHER leadId OR dynamicDataId, not both.");
        }

        // ── Resolve lead / dynamic record ─────────────────────────────────────
        Leads lead  = null;
        DynamicData dynamicData = null;
        User employee = null;

        if (leadId != null) {
            lead = leadsRepository.findById(leadId)
                    .orElseThrow(() -> new RuntimeException("Lead not found: " + leadId));
            // Default to lead's assignedemployee if none specified
            if (employeeId == null && lead.getAssignedEmp() != null) {
                employee = lead.getAssignedEmp();
            }
        }

        if (dynamicDataId != null) {
            dynamicData = dynamicDataRepository.findById(dynamicDataId)
                    .orElseThrow(() -> new RuntimeException("DynamicData not found: " + dynamicDataId));
            if (employeeId == null && dynamicData.getAssignedEmployee() != null) {
               employee = dynamicData.getAssignedEmployee();
            }
        }

        // Explicitemployee override
        if (employeeId != null) {
           employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Agent not found: " +employeeId));
        }

        // ── Build & persist ───────────────────────────────────────────────────
        FollowUp followUp = FollowUp.builder()
                .lead(lead)
                .dynamicData(dynamicData)
                .assignedTo(employee)
                .scheduledDate(scheduledDate)
                .scheduledTime(scheduledTime)
                .type(type)
                .priority(priority)
                .notes(notes)
                .done(false)
                .build();

        FollowUp saved = followUpRepository.save(followUp);
        log.info("FollowUp created id={} for {} on {}",
                saved.getFollowUpId(), saved.getLeadDisplayName(), scheduledDate);
        return saved;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────────────────────

    public FollowUp getById(Long id) {
        return followUpRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FollowUp not found: " + id));
    }

    public List<FollowUp> getAll() {
        return followUpRepository.findAll();
    }


    public List<FollowUp> getByStandardLead(Long leadId) {
        return followUpRepository.findByLeadId(leadId);
    }

    public List<FollowUp> getByDynamicData(Long dynamicDataId) {
        return followUpRepository.findByDynamicDataId(dynamicDataId);
    }

    public List<FollowUp> getByAgent(Long agentId) {
        return followUpRepository.findByAssignedTo_Id(agentId);
    }

    public List<FollowUp> getOverdue() {
        return followUpRepository.findOverdue(LocalDate.now());
    }

    public List<FollowUp> getToday() {
        return followUpRepository.findByScheduledDateAndDoneFalse(LocalDate.now());
    }

    public List<FollowUp> getUpcoming() {
        return followUpRepository.findUpcoming(LocalDate.now());
    }

    public List<FollowUp> getPending() {
        // overdue + today
        List<FollowUp> result = followUpRepository.findOverdue(LocalDate.now());
        result.addAll(followUpRepository.findByScheduledDateAndDoneFalse(LocalDate.now()));
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────────────────

    public FollowUp update(Long id,
                           LocalDate scheduledDate,
                           java.time.LocalTime scheduledTime,
                           FollowUpType type,
                           Priority priority,
                           String notes) {
        FollowUp fu = getById(id);
        if (scheduledDate != null) fu.setScheduledDate(scheduledDate);
        if (scheduledTime != null) fu.setScheduledTime(scheduledTime);
        if (type          != null) fu.setType(type);
        if (priority      != null) fu.setPriority(priority);
        if (notes         != null) fu.setNotes(notes);
        return followUpRepository.save(fu);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MARK DONE
    // ─────────────────────────────────────────────────────────────────────────

    public FollowUp markDone(Long id, String outcome) {
        FollowUp fu = getById(id);
        fu.setDone(true);
        fu.setDoneAt(LocalDateTime.now());
        fu.setOutcome(outcome);
        FollowUp saved = followUpRepository.save(fu);
        log.info("FollowUp {} marked done for {}", id, saved.getLeadDisplayName());
        return saved;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────────────────────

    public void delete(Long id) {
        followUpRepository.deleteById(id);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BULK REASSIGN (used during agent transfer)
    // ─────────────────────────────────────────────────────────────────────────

    public int reassignPendingFollowUps(Long fromAgentId, Long toAgentId) {
        int count = followUpRepository.reassignPending(fromAgentId, toAgentId);
        log.info("Reassigned {} pending follow-ups from agent {} to {}", count, fromAgentId, toAgentId);
        return count;
    }


    // ─────────────────────────────────────────────────────────────────────────
    // DASHBOARD STATS
    // ─────────────────────────────────────────────────────────────────────────

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFollowUps",   followUpRepository.count());
        stats.put("pendingToday",     followUpRepository.countDueToday(LocalDate.now()));
        stats.put("overdue",          followUpRepository.findOverdue(LocalDate.now()).size());
        stats.put("completedToday",   followUpRepository
                .findByScheduledDateAndDoneFalse(LocalDate.now()).stream()
                .filter(FollowUp::getDone).count());
        return stats;
    }

}
