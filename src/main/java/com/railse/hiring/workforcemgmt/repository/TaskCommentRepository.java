package com.railse.hiring.workforcemgmt.repository;


import com.railse.hiring.workforcemgmt.model.TaskComment;
import com.railse.hiring.workforcemgmt.model.TaskManagement;
import com.railse.hiring.workforcemgmt.model.enums.Priority;

import java.util.List;
import java.util.Optional;

public interface TaskCommentRepository {

    List<TaskComment> findByTaskIdOrderByTimestampAsc(Long id);
    void save(TaskComment t);
}