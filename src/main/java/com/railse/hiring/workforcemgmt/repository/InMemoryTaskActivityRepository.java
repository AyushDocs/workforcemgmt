package com.railse.hiring.workforcemgmt.repository;

import com.railse.hiring.workforcemgmt.model.TaskActivity;
import com.railse.hiring.workforcemgmt.model.TaskComment;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryTaskActivityRepository implements TaskActivityRepository {

    private final Map<Long, TaskActivity> taskActivityStore = new
            ConcurrentHashMap<>();

    private final AtomicLong idCounter = new AtomicLong(0);

    @Override
    public List<TaskActivity> findByTaskIdOrderByTimestampAsc(Long taskId) {
        return taskActivityStore.values()
                .stream()
                .filter(c -> c.getTaskId().equals(taskId))
                .sorted(Comparator.comparing(TaskActivity::getTimestamp).reversed())
                .toList();
    }

    @Override
    public void save(TaskActivity t) {
        long id=t.getId()==null?idCounter.incrementAndGet():t.getId();
        t.setId(id);
        taskActivityStore.put(id, t);
    }
}

