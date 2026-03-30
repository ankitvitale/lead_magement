package com.ShreeNagariCRM.Service;


import com.ShreeNagariCRM.DTO.leadDto.LeadRequest;
import com.ShreeNagariCRM.DTO.leadDto.LeadRespond;
import com.ShreeNagariCRM.Entity.Leads;
import com.ShreeNagariCRM.Entity.User;
import com.ShreeNagariCRM.Entity.enums.LeadStatus;
import com.ShreeNagariCRM.Entity.enums.Priority;
import com.ShreeNagariCRM.Repository.LeadRepository;
import com.ShreeNagariCRM.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeadService {
    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private UserRepository userRepository;


    public LeadRespond createLead(LeadRequest request) {

        User user = null;

        if (request.getAssignedEmpId() != null) {
            user = userRepository.findById(request.getAssignedEmpId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        Leads lead = new Leads();

        lead.setName(request.getName());
        lead.setEmail(request.getEmail());
        lead.setPhone(request.getPhone());
        lead.setAlternatePhone(request.getAlternatePhone());

        lead.setPropertyType(request.getPropertyType());
        lead.setPreferredLocation(request.getPreferredLocation());

        lead.setBudgetRange(request.getBudgetRange());
        lead.setBudgetMinLakhs(request.getBudgetMinLakhs());
        lead.setBudgetMaxLakhs(request.getBudgetMaxLakhs());

        lead.setNotes(request.getNotes());

        lead.setStatus(request.getStatus() != null ? request.getStatus() : LeadStatus.NEW);
        lead.setPriority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM);
        lead.setScore(request.getScore() != null ? request.getScore() : 50);

        lead.setSource(request.getSource());
        lead.setAssignedEmp(user);

        Leads savedLead = leadRepository.save(lead);

        return mapToResponse(savedLead);
    }

    private LeadRespond mapToResponse(Leads lead) {

        LeadRespond response = new LeadRespond();

        response.setId(lead.getId());
        response.setName(lead.getName());
        response.setEmail(lead.getEmail());
        response.setPhone(lead.getPhone());
        response.setAlternatePhone(lead.getAlternatePhone());

        response.setPropertyType(lead.getPropertyType());
        response.setPreferredLocation(lead.getPreferredLocation());

        response.setBudgetRange(lead.getBudgetRange());
        response.setBudgetMinLakhs(lead.getBudgetMinLakhs());
        response.setBudgetMaxLakhs(lead.getBudgetMaxLakhs());

        response.setNotes(lead.getNotes());

        response.setStatus(lead.getStatus());
        response.setPriority(lead.getPriority());
        response.setScore(lead.getScore());
        response.setSource(lead.getSource());

        if (lead.getAssignedEmp() != null) {
            response.setAssignedEmpId(lead.getAssignedEmp().getId());
            response.setAssignedEmpName(lead.getAssignedEmp().getName());
        }

        response.setCreatedAt(lead.getCreatedAt());
        response.setUpdatedAt(lead.getUpdatedAt());

        return response;
    }

    public LeadRespond getLeadById(Long id) {
        Leads lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        return mapToResponse(lead);
    }

    public List<LeadRespond> getAllLeads() {
        return leadRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public LeadRespond updateLead(Long id, LeadRequest request) {

        Leads lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        User user = null;
        if (request.getAssignedEmpId() != null) {
            user = userRepository.findById(request.getAssignedEmpId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        lead.setName(request.getName());
        lead.setEmail(request.getEmail());
        lead.setPhone(request.getPhone());
        lead.setAlternatePhone(request.getAlternatePhone());

        lead.setPropertyType(request.getPropertyType());
        lead.setPreferredLocation(request.getPreferredLocation());

        lead.setBudgetRange(request.getBudgetRange());
        lead.setBudgetMinLakhs(request.getBudgetMinLakhs());
        lead.setBudgetMaxLakhs(request.getBudgetMaxLakhs());

        lead.setNotes(request.getNotes());

        lead.setStatus(request.getStatus());
        lead.setPriority(request.getPriority());
        lead.setScore(request.getScore());
        lead.setSource(request.getSource());

        lead.setAssignedEmp(user);

        Leads updated = leadRepository.save(lead);

        return mapToResponse(updated);
    }

    public void deleteLead(Long id) {

        Leads lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        leadRepository.delete(lead);
    }
}
