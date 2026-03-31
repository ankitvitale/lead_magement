package com.ShreeNagariCRM.Repository;



import com.ShreeNagariCRM.Entity.FollowUpTransferLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowUpTransferLogRepository
        extends JpaRepository<FollowUpTransferLog, Long> {

    // All transfer logs for one follow-up
    List<FollowUpTransferLog> findByFollowUpFollowUpIdOrderByTransferredAtDesc(
            Long followUpId);

    // All transfers done by admin
    List<FollowUpTransferLog> findByTransferredByIdOrderByTransferredAtDesc(
            Long adminId);
}
