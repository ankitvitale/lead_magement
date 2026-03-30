package com.ShreeNagariCRM.Entity;


import com.ShreeNagariCRM.Entity.enums.LeadSource;
import com.ShreeNagariCRM.Entity.enums.LeadStatus;
import com.ShreeNagariCRM.Entity.enums.Priority;
import com.ShreeNagariCRM.Entity.enums.PropertyType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"followUps"})
public class Leads {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Contact ───────────────────────────────────────────────────────────────
    @Column(nullable = false)
    private String name;

    @Column
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column
    private String alternatePhone;

    // ── Property Interest ─────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column
    private PropertyType propertyType;

    @Column
    private String preferredLocation;

    @Column
    private String budgetRange;       // display label  e.g. "₹1Cr – ₹2Cr"

    @Column
    private Double budgetMinLakhs;    // numeric lower bound for property matching

    @Column
    private Double budgetMaxLakhs;    // numeric upper bound

    @Column(columnDefinition = "TEXT")
    private String notes;

    // ── CRM Meta ──────────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LeadStatus status = LeadStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @Column(nullable = false)
    @Builder.Default
    private Integer score = 50;

    @Enumerated(EnumType.STRING)
    @Column
    private LeadSource source;

    // ── Assignment ────────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_emp_id")
    private User assignedEmp;

    // ── Timestamps ────────────────────────────────────────────────────────────
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
