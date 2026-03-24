package com.ShreeNagariCRM.Controller;

import com.ShreeNagariCRM.DTO.leadDto.FileDataResponseDTO;
import com.ShreeNagariCRM.Entity.DynamicData;
import com.ShreeNagariCRM.Entity.enums.LeadStatus;
import com.ShreeNagariCRM.Service.ExcelService;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
public class LeadController {
    @Autowired
    private ExcelService leadsExcelService;

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
}
