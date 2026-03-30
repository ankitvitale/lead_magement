package com.ShreeNagariCRM.DTO.leadDto;

import com.ShreeNagariCRM.Entity.enums.LeadSource;
import com.ShreeNagariCRM.Entity.enums.LeadStatus;
import com.ShreeNagariCRM.Entity.enums.Priority;
import com.ShreeNagariCRM.Entity.enums.PropertyType;

public class LeadRequest {

    private String name;
    private String email;
    private String phone;
    private String alternatePhone;

    private PropertyType propertyType;
    private String preferredLocation;

    private String budgetRange;
    private Double budgetMinLakhs;
    private Double budgetMaxLakhs;

    private String notes;

    private LeadStatus status;
    private Priority priority;
    private Integer score;
    private LeadSource source;

    private Long assignedEmpId; // only ID for mapping

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAlternatePhone() {
        return alternatePhone;
    }

    public void setAlternatePhone(String alternatePhone) {
        this.alternatePhone = alternatePhone;
    }

    public PropertyType getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(PropertyType propertyType) {
        this.propertyType = propertyType;
    }

    public String getPreferredLocation() {
        return preferredLocation;
    }

    public void setPreferredLocation(String preferredLocation) {
        this.preferredLocation = preferredLocation;
    }

    public String getBudgetRange() {
        return budgetRange;
    }

    public void setBudgetRange(String budgetRange) {
        this.budgetRange = budgetRange;
    }

    public Double getBudgetMinLakhs() {
        return budgetMinLakhs;
    }

    public void setBudgetMinLakhs(Double budgetMinLakhs) {
        this.budgetMinLakhs = budgetMinLakhs;
    }

    public Double getBudgetMaxLakhs() {
        return budgetMaxLakhs;
    }

    public void setBudgetMaxLakhs(Double budgetMaxLakhs) {
        this.budgetMaxLakhs = budgetMaxLakhs;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LeadStatus getStatus() {
        return status;
    }

    public void setStatus(LeadStatus status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public LeadSource getSource() {
        return source;
    }

    public void setSource(LeadSource source) {
        this.source = source;
    }

    public Long getAssignedEmpId() {
        return assignedEmpId;
    }

    public void setAssignedEmpId(Long assignedEmpId) {
        this.assignedEmpId = assignedEmpId;
    }
}
