package com.railse.hiring.workforcemgmt.mapper;

import com.railse.hiring.workforcemgmt.dto.TaskManagementDto;
import com.railse.hiring.workforcemgmt.dto.UpdateTaskDto;
import com.railse.hiring.workforcemgmt.model.TaskActivity;
import com.railse.hiring.workforcemgmt.model.TaskManagement;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy =
        NullValuePropertyMappingStrategy.IGNORE)
public interface ITaskUpdateMapper {
    ITaskUpdateMapper INSTANCE =
            Mappers.getMapper(ITaskUpdateMapper.class);

    UpdateTaskDto modelToDto(TaskManagement model);
    TaskManagement dtoToModel(UpdateTaskDto dto);


    List<UpdateTaskDto> modelListToDtoList(List<TaskManagement> updatedTasks);
}