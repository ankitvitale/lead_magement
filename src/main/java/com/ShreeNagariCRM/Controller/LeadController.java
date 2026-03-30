package com.ShreeNagariCRM.Controller;

import com.ShreeNagariCRM.DTO.leadDto.FileDataResponseDTO;
import com.ShreeNagariCRM.DTO.leadDto.LeadRequest;
import com.ShreeNagariCRM.DTO.leadDto.LeadRespond;
import com.ShreeNagariCRM.Entity.DynamicData;
import com.ShreeNagariCRM.Entity.enums.LeadStatus;
import com.ShreeNagariCRM.Service.ExcelService;

import com.ShreeNagariCRM.Service.LeadService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LeadController {
    @Autowired
    private ExcelService leadsExcelService;


    @Autowired
    private LeadService leadService;

//    @PostMapping("/upload-file")
//    public String uploadExcel(@RequestParam("file") MultipartFile file) {
//        try {
//            leadsExcelService.  saveExcelData(file);
//            return "Leads imported successfully!";
//        } catch (Exception e) {
//            return "Error: " + e.getMessage();
//        }
//    }


    //working api

    @PostMapping("/upload")
    public String uploadDynamicData(@RequestParam("file") MultipartFile file) {
        try {
            leadsExcelService.saveExcelDatas(file);
            return "Leads imported successfully!";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }


    @PutMapping("/update-status")
    public String updateStatus(
            @RequestParam Long id,
            @RequestParam String fileName,
            @RequestParam LeadStatus status) {

        return leadsExcelService.updateLeadStatus(id, fileName, status);
    }

    // Get data by file name
    @GetMapping("/all/files")
    public FileDataResponseDTO getDataByFile(@RequestParam String fileName) throws Exception {
        return leadsExcelService.getDataByFile(fileName);
    }

    @GetMapping("/all-files-data")
    public List<FileDataResponseDTO> getAllFilesData() throws Exception {
        return leadsExcelService.getAllFilesData();
    }


    //---------------------lead api


    @PostMapping("/leads/create")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<LeadRespond> createLead(@RequestBody LeadRequest request) {
        LeadRespond response = leadService.createLead(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/leads/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<LeadRespond> getLeadById(@PathVariable Long id) {
        return ResponseEntity.ok(leadService.getLeadById(id));
    }

    @GetMapping("/leads")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<LeadRespond>> getAllLeads() {
        return ResponseEntity.ok(leadService.getAllLeads());
    }


    @PutMapping("/leads/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<LeadRespond> updateLead(@PathVariable Long id,
                                                  @RequestBody LeadRequest request) {
        return ResponseEntity.ok(leadService.updateLead(id, request));
    }

    @DeleteMapping("/leads/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<String> deleteLead(@PathVariable Long id) {
        leadService.deleteLead(id);
        return ResponseEntity.ok("Lead deleted successfully");
    }
}
