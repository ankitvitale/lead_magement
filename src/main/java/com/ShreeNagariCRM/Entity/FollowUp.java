package com.ShreeNagariCRM.Entity;


import com.ShreeNagariCRM.Entity.enums.FollowUpType;
import com.ShreeNagariCRM.Entity.enums.Priority;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "follow_ups")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class FollowUp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long followUpId;


    // Association to standard Lead (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    private Leads lead;

    // Association to dynamic lead (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dynamic_data_id")
    private DynamicData dynamicData;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_id")
    private User assignedTo;


    @Column(nullable = false)
    private LocalDate scheduledDate;

    @Column(nullable = false)
    private LocalTime scheduledTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FollowUpType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @Column(columnDefinition = "TEXT")
    private String notes;             // agenda / purpose

    @Column(columnDefinition = "TEXT")
    private String outcome;           // filled after completion

    @Builder.Default
    private Boolean done = false;

    private LocalDateTime doneAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ── Source type helper ────────────────────────────────────────────────────
    /**
     * Returns the display name of the associated lead (standard or dynamic).
     * Useful for API responses without forcing callers to check both fields.
     */
    @Transient
    public String getLeadDisplayName() {
        if (lead != null) return lead.getName();
        if (dynamicData != null && dynamicData.getResolvedName() != null)
            return dynamicData.getResolvedName();
        if (dynamicData != null) return "Dynamic Lead #" + dynamicData.getId();
        return "Unknown";
    }

    @Transient
    public String getLeadPhone() {
        if (lead != null) return lead.getPhone();
        if (dynamicData != null) return dynamicData.getResolvedPhone();
        return null;
    }

    @Transient
    public String getLeadEmail() {
        if (lead != null) return lead.getEmail();
        if (dynamicData != null) return dynamicData.getResolvedEmail();
        return null;
    }

    @Transient
    public boolean isForDynamicLead() {
        return dynamicData != null;
    }



}
