package com.railse.hiring.workforcemgmt.repository;


import com.railse.hiring.workforcemgmt.model.TaskActivity;
import com.railse.hiring.workforcemgmt.model.TaskManagement;
import com.railse.hiring.workforcemgmt.model.enums.Priority;

import java.util.List;
import java.util.Optional;

public interface TaskActivityRepository {

    List<TaskActivity> findByTaskIdOrderByTimestampAsc(Long id);
    void save(TaskActivity t);
}