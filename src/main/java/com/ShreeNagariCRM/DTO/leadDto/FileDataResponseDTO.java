package com.ShreeNagariCRM.DTO.leadDto;

import java.util.List;
import java.util.Map;

public class FileDataResponseDTO {

    private String fileName;
    private List<Map<String, Object>> data;

    public FileDataResponseDTO(String fileName, List<Map<String, Object>> data) {
        this.fileName = fileName;
        this.data = data;
    }

    // getters & setters


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }
}