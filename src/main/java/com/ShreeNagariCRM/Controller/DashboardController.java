package com.ShreeNagariCRM.Controller;

import com.ShreeNagariCRM.Entity.Leads;
import com.ShreeNagariCRM.Entity.enums.LeadStatus;
import com.ShreeNagariCRM.Repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final LeadRepository leadsRepository;
    private final FollowUpController followUpController; // reuse stats method indirectly

    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("totalLeads",      leadsRepository.count());
        s.put("newLeads",        leadsRepository.countByStatus(LeadStatus.NEW));
        s.put("qualifiedLeads",  leadsRepository.countByStatus(LeadStatus.QUALIFIED));
        s.put("closedLeads",     leadsRepository.countByStatus(LeadStatus.CLOSED));

        // Pipeline breakdown
        Map<String, Long> pipeline = new LinkedHashMap<>();
        for (LeadStatus st : LeadStatus.values()) {
            pipeline.put(st.name(), leadsRepository.countByStatus(st));
        }
        s.put("pipeline", pipeline);
        return ResponseEntity.ok(s);
    }
}