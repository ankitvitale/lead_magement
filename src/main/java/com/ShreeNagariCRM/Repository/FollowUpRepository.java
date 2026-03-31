package com.ShreeNagariCRM.Repository;

import com.ShreeNagariCRM.Entity.FollowUp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FollowUpRepository extends JpaRepository<FollowUp, Long> {

    // ── By Lead ───────────────────────────────────────────────────────────────
    List<FollowUp> findByLeadId(Long leadId);
    List<FollowUp> findByLeadIdAndAssignedToId(Long leadId, Long agentId);

    // ── By Dynamic Lead ───────────────────────────────────────────────────────
    List<FollowUp> findByDynamicDataId(Long dynamicDataId);
    List<FollowUp> findByDynamicDataIdAndAssignedToId(Long dynamicDataId, Long agentId);

    // ── By Agent ──────────────────────────────────────────────────────────────
    List<FollowUp> findByAssignedTo_Id(Long agentId);
    List<FollowUp> findByAssignedToIdAndDone(Long agentId, Boolean done);

    // ── Today ─────────────────────────────────────────────────────────────────
    // Admin
    List<FollowUp> findByScheduledDateAndDoneFalse(LocalDate date);

    // Employee
    List<FollowUp> findByAssignedToIdAndScheduledDateAndDoneFalse(
            Long agentId, LocalDate date);

    // ── Overdue ───────────────────────────────────────────────────────────────
    // Admin
    @Query("SELECT f FROM FollowUp f " +
            "WHERE f.scheduledDate < :today AND f.done = false " +
            "ORDER BY f.scheduledDate ASC")
    List<FollowUp> findOverdue(@Param("today") LocalDate today);

    // Employee
    @Query("SELECT f FROM FollowUp f " +
            "WHERE f.scheduledDate < :today " +
            "AND f.done = false " +
            "AND f.assignedTo.id = :agentId " +
            "ORDER BY f.scheduledDate ASC")
    List<FollowUp> findOverdueByAssignedToId(
            @Param("today") LocalDate today,
            @Param("agentId") Long agentId);

    // ── Upcoming ──────────────────────────────────────────────────────────────
    // Admin
    @Query("SELECT f FROM FollowUp f " +
            "WHERE f.scheduledDate > :today AND f.done = false " +
            "ORDER BY f.scheduledDate ASC")
    List<FollowUp> findUpcoming(@Param("today") LocalDate today);

    // Employee
    @Query("SELECT f FROM FollowUp f " +
            "WHERE f.scheduledDate > :today " +
            "AND f.done = false " +
            "AND f.assignedTo.id = :agentId " +
            "ORDER BY f.scheduledDate ASC")
    List<FollowUp> findUpcomingByAssignedToId(
            @Param("today") LocalDate today,
            @Param("agentId") Long agentId);

    // ── Counts ────────────────────────────────────────────────────────────────
    long countByAssignedToIdAndDoneFalse(Long agentId);
}