package com.railse.hiring.workforcemgmt.service.impl;

import com.railse.hiring.workforcemgmt.common.exception.ResourceNotFoundException;
import com.railse.hiring.workforcemgmt.dto.*;
import com.railse.hiring.workforcemgmt.mapper.ITaskManagementMapper;
import com.railse.hiring.workforcemgmt.model.TaskManagement;
import com.railse.hiring.workforcemgmt.model.enums.Task;
import com.railse.hiring.workforcemgmt.model.enums.TaskStatus;
import com.railse.hiring.workforcemgmt.repository.TaskRepository;
import com.railse.hiring.workforcemgmt.service.TaskManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskManagementServiceImpl implements TaskManagementService {

    private final TaskRepository taskRepository;
    private final ITaskManagementMapper taskMapper;

    @Override
    public TaskManagementDto findTaskById(Long id) {
        TaskManagement task = taskRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return taskMapper.modelToDto(task);
    }

    @Override
    public List<TaskManagementDto> createTasks(TaskCreateRequest
                                                       createRequest) {
        List<TaskManagement> createdTasks = new ArrayList<>();
        createRequest.getRequests()
                .stream()
                .map(item->{
                    TaskManagement newTask = new TaskManagement();
                    newTask.setReferenceId(item.getReferenceId());
                    newTask.setReferenceType(item.getReferenceType());
                    newTask.setTask(item.getTask());
                    newTask.setAssigneeId(item.getAssigneeId());
                    newTask.setPriority(item.getPriority());
                    newTask.setTaskDeadlineTime(item.getTaskDeadlineTime());
                    newTask.setStatus(TaskStatus.ASSIGNED);
                    newTask.setDescription("New task created.");
                    return newTask;
                })
                .forEach(t->{
                    createdTasks.add(t);
                    taskRepository.save(t);
                });
        return taskMapper.modelListToDtoList(createdTasks);
    }

    @Override
    public List<TaskManagementDto> updateTasks(UpdateTaskRequest
                                                       updateRequest) {
        List<TaskManagement> updatedTasks = new ArrayList<>();
        for (UpdateTaskRequest.RequestItem item : updateRequest.getRequests()) {
            TaskManagement task = taskRepository
                    .findById(item.getTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + item.getTaskId()));

            if (item.getTaskStatus() != null) {
                task.setStatus(item.getTaskStatus());
            }
            if (item.getDescription() != null) {
                task.setDescription(item.getDescription());
            }
            updatedTasks.add(taskRepository.save(task));
        }
        return taskMapper.modelListToDtoList(updatedTasks);
    }

    @Override
    public String assignByReference(AssignByReferenceRequest request) {
        List<Task> applicableTasks = Task.getTasksByReferenceType(request.getReferenceType());
        List<TaskManagement> existingTasks = taskRepository
                        .findByReferenceIdAndReferenceType(request.getReferenceId(),request.getReferenceType());

        for (Task taskType : applicableTasks) {
            List<TaskManagement> tasksOfType = existingTasks.stream()
                    .filter(t -> t.getTask() == taskType && t.getStatus()
                            != TaskStatus.COMPLETED)
                    .sorted(Comparator.comparing(TaskManagement::getTaskDeadlineTime))
                    .toList();

            // BUG #1 is here. It should assign one and cancel the rest.
            // Instead, it reassigns ALL of them.
            if (!tasksOfType.isEmpty()) {
                TaskManagement taskToAssign = tasksOfType.get(0);
                taskToAssign.setAssigneeId(request.getAssigneeId());
                taskRepository.save(taskToAssign);

                for (int i = 1; i < tasksOfType.size(); i++) {
                    TaskManagement redundant = tasksOfType.get(i);
                    redundant.setStatus(TaskStatus.CANCELLED); // or equivalent
                    taskRepository.save(redundant);
                }
            } else {
                // Create a new task if none exist
                TaskManagement newTask = new TaskManagement();
                newTask.setReferenceId(request.getReferenceId());
                newTask.setReferenceType(request.getReferenceType());
                newTask.setTask(taskType);
                newTask.setAssigneeId(request.getAssigneeId());
                newTask.setStatus(TaskStatus.ASSIGNED);
                taskRepository.save(newTask);
            }
        }
        return "Tasks assigned successfully for reference " + request.getReferenceId();
    }

    @Override
    public List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest
                                                            request) {
        List<TaskManagement> tasks =
                taskRepository.findByAssigneeIdIn(request.getAssigneeIds());

        // BUG #2 is here. It should filter out CANCELLED tasks but
        //doesn't.
        List<TaskManagement> filteredTasks = tasks.stream()
                .filter(task -> !task.getStatus().equals(TaskStatus.CANCELLED))
                .filter(task -> {
                    Long deadline = task.getTaskDeadlineTime();
                    return deadline != null &&
                            deadline >= request.getStartDate() &&
                            deadline <= request.getEndDate();
                })
                .collect(Collectors.toList());

        return taskMapper.modelListToDtoList(filteredTasks);
    }
}