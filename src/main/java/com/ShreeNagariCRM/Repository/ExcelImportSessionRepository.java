package com.ShreeNagariCRM.Repository;

import com.ShreeNagariCRM.Entity.ExcelImportSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExcelImportSessionRepository extends JpaRepository<ExcelImportSession,String> {
    List<ExcelImportSession> findAllByOrderByUploadedAtDesc();

    @Query("SELECT s FROM ExcelImportSession s " +
            "WHERE s.status IN ('COMPLETED', 'PARTIALLY_IMPORTED') " +
            "ORDER BY s.uploadedAt DESC")
    List<ExcelImportSession> findCompletedSessionsOrderByUploadedAtDesc();

    // ✅ NEW — for USER (only sessions he uploaded)
    @Query("SELECT s FROM ExcelImportSession s " +
            "WHERE s.status IN ('COMPLETED', 'PARTIALLY_IMPORTED') " +
            "AND s.uploadedBy.id = :userId " +
            "ORDER BY s.uploadedAt DESC")
    List<ExcelImportSession> findCompletedSessionsByUploadedByOrderByUploadedAtDesc(
            @Param("userId") Long userId);

    @Query("""
    SELECT DISTINCT s FROM ExcelImportSession s
    LEFT JOIN DynamicData d ON d.uploadSessionId = s.sessionId
    WHERE s.status IN ('COMPLETED', 'PARTIALLY_IMPORTED')
    AND (
        s.uploadedBy.id = :userId
        OR d.assignedEmployee.id = :userId
    )
    ORDER BY s.uploadedAt DESC
""")
    List<ExcelImportSession> findSessionsForUser(@Param("userId") Long userId);

    List<ExcelImportSession> findByUploadedById(Long agentId);
}
