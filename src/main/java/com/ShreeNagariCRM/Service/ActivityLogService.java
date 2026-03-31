package com.ShreeNagariCRM.Service;


import com.ShreeNagariCRM.DTO.activityDto.ActivityLogResponse;
import com.ShreeNagariCRM.Entity.EmployeeActivityLog;
import com.ShreeNagariCRM.Entity.EmployeeActivityLog.ActivityAction;
import com.ShreeNagariCRM.Entity.User;
import com.ShreeNagariCRM.Repository.EmployeeActivityLogRepository;
import com.ShreeNagariCRM.DTO.baseDto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogService {

    private final EmployeeActivityLogRepository activityLogRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // LOG — called from other services (async so it never slows down main flow)
    // ─────────────────────────────────────────────────────────────────────────
    @Async
    public void log(User employee,
                    ActivityAction action,
                    EmployeeActivityLog.ActivityModule module,
                    Long recordId,
                    String description) {
        try {
            EmployeeActivityLog log = EmployeeActivityLog.builder()
                    .employee(employee)
                    .action(action)
                    .module(module)
                    .recordId(recordId)
                    .description(description)
                    .build();

            activityLogRepository.save(log);

        } catch (Exception e) {
            // Never fail the main request just because logging failed
            log.error("Activity log failed: {}", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET MY ACTIVITY LOG — employee sees only their own
    // ─────────────────────────────────────────────────────────────────────────
    public ApiResponse<List<ActivityLogResponse>> getMyLogs(User currentUser) {

        List<ActivityLogResponse> list = activityLogRepository
                .findByEmployeeIdOrderByPerformedAtDesc(currentUser.getId())
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ApiResponse.success(
                "Activity logs fetched successfully.", list);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET MY LOGS BY MODULE
    // ─────────────────────────────────────────────────────────────────────────
    public ApiResponse<List<ActivityLogResponse>> getMyLogsByModule(
            User currentUser, EmployeeActivityLog.ActivityModule module) {

        List<ActivityLogResponse> list = activityLogRepository
                .findByEmployeeIdAndModuleOrderByPerformedAtDesc(
                        currentUser.getId(), module)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ApiResponse.success(
                "Activity logs for module fetched successfully.", list);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONVERT
    // ─────────────────────────────────────────────────────────────────────────
    private ActivityLogResponse convertToResponse(EmployeeActivityLog log) {
        return ActivityLogResponse.builder()
                .id(log.getId())
                .employeeId(log.getEmployee().getId())
                .employeeName(log.getEmployee().getName())
                .action(log.getAction())
                .module(log.getModule())
                .recordId(log.getRecordId())
                .description(log.getDescription())
                .performedAt(log.getPerformedAt())
                .build();
    }
}
