package com.ShreeNagariCRM.Repository;

import com.ShreeNagariCRM.Entity.Leads;
import com.ShreeNagariCRM.Entity.enums.LeadStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadRepository extends JpaRepository<Leads,Long> {

    List<Leads> findByStatus(LeadStatus status);

    List<Leads> findByAssignedEmp_Id(Long agentId);

    List<Leads> findByAssignedEmp_IdAndStatus(Long agentId, LeadStatus status);

    long countByAssignedEmp_Id(Long agentId);

    long countByStatus(LeadStatus status);

    @Query("""
        SELECT l FROM Leads l
        WHERE LOWER(l.name)  LIKE LOWER(CONCAT('%',:q,'%'))
           OR LOWER(l.email) LIKE LOWER(CONCAT('%',:q,'%'))
           OR l.phone        LIKE CONCAT('%',:q,'%')
        """)
    List<Leads> search(@Param("q") String query);

    /** Bulk reassign when an agent resigns / is absent */
    @Modifying
    @Transactional
    @Query("UPDATE Leads l SET l.assignedEmp.id = :toId WHERE l.assignedEmp.id = :fromId")
    int reassignAll(@Param("fromId") Long fromAgentId, @Param("toId") Long toAgentId);
}
