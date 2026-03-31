package com.ShreeNagariCRM.Repository;

import com.ShreeNagariCRM.Entity.EmployeeActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeActivityLogRepository
        extends JpaRepository<EmployeeActivityLog, Long> {

    // Employee sees only their own activity
    List<EmployeeActivityLog> findByEmployeeIdOrderByPerformedAtDesc(
            Long employeeId);

    // Filter by module
    List<EmployeeActivityLog> findByEmployeeIdAndModuleOrderByPerformedAtDesc(
            Long employeeId, EmployeeActivityLog.ActivityModule module);
}