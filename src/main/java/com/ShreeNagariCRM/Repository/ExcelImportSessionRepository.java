package com.ShreeNagariCRM.Repository;

import com.ShreeNagariCRM.Entity.ExcelImportSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExcelImportSessionRepository extends JpaRepository<ExcelImportSession,String> {
    List<ExcelImportSession> findAllByOrderByUploadedAtDesc();

    List<ExcelImportSession> findByUploadedById(Long agentId);
}
