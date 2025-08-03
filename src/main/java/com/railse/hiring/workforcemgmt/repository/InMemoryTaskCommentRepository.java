package com.railse.hiring.workforcemgmt.repository;

import com.railse.hiring.workforcemgmt.common.model.enums.ReferenceType;
import com.railse.hiring.workforcemgmt.model.TaskComment;
import com.railse.hiring.workforcemgmt.model.TaskManagement;
import com.railse.hiring.workforcemgmt.model.enums.Priority;
import com.railse.hiring.workforcemgmt.model.enums.Task;
import com.railse.hiring.workforcemgmt.model.enums.TaskStatus;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryTaskCommentRepository implements TaskCommentRepository {

    private final Map<Long, TaskComment> taskCommentStore = new
            ConcurrentHashMap<>();

    private final AtomicLong idCounter = new AtomicLong(0);

    @Override
    public List<TaskComment> findByTaskIdOrderByTimestampAsc(Long id) {
        return taskCommentStore.values()
                .stream()
                .filter(c -> c.getTaskId().equals(id))
                .sorted(Comparator.comparing(TaskComment::getTimestamp).reversed())
                .toList();
    }

    @Override
    public void save(TaskComment t) {
        long id=idCounter.incrementAndGet();
        t.setId(id);
        taskCommentStore.put(id, t);
    }
}

