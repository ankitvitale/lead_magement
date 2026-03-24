package com.ShreeNagariCRM.Entity;

import com.ShreeNagariCRM.Entity.enums.LeadStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dynamic_data")
public class DynamicData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "json")
    private String data;

    private String fileName;   // NEW FIELD

    private LocalDateTime uploadedAt;
    private LeadStatus leadStatus;


    // getters and setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public LeadStatus getLeadStatus() {
        return leadStatus;
    }

    public void setLeadStatus(LeadStatus leadStatus) {
        this.leadStatus = leadStatus;
    }
}
