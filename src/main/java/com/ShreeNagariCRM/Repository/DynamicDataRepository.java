package com.ShreeNagariCRM.Repository;

import com.ShreeNagariCRM.Entity.DynamicData;
import com.ShreeNagariCRM.Entity.enums.LeadStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface DynamicDataRepository extends JpaRepository<DynamicData,Long> {


    List<DynamicData> findByFileName(String fileName);

    @Query("SELECT DISTINCT d.fileName FROM DynamicData d")
    List<String> findAllFileNames();

    Optional<DynamicData> findByIdAndFileName(Long id, String fileName);

    /** All rows from one upload session */
    List<DynamicData> findByUploadSessionId(String sessionId);

    /** By CRM status */
    List<DynamicData> findByMappedLeadStatus(LeadStatus status);

    /** All rows assigned to an agent */
    List<DynamicData> findByAssignedEmployee_Id(Long agentId);

    long countByUploadSessionId(String sessionId);

    // DynamicDataRepository
    List<DynamicData> findByUploadSessionIdAndAssignedEmployeeIsNull(String sessionId);


    /**
     * Full-text search on the resolved fields only (not the JSON blob —
     * that would be a database JSON function and not portable).
     */
    @Query("""
        SELECT d FROM DynamicData d
        WHERE LOWER(d.resolvedName)  LIKE LOWER(CONCAT('%',:q,'%'))
           OR LOWER(d.resolvedEmail) LIKE LOWER(CONCAT('%',:q,'%'))
           OR d.resolvedPhone        LIKE CONCAT('%',:q,'%')
           OR LOWER(d.fileName)      LIKE LOWER(CONCAT('%',:q,'%'))
        """)
    List<DynamicData> search(@Param("q") String query);

    /** Bulk reassign dynamic leads when agent leaves */
    @Modifying
    @Transactional
    @Query("UPDATE DynamicData d SET d.assignedEmployee.id = :toId WHERE d.assignedEmployee.id = :fromId")
    int reassignAll(@Param("fromId") Long fromId, @Param("toId") Long toId);

    /** Delete all rows from one session (undo import) */
    @Modifying
    @Transactional
    void deleteByUploadSessionId(String sessionId);

    // DynamicDataRepository.java

    // Count by sessionId + leadStatus
    @Query("SELECT d.mappedLeadStatus, COUNT(d) " +
            "FROM DynamicData d " +
            "WHERE d.uploadSessionId = :sessionId " +
            "GROUP BY d.mappedLeadStatus")
    List<Object[]> countBySessionIdGroupByStatus(
            @Param("sessionId") String sessionId);

    // ✅ Used by USER — get only his assigned rows in a session
    List<DynamicData> findByUploadSessionIdAndAssignedEmployee_Id(
            String uploadSessionId, Long employeeId);

    List<DynamicData> findByUploadSessionIdAndAssignedEmployeeId(
            String sessionId, Long employeeId);

}
