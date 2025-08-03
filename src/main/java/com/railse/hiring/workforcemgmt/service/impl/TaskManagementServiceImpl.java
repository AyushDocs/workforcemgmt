package com.railse.hiring.workforcemgmt.service.impl;

import com.railse.hiring.workforcemgmt.common.exception.ResourceNotFoundException;
import com.railse.hiring.workforcemgmt.dto.*;
import com.railse.hiring.workforcemgmt.mapper.ITaskActivityMapper;
import com.railse.hiring.workforcemgmt.mapper.ITaskCommentMapper;
import com.railse.hiring.workforcemgmt.mapper.ITaskManagementMapper;
import com.railse.hiring.workforcemgmt.mapper.ITaskUpdateMapper;
import com.railse.hiring.workforcemgmt.model.TaskActivity;
import com.railse.hiring.workforcemgmt.model.TaskComment;
import com.railse.hiring.workforcemgmt.model.TaskManagement;
import com.railse.hiring.workforcemgmt.model.enums.Priority;
import com.railse.hiring.workforcemgmt.model.enums.Task;
import com.railse.hiring.workforcemgmt.model.enums.TaskStatus;
import com.railse.hiring.workforcemgmt.repository.TaskActivityRepository;
import com.railse.hiring.workforcemgmt.repository.TaskCommentRepository;
import com.railse.hiring.workforcemgmt.repository.TaskRepository;
import com.railse.hiring.workforcemgmt.service.TaskManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskManagementServiceImpl implements TaskManagementService {

    private final TaskRepository taskRepository;
    private final ITaskManagementMapper taskMapper;
    private final TaskActivityRepository activityRepo;
    private final TaskCommentRepository commentRepo;
    private final ITaskCommentMapper commentMapper;
    private final ITaskActivityMapper activityMapper;
    private final ITaskUpdateMapper updateMapper;

    @Override
    public TaskManagementDto findTaskById(Long id) {
        TaskManagement task = taskRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        List<TaskComment> comments = commentRepo.findByTaskIdOrderByTimestampAsc(id);
        List<TaskActivity> activities = activityRepo.findByTaskIdOrderByTimestampAsc(id);

        TaskManagementDto dto=taskMapper.modelToDto(task);
        dto.setComments(commentMapper.modelListToDtoList(comments));
        dto.setActivityHistory(activityMapper.modelListToDtoList(activities));

        return dto;
    }

    @Override
    public List<UpdateTaskDto> fetchTasksByPriority(String priority) {
        Priority p=switch(priority){
            case "HIGH"-> Priority.HIGH;
            case "MEDIUM"-> Priority.MEDIUM;
            case "LOW"-> Priority.LOW;
            default -> throw new IllegalStateException("Unexpected value: " + priority);
        };
        return updateMapper.modelListToDtoList(taskRepository.findByPriority(p));
    }

    @Override
    public UpdateTaskDto updatePriority(PriorityUpdateRequest request) {
        Optional<TaskManagement> taskOpt= taskRepository.findById(request.getTaskId());
        if(taskOpt.isEmpty())
            throw new RuntimeException();
        TaskManagement task=taskOpt.get();
        task.setPriority(request.getPriority());
        taskRepository.save(task);
        return updateMapper.modelToDto(task);
    }

    @Override
    public List<TaskManagementDto> createTasks(TaskCreateRequest
                                                       createRequest) {
        List<TaskManagement> createdTasks = new ArrayList<>();
        createRequest.getRequests()
                .forEach(item->{
                    TaskManagement newTask = new TaskManagement();
                    newTask.setReferenceId(item.getReferenceId());
                    newTask.setReferenceType(item.getReferenceType());
                    newTask.setTask(item.getTask());
                    newTask.setAssigneeId(item.getAssigneeId());
                    newTask.setPriority(item.getPriority());
                    newTask.setTaskDeadlineTime(item.getTaskDeadlineTime());
                    newTask.setStatus(TaskStatus.ASSIGNED);
                    newTask.setDescription("New task created.");
                    taskRepository.save(newTask);
                    activityRepo.save(new TaskActivity(null,newTask.getId(),"User "+item.getAssigneeId()+"  created this task", System.currentTimeMillis()));
                    createdTasks.add(newTask);
                });
        return taskMapper.modelListToDtoList(createdTasks);
    }

    @Override
    public List<UpdateTaskDto> updateTasks(UpdateTaskRequest
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
            activityRepo.save(new TaskActivity(null,
                    task.getId(),
                    "User "+task.getAssigneeId()+"  modified this task. Status: " + task.getStatus()+"description: "+task.getDescription(),
                    System.currentTimeMillis()));
            updatedTasks.add(taskRepository.save(task));
        }
        return updateMapper.modelListToDtoList(updatedTasks);
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


            if (!tasksOfType.isEmpty()) {
                TaskManagement taskToAssign = tasksOfType.get(0);
                taskToAssign.setAssigneeId(request.getAssigneeId());
                taskRepository.save(taskToAssign);
                activityRepo.save(new TaskActivity(null,taskToAssign.getId(),"assigned to new person"+request.getAssigneeId(),System.currentTimeMillis()));
                for (int i = 1; i < tasksOfType.size(); i++) {
                    TaskManagement redundant = tasksOfType.get(i);
                    redundant.setStatus(TaskStatus.CANCELLED); // or equivalent
                    taskRepository.save(redundant);
                    activityRepo.save(new TaskActivity(null,taskToAssign.getId(),"cancelling the redundant activity"+request.getAssigneeId(),System.currentTimeMillis()));

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
                activityRepo.save(new TaskActivity(null,newTask.getId(),"created and assigned to new person"+request.getAssigneeId(),System.currentTimeMillis()));

            }
        }
        return "Tasks assigned successfully for reference " + request.getReferenceId();
    }

    @Override
    public List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest
                                                            request) {
        List<TaskManagement> tasks =
                taskRepository.findByAssigneeIdIn(request.getAssigneeIds());
        List<TaskManagement> filteredTasks = tasks.stream()
                .filter(task -> !task.getStatus().equals(TaskStatus.CANCELLED))
                .filter(task -> {
                    Long deadline = task.getTaskDeadlineTime();
                    if(deadline==null)
                        return false;
                    return deadline > request.getStartDate() && deadline < request.getEndDate();
                })
                .collect(Collectors.toList());
        return taskMapper.modelListToDtoList(filteredTasks);
    }
}