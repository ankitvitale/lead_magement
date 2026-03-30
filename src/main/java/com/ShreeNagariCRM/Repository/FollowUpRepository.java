package com.ShreeNagariCRM.Repository;


import com.ShreeNagariCRM.Entity.FollowUp;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FollowUpRepository extends JpaRepository<FollowUp,Long> {
    // ── By Standard Lead ─────────────────────────────────────────────────────
    List<FollowUp> findByLeadId(Long leadId);
    List<FollowUp> findByLeadIdAndDone(Long leadId, Boolean done);

    // ── By Dynamic Lead ───────────────────────────────────────────────────────
    List<FollowUp> findByDynamicDataId(Long dynamicDataId);
    List<FollowUp> findByDynamicDataIdAndDone(Long dynamicDataId, Boolean done);

    // ── By Agent ──────────────────────────────────────────────────────────────
    List<FollowUp> findByAssignedTo_Id(Long agentId);
    List<FollowUp> findByAssignedTo_IdAndDone(Long agentId, Boolean done);

    // ── By Date ───────────────────────────────────────────────────────────────
    List<FollowUp> findByScheduledDateAndDoneFalse(LocalDate date);

    @Query("SELECT f FROM FollowUp f WHERE f.scheduledDate < :today AND f.done = false")
    List<FollowUp> findOverdue(@Param("today") LocalDate today);

    @Query("SELECT f FROM FollowUp f WHERE f.scheduledDate > :today AND f.done = false ORDER BY f.scheduledDate ASC")
    List<FollowUp> findUpcoming(@Param("today") LocalDate today);

    // ── Counts ────────────────────────────────────────────────────────────────
    long countByAssignedTo_IdAndDoneFalse(Long agentId);

    @Query("SELECT COUNT(f) FROM FollowUp f WHERE f.scheduledDate <= :today AND f.done = false")
    long countDueToday(@Param("today") LocalDate today);

    // ── Bulk reassign (when agent resigns) ────────────────────────────────────
    @Modifying
    @Transactional
    @Query("UPDATE FollowUp f SET f.assignedTo.id = :toId WHERE f.assignedTo.id = :fromId AND f.done = false")
    int reassignPending(@Param("fromId") Long fromId, @Param("toId") Long toId);
}
