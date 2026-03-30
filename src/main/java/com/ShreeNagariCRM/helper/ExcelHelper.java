package com.ShreeNagariCRM.helper;

import com.ShreeNagariCRM.DTO.excelDto.ExcelSessionResponseDto;
import com.ShreeNagariCRM.DTO.excelDto.SuggestedMapping;
import com.ShreeNagariCRM.Entity.ExcelImportSession;
import com.ShreeNagariCRM.Entity.enums.LeadStatus;
import com.ShreeNagariCRM.Repository.DynamicDataRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Component
@RequiredArgsConstructor
public class ExcelHelper {

    private final DynamicDataRepository dynamicDataRepository;

    // ── Keywords used for auto-detecting column purposes ─────────────────────
    private static final List<String> NAME_HINTS =
            List.of("name", "customer name", "client name", "full name",
                    "client", "customer", "buyer", "person", "contact name");

    private static final List<String> PHONE_HINTS =
            List.of("phone", "mobile", "contact", "cell", "number",
                    "phone number", "mobile number", "contact number", "mob", "ph");

    private static final List<String> EMAIL_HINTS =
            List.of("email", "mail", "email id", "email address", "e-mail");

    public static Workbook getWorkbook(MultipartFile file) {
        try {
            return WorkbookFactory.create(file.getInputStream());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Excel file");
        }
    }

    public static Row findHeaderRow(Sheet sheet) {
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null && !isRowEmpty(row)) {
                return row;
            }
        }
        throw new IllegalArgumentException("Header row not found");
    }

    public static List<String> extractHeaders(Row headerRow) {
        List<String> headers = new ArrayList<>();

        for (Cell cell : headerRow) {
            headers.add(getCellValue(cell).trim());
        }

        return headers;
    }

    public static List<Map<String, String>> extractPreviewRows(
            Sheet sheet, Row headerRow, List<String> headers) {

        List<Map<String, String>> preview = new ArrayList<>();

        for (int i = headerRow.getRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) continue;

            if (preview.size() < 5) {
                Map<String, String> map = new LinkedHashMap<>();

                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    map.put(headers.get(j), cell != null ? getCellValue(cell) : "");
                }

                preview.add(map);
            }
        }
        return preview;
    }

    public static int countTotalRows(Sheet sheet, Row headerRow) {
        int count = 0;

        for (int i = headerRow.getRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null && !isRowEmpty(row)) {
                count++;
            }
        }
        return count;
    }



    public static SuggestedMapping detectMapping(List<String> headers) {

        String name  = find(headers, merge("name", NAME_HINTS));
        String phone = find(headers, merge("phone", PHONE_HINTS));
        String email = find(headers, merge("email", EMAIL_HINTS));

        String confidence = (hasText(name) && hasText(phone))
                ? "HIGH"
                : (hasText(name) ? "MEDIUM" : "LOW");

        return SuggestedMapping.builder()
                .nameColumn(name != null ? name : "")
                .phoneColumn(phone != null ? phone : "")
                .emailColumn(email != null ? email : "")
                .confidence(confidence)
                .build();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static List<String> merge(String primary, List<String> hints) {
        List<String> list = new ArrayList<>();
        list.add(primary.toLowerCase());
        list.addAll(hints);
        return list;
    }

    private static String find(List<String> headers, List<String> hints) {

        for (String header : headers) {
            String lowerHeader = header.toLowerCase();

            for (String hint : hints) {
                if (lowerHeader.contains(hint.toLowerCase())) {
                    return header;
                }
            }
        }
        return null; // important change
    }

    private static String getCellValue(Cell cell) {
        return new DataFormatter().formatCellValue(cell);
    }

    private static boolean isRowEmpty(Row row) {
        for (Cell cell : row) {
            if (cell != null && !getCellValue(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }


}
