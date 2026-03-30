package com.ShreeNagariCRM.Service;

import com.ShreeNagariCRM.DTO.baseDto.ApiResponse;
import com.ShreeNagariCRM.DTO.excelDto.*;
import com.ShreeNagariCRM.Entity.DynamicData;
import com.ShreeNagariCRM.Entity.ExcelImportSession;
import com.ShreeNagariCRM.Entity.User;
import com.ShreeNagariCRM.Entity.enums.LeadStatus;
import com.ShreeNagariCRM.Entity.enums.Role;
import com.ShreeNagariCRM.Repository.DynamicDataRepository;
import com.ShreeNagariCRM.Repository.ExcelImportSessionRepository;
import com.ShreeNagariCRM.Repository.UserRepository;
import com.ShreeNagariCRM.exception.ResourceNotFoundException;
import com.ShreeNagariCRM.helper.ExcelHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportService {

    private final DynamicDataRepository dynamicDataRepository;
    private final ExcelImportSessionRepository sessionRepository;
    private final UserRepository employeeRepository;
    private final ObjectMapper objectMapper;
    private final ExcelHelper excelHelper;


    // ── Keywords used for auto-detecting column purposes ─────────────────────
    private static final List<String> NAME_HINTS =
            List.of("name", "customer name", "client name", "full name",
                    "client", "customer", "buyer", "person", "contact name");

    private static final List<String> PHONE_HINTS =
            List.of("phone", "mobile", "contact", "cell", "number",
                    "phone number", "mobile number", "contact number", "mob", "ph");

    private static final List<String> EMAIL_HINTS =
            List.of("email", "mail", "email id", "email address", "e-mail");


    // ─────────────────────────────────────────────────────────────────────────
    // STEP 1 — PREVIEW
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Parses the uploaded Excel file and returns:
     *   - All column headers
     *   - First 5 preview rows as List<Map<header, value>>
     *   - Auto-detected name/phone/email column suggestions
     *   - A sessionId (UUID) for the confirm step
     *
     * Does NOT persist any DynamicData rows yet.
     */
    @Transactional
    public ApiResponse<ExcelPreviewDto> previewImport(MultipartFile file,User user) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        Workbook workbook = ExcelHelper.getWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);

        Row headerRow = ExcelHelper.findHeaderRow(sheet);
        List<String> headers = ExcelHelper.extractHeaders(headerRow);

        String sessionId = UUID.randomUUID().toString();
        String fileName  = file.getOriginalFilename();

        if (headers.isEmpty()) {
            throw new IllegalArgumentException("No headers found in Excel");
        }


        List<Map<String, String>> previewRows =
                ExcelHelper.extractPreviewRows(sheet, headerRow, headers);

        int totalRows = ExcelHelper.countTotalRows(sheet, headerRow);

        SuggestedMapping mapping =
                ExcelHelper.detectMapping(headers);


        ExcelImportSession session = ExcelImportSession.builder()
                .sessionId(sessionId)
                .originalFileName(fileName)
                .totalRows(totalRows)
                .importedRows(0)
                .skippedRows(0)
                .uploadedBy(user)
                .detectedHeaders(toJson(headers))
                .status(ExcelImportSession.ImportStatus.PROCESSING)
                .build();

        sessionRepository.save(session);

        ExcelPreviewDto dto = ExcelPreviewDto.builder()
                .sessionId(sessionId)
                .fileName(file.getOriginalFilename())
                .sheetName(sheet.getSheetName())
                .headers(headers)
                .totalDataRows(totalRows)
                .previewRows(previewRows)
                .suggestedMapping(mapping)
                .build();

        return ApiResponse.success("Preview generated successfully", dto);

    }

    // ─────────────────────────────────────────────────────────────────────────
    // STEP 2 — CONFIRM & PERSIST
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Called after admin confirms column mapping.
     * Reads the original file again and persists every row as DynamicData.
     */
    @Transactional
    public ApiResponse<ExcelImportResultDto> confirmImport(
            MultipartFile file,
            String  sessionId,
            String  nameColumn,
            String  phoneColumn,
            String  emailColumn,
            LeadStatus defaultStatus,
            User uploadedByUser) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        ExcelImportSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));


        // ✅ Security check — only the person who previewed can confirm
        // (or ADMIN can confirm anyone's session)
        if (!isAdmin(uploadedByUser)
                && ! session.getUploadedBy().getId().equals(uploadedByUser.getId())) {
            throw new SecurityException(
                    "You are not allowed to confirm this session");
        }

        User assignedEmployee = null;

        if (uploadedByUser.getRole().equals(Role.USER)){
             assignedEmployee = uploadedByUser;
        }


        LeadStatus status = defaultStatus != null ? defaultStatus : LeadStatus.NEW;

        // Save column mapping in session
        Map<String, String> mapping = Map.of(
                "nameColumn",  nameColumn  != null ? nameColumn  : "",
                "phoneColumn", phoneColumn != null ? phoneColumn : "",
                "emailColumn", emailColumn != null ? emailColumn : ""
        );
        session.setColumnMapping(objectMapper.writeValueAsString(mapping));

        List<String> skippedReasons = new ArrayList<>();
        int imported = 0, skipped = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {

            Sheet sheet     = workbook.getSheetAt(0);
            String sheetName = sheet.getSheetName();
            Row headerRow   = sheet.getRow(0);

            // Re-read headers
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(getCellStringValue(cell).trim());
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    skipped++;
                    skippedReasons.add("Row " + (i + 1) + ": empty row");
                    continue;
                }

                try {
                    // ── Build JSON from the row ───────────────────────────────
                    Map<String, String> rowMap = new LinkedHashMap<>();
                    for (int j = 0; j < headers.size(); j++) {
                        if (headers.get(j).isBlank()) continue;
                        Cell cell = row.getCell(j, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                        rowMap.put(headers.get(j), cell != null ? getCellStringValue(cell) : "");
                    }

                    String jsonData = objectMapper.writeValueAsString(rowMap);

                    // ── Extract resolved contact fields ───────────────────────
                    String resolvedName  = resolveField(rowMap, nameColumn,  NAME_HINTS);
                    String resolvedPhone = resolveField(rowMap, phoneColumn, PHONE_HINTS);
                    String resolvedEmail = resolveField(rowMap, emailColumn, EMAIL_HINTS);

                    DynamicData record = DynamicData.builder()
                            .data(jsonData)
                            .fileName(file.getOriginalFilename())
                            .rowNumber(i)
                            .sheetName(sheetName)
                            .resolvedName(resolvedName)
                            .resolvedPhone(resolvedPhone)
                            .resolvedEmail(resolvedEmail)
                            .mappedLeadStatus(status)
                            .assignedEmployee(assignedEmployee)
                            .uploadSessionId(sessionId)
                            .build();

                    dynamicDataRepository.save(record);
                    imported++;

                } catch (Exception e) {
                    skipped++;
                    skippedReasons.add("Row " + (i + 1) + ": " + e.getMessage());
                    log.warn("Skipped row {} in {}: {}", i + 1, file.getOriginalFilename(), e.getMessage());
                }
            }
        }

        // Update session
        session.setImportedRows(imported);
        session.setSkippedRows(skipped);
        session.setStatus(skipped > 0
                ? ExcelImportSession.ImportStatus.PARTIALLY_IMPORTED
                : ExcelImportSession.ImportStatus.COMPLETED);
        sessionRepository.save(session);

        // Build DTO
        ExcelImportResultDto dto = ExcelImportResultDto.builder()
                .sessionId(sessionId)
                .fileName(file.getOriginalFilename())
                .importedRows(imported)
                .skippedRows(skipped)
                .skippedReasons(skippedReasons)
                .status(session.getStatus().name())
                .build();

        log.info("Excel import done: {} | imported={} skipped={}",
                file.getOriginalFilename(), imported, skipped);

        return ApiResponse.success("Import completed successfully", dto);
    }



    public ApiResponse<List<ExcelSessionResponseDto>> getSessions(User currentUser){

        List<ExcelImportSession> sessions;

        if (isAdmin(currentUser)) {
            // ✅ Admin sees ALL completed sessions
            sessions = sessionRepository
                    .findCompletedSessionsOrderByUploadedAtDesc();
        } else {
            // ✅ User sees ONLY sessions he uploaded
            sessions = sessionRepository
                    .findSessionsForUser(currentUser.getId());
        }

        List<ExcelSessionResponseDto> result = sessions.stream()
                .map(session -> mapSession(session, currentUser))
                .toList();

        return ApiResponse.success("Excel sessions fetch successfully.",result);
    }



    public ApiResponse<DynamicDataResponseDto> updateRowStatus(Long id,
                                                               LeadStatus status,User currentUser) {
        DynamicData row = dynamicDataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Row not found: " + id));

        // ✅ User can only update his own assigned rows
        if (!isAdmin(currentUser)) {
            if (row.getAssignedEmployee() == null
                    || !row.getAssignedEmployee().getId()
                    .equals(currentUser.getId())) {
                throw new SecurityException(
                        "You are not allowed to update this row. "
                                + "This row is not assigned to you.");
            }
        }
        row.setMappedLeadStatus(status);
        dynamicDataRepository.save(row);
        return ApiResponse.success("Row status updated successfully.",dynamicDataConvertToResponse(row));
    }

//    // ─────────────────────────────────────────────────────────────────────────
//    // DELETE SESSION (Undo Import)
//    // ─────────────────────────────────────────────────────────────────────────
//
//    @Transactional
//    public void deleteSession(String sessionId) {
//        dynamicDataRepository.deleteByUploadSessionId(sessionId);
//        sessionRepository.deleteById(sessionId);
//        log.info("Import session {} deleted (undo import)", sessionId);
//    }
//



     public ApiResponse<List<DynamicDataResponseDto>> getRowsBySession(String sessionId,User currentUser){
         List<DynamicData> rows;

         if (isAdmin(currentUser)) {
             // ✅ Admin sees ALL rows of this session
             rows = dynamicDataRepository.findByUploadSessionId(sessionId);
         } else {
             // ✅ User sees ONLY rows assigned to him in this session
             rows = dynamicDataRepository
                     .findByUploadSessionIdAndAssignedEmployee_Id(
                             sessionId, currentUser.getId());
         }
         // Convert each row — parse data String → Map
         List<DynamicDataResponseDto> result = rows.stream()
                 .map(row -> dynamicDataConvertToResponse(row))
                 .collect(Collectors.toList());

         return ApiResponse.success("Session dynamic data fetched successfully.",result);
     }


     public ApiResponse<List<DynamicDataResponseDto>> assignMultipleRows(BulkAssignRequest request) {

         User agent = employeeRepository.findById(request.getAgentId())
                 .orElseThrow(() -> new ResourceNotFoundException(
                         "Agent not found: " + request.getAgentId()));

         List<DynamicData> rows = dynamicDataRepository.findAllById(request.getRowIds());

         if (rows.isEmpty()) {
             throw new ResourceNotFoundException("No rows found");
         }

         // ✅ Filter out already-assigned rows
         List<DynamicData> alreadyAssigned = rows.stream()
                 .filter(r -> r.getAssignedEmployee() != null)
                 .collect(Collectors.toList());


         List<DynamicData> toAssign = rows.stream()
                 .filter(r -> r.getAssignedEmployee() == null)
                 .collect(Collectors.toList());

         // Assign only unassigned rows
         for (DynamicData row : toAssign) {
             row.setAssignedEmployee(agent);
         }
         dynamicDataRepository.saveAll(toAssign);

         // Build response with summary
         Map<String, Object> summary = new LinkedHashMap<>();
         summary.put("assignedCount", toAssign.size());
         summary.put("skippedAlreadyAssigned", alreadyAssigned.stream()
                 .map(r -> Map.of(
                         "rowId", r.getId(),
                         "assignedTo", r.getAssignedEmployee().getName()
                 ))
                 .collect(Collectors.toList()));

         List<DynamicDataResponseDto> responseDtoList = toAssign.stream()
                 .map(this::dynamicDataConvertToResponse)
                 .collect(Collectors.toList());

         return ApiResponse.success(
                 "Assigned " + toAssign.size() + " rows. Skipped " +
                         alreadyAssigned.size() + " already assigned rows.",
                 responseDtoList);
     }

    public ApiResponse<List<DynamicDataResponseDto>> getUnassignedRows(String sessionId) {
        List<DynamicData> rows = dynamicDataRepository
                .findByUploadSessionIdAndAssignedEmployeeIsNull(sessionId);

        List<DynamicDataResponseDto> result = rows.stream()
                .map(this::dynamicDataConvertToResponse)
                .collect(Collectors.toList());

        return ApiResponse.success(
                "Unassigned rows fetched. Count: " + result.size(), result);
    }


    /**
     * Finds the best matching column header from the given list of hint keywords.
     * Returns the actual header string as it appears in the Excel file.
     */
    private String detectColumn(List<String> headers, List<String> hints) {
        for (String hint : hints) {
            for (String header : headers) {
                if (header.toLowerCase().trim().equals(hint.toLowerCase())) {
                    return header;  // exact match preferred
                }
            }
        }
        // Partial match fallback
        for (String hint : hints) {
            for (String header : headers) {
                if (header.toLowerCase().contains(hint.toLowerCase())) {
                    return header;
                }
            }
        }
        return null;
    }

    /**
     * Extracts a field value from the row map.
     * Uses admin-specified column name first; falls back to hint-based detection.
     */
    private String resolveField(Map<String, String> row, String specifiedColumn, List<String> hints) {
        if (specifiedColumn != null && !specifiedColumn.isBlank()) {
            String val = row.get(specifiedColumn);
            if (val != null && !val.isBlank()) return val.trim();
        }
        // Fallback: try hints against actual row keys
        for (String hint : hints) {
            for (Map.Entry<String, String> entry : row.entrySet()) {
                if (entry.getKey().toLowerCase().contains(hint.toLowerCase())) {
                    String val = entry.getValue();
                    if (val != null && !val.isBlank()) return val.trim();
                }
            }
        }
        return null;
    }

    /** Converts any cell type to a String value cleanly */
    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                // Avoid scientific notation like "1.0" for numbers
                double num = cell.getNumericCellValue();
                if (num == Math.floor(num)) {
                    return String.valueOf((long) num);
                }
                return String.valueOf(num);

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case FORMULA:
                // Evaluate formula and get its cached result
                try {
                    FormulaEvaluator evaluator = cell.getSheet()
                            .getWorkbook()
                            .getCreationHelper()
                            .createFormulaEvaluator();
                    CellValue evaluated = evaluator.evaluate(cell);
                    switch (evaluated.getCellType()) {
                        case STRING:  return evaluated.getStringValue().trim();
                        case NUMERIC: return String.valueOf((long) evaluated.getNumberValue());
                        case BOOLEAN: return String.valueOf(evaluated.getBooleanValue());
                        default:      return "";
                    }
                } catch (Exception e) {
                    return cell.toString(); // fallback
                }

            case BLANK:
            case _NONE:
            default:
                return "";
        }
    }

    private boolean isRowEmpty(Row row) {
        for (Cell cell : row) {
            if (cell.getCellType() != CellType.BLANK
                    && !getCellStringValue(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }


    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON conversion failed");
        }
    }


    // Helper method — converts DynamicData entity to clean DTO
    private DynamicDataResponseDto dynamicDataConvertToResponse(DynamicData row) {
        try {
            // ✅ Parse the JSON string into a real Map
            Map<String, Object> dataMap = objectMapper.readValue(
                    row.getData(),
                    new TypeReference<Map<String, Object>>() {}
            );

            return DynamicDataResponseDto.builder()
                    .id(row.getId())
                    .data(dataMap)                          // ✅ Map not String
                   // .fileName(row.getFileName())
                    .rowNumber(row.getRowNumber())
                   // .sheetName(row.getSheetName())
                  //  .resolvedName(row.getResolvedName())
                    //.resolvedEmail(row.getResolvedEmail())
                   // .resolvedPhone(row.getResolvedPhone())
                  //  .mappedLeadStatus(row.getMappedLeadStatus())
                    .status(row.getMappedLeadStatus())
                   // .uploadSessionId(row.getUploadSessionId())
                    .uploadedAt(row.getUploadedAt())
                    .assignedEmployeeId(
                            row.getAssignedEmployee() != null
                                    ? row.getAssignedEmployee().getId() : null)
                    .assignedEmployeeName(
                            row.getAssignedEmployee() != null
                                    ? row.getAssignedEmployee().getName() : null)
                    .build();

        } catch (Exception e) {
            // If JSON parsing fails — return empty map with error
            return DynamicDataResponseDto.builder()
                    .id(row.getId())
                    .data(Map.of("error", "Could not parse data", "raw", row.getData()))
                   // .fileName(row.getFileName())
                  //  .uploadSessionId(row.getUploadSessionId())
                    .build();
        }

}

    private boolean isAdmin(User user) {
        // Change Role.ADMIN to whatever your admin role enum value is
        return user.getRole() == Role.ADMIN;
    }

    private ExcelSessionResponseDto mapSession(ExcelImportSession session, User currentUser) {

        List<DynamicData> rows;

        if (isAdmin(currentUser)) {
            rows = dynamicDataRepository.findByUploadSessionId(session.getSessionId());
        } else {
            rows = dynamicDataRepository
                    .findByUploadSessionIdAndAssignedEmployeeId(
                            session.getSessionId(), currentUser.getId());
        }

        long totalDynamicRows = rows.size();

        Map<String, Long> statusCount = rows.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getMappedLeadStatus().name(),
                        Collectors.counting()
                ));

        // ✅ Add these two lines
        long assignedCount   = rows.stream()
                .filter(r -> r.getAssignedEmployee() != null).count();
        long unassignedCount = totalDynamicRows - assignedCount;

        return ExcelSessionResponseDto.builder()
                .sessionId(session.getSessionId())
                .originalFileName(session.getOriginalFileName())
                .importedRows(session.getImportedRows())
                .skippedRows(session.getSkippedRows())
                .importStatus(session.getStatus().name())
                .uploadedAt(session.getUploadedAt())
                .uploadedById(session.getUploadedBy().getId())
                .uploadedByName(session.getUploadedBy().getName())
                .totalRows((int) totalDynamicRows)
                .totalDynamicRows(totalDynamicRows)
                .leadStatusCount(statusCount)
                .assignedCount(assignedCount)       // ✅
                .unassignedCount(unassignedCount)   // ✅
                .build();
    }


}
