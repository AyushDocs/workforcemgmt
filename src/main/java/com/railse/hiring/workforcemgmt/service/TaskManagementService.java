package com.railse.hiring.workforcemgmt.service;

import com.railse.hiring.workforcemgmt.dto.*;

import java.util.List;

public interface TaskManagementService {
    List<TaskManagementDto> createTasks(TaskCreateRequest request);
    List<UpdateTaskDto> updateTasks(UpdateTaskRequest request);
    String assignByReference(AssignByReferenceRequest request);
    List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest
                                                     request);
    TaskManagementDto findTaskById(Long id);

    List<UpdateTaskDto> fetchTasksByPriority(String priority);

    UpdateTaskDto updatePriority(PriorityUpdateRequest request);
}