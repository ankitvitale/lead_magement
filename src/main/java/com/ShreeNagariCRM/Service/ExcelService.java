package com.ShreeNagariCRM.Service;

import com.ShreeNagariCRM.DTO.leadDto.FileDataResponseDTO;
import com.ShreeNagariCRM.Entity.DynamicData;
import com.ShreeNagariCRM.Entity.Leads;
import com.ShreeNagariCRM.Entity.User;
import com.ShreeNagariCRM.Entity.enums.LeadStatus;
import com.ShreeNagariCRM.Repository.DynamicDataRepostory;
import com.ShreeNagariCRM.Repository.LeadRepository;
import com.ShreeNagariCRM.Repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExcelService {

    @Autowired
    private LeadRepository leadsRepository;


    @Autowired
    private DynamicDataRepostory dynamicDataRepostory;

    public void saveExcelData(MultipartFile file) throws Exception {

        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {

            Row row = sheet.getRow(i);
            if (row == null) continue;

            Leads lead = new Leads();

            lead.setName(getCellValue(row.getCell(0)));
            lead.setEmail(getCellValue(row.getCell(1)));
            lead.setPhone(getCellValue(row.getCell(2)));

            leadsRepository.save(lead);
        }

        workbook.close();
    }

    private String getCellValue(Cell cell) {

        if (cell == null) return "";

        switch (cell.getCellType()) {

            case STRING:
                return cell.getStringCellValue();

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case FORMULA:
                return cell.getCellFormula();

            case BLANK:
                return "";

            default:
                return "";
        }
    }



//    public void saveExcelDatas(MultipartFile file) throws Exception {
//
//        Workbook workbook = new XSSFWorkbook(file.getInputStream());
//        Sheet sheet = workbook.getSheetAt(0);
//
//        Row headerRow = sheet.getRow(0);
//
//        List<DynamicData> list = new ArrayList<>();
//
//        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
//
//            Row row = sheet.getRow(i);
//            if (row == null) continue;
//
//            Map<String, String> rowData = new HashMap<>();
//
//            for (int j = 0; j < headerRow.getLastCellNum(); j++) {
//
//                String columnName = headerRow.getCell(j).getStringCellValue();
//                String value = getCellValue(row.getCell(j));
//
//                rowData.put(columnName, value);
//            }
//
//            ObjectMapper mapper = new ObjectMapper();
//            String jsonData = mapper.writeValueAsString(rowData);
//
//            DynamicData data = new DynamicData();
//            data.setData(jsonData);
//
//            list.add(data);
//        }
//
//        dynamicDataRepostory.saveAll(list);
//    }




    public void saveExcelDatas(MultipartFile file) throws Exception {

        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        Row headerRow = sheet.getRow(0);

        List<DynamicData> list = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {

            Row row = sheet.getRow(i);
            if (row == null) continue;

            Map<String, String> rowData = new HashMap<>();

            for (int j = 0; j < headerRow.getLastCellNum(); j++) {

                String columnName = headerRow.getCell(j).getStringCellValue();
                String value = getCellValue(row.getCell(j));

                rowData.put(columnName, value);
            }

            ObjectMapper mapper = new ObjectMapper();
            String jsonData = mapper.writeValueAsString(rowData);

            DynamicData data = new DynamicData();
            data.setData(jsonData);
            data.setFileName(file.getOriginalFilename()); // IMPORTANT
            data.setUploadedAt(LocalDateTime.now());

            list.add(data);
        }

        dynamicDataRepostory.saveAll(list);
    }



    public List<DynamicData> getDataByFileName(String fileName) {
        return dynamicDataRepostory.findByFileName(fileName);
    }

    public List<FileDataResponseDTO> getAllFilesWithData() throws Exception {

        List<DynamicData> allData = dynamicDataRepostory.findAll();

        Map<String, List<Map<String, Object>>> groupedData = new HashMap<>();

        ObjectMapper mapper = new ObjectMapper();

        for (DynamicData d : allData) {

            String fileName = d.getFileName();

            Map<String, Object> jsonMap = mapper.readValue(d.getData(), Map.class);

            // ✅ ADD ID INTO JSON
            jsonMap.put("id", d.getId());

            groupedData
                    .computeIfAbsent(fileName, k -> new ArrayList<>())
                    .add(jsonMap);
        }

        List<FileDataResponseDTO> response = new ArrayList<>();

        for (Map.Entry<String, List<Map<String, Object>>> entry : groupedData.entrySet()) {
            response.add(new FileDataResponseDTO(entry.getKey(), entry.getValue()));
        }

        return response;
    }


//    public List<FileDataResponseDTO> getGroupedData() throws Exception {
//
//        List<DynamicData> list = dynamicDataRepostory.findAll();
//
//        ObjectMapper mapper = new ObjectMapper();
//
//        Map<String, List<Map<String, Object>>> grouped = new HashMap<>();
//
//        for (DynamicData d : list) {
//
//            Map<String, Object> map = mapper.readValue(d.getData(), Map.class);
//
//            map.put("id", d.getId());
//            map.put("uploadedAt", d.getUploadedAt());
//
//            grouped
//                    .computeIfAbsent(d.getFileName(), k -> new ArrayList<>())
//                    .add(map);
//        }
//
//        List<FileDataResponseDTO> response = new ArrayList<>();
//
//        for (Map.Entry<String, List<Map<String, Object>>> entry : grouped.entrySet()) {
//            response.add(new FileDataResponseDTO(entry.getKey(), entry.getValue()));
//        }
//
//        return response;
//    }




    public FileDataResponseDTO getDataByFile(String fileName) throws Exception {

        List<DynamicData> list = dynamicDataRepostory.findByFileName(fileName);

        ObjectMapper mapper = new ObjectMapper();

        List<Map<String, Object>> dataList = new ArrayList<>();

        for (DynamicData d : list) {

            Map<String, Object> map = mapper.readValue(d.getData(), Map.class);

            map.put("id", d.getId());
            map.put("uploadedAt", d.getUploadedAt());
            map.put("leadStatus", d.getLeadStatus());

            dataList.add(map);
        }

        return new FileDataResponseDTO(fileName, dataList);
    }

    public String updateLeadStatus(Long id, String fileName, LeadStatus status) {

        DynamicData data = dynamicDataRepostory
                .findByIdAndFileName(id, fileName)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        data.setLeadStatus(status);

        dynamicDataRepostory.save(data);

        return "Lead status updated successfully";
    }

    public List<FileDataResponseDTO> getAllFilesData() throws Exception {

        List<DynamicData> list = dynamicDataRepostory.findAll();

        ObjectMapper mapper = new ObjectMapper();

        // Group by fileName
        Map<String, List<Map<String, Object>>> groupedData = new HashMap<>();

        for (DynamicData d : list) {

            Map<String, Object> map = mapper.readValue(d.getData(), Map.class);

            map.put("id", d.getId());
            map.put("uploadedAt", d.getUploadedAt());
            map.put("leadStatus", d.getLeadStatus());

            groupedData
                    .computeIfAbsent(d.getFileName(), k -> new ArrayList<>())
                    .add(map);
        }

        // Convert Map → DTO List
        List<FileDataResponseDTO> response = new ArrayList<>();

        for (Map.Entry<String, List<Map<String, Object>>> entry : groupedData.entrySet()) {
            response.add(new FileDataResponseDTO(entry.getKey(), entry.getValue()));
        }

        return response;
    }


//    public List<FileDataResponseDTO> getGroupedData() throws Exception {
//
//        List<DynamicData> list = dynamicDataRepostory.findAll();
//
//        ObjectMapper mapper = new ObjectMapper();
//
//        Map<String, List<Map<String, Object>>> grouped = new HashMap<>();
//
//        for (DynamicData d : list) {
//
//            Map<String, Object> map = mapper.readValue(d.getData(), Map.class);
//
//            map.put("id", d.getId()); // DB ID
//            map.put("uploadedAt", d.getUploadedAt());
//
//            grouped
//                    .computeIfAbsent(d.getFileName(), k -> new ArrayList<>())
//                    .add(map);
//        }
//
//        List<FileDataResponseDTO> response = new ArrayList<>();
//
//        for (Map.Entry<String, List<Map<String, Object>>> entry : grouped.entrySet()) {
//
//            List<Map<String, Object>> dataList = entry.getValue();
//
//            // 🔥 ADD rowId (per file)
//            for (int i = 0; i < dataList.size(); i++) {
//                dataList.get(i).put("rowId", i + 1);
//            }
//
//            response.add(new FileDataResponseDTO(entry.getKey(), dataList));
//        }
//
//        return response;
//    }
}
