package com.railse.hiring.workforcemgmt.mapper;

import com.railse.hiring.workforcemgmt.dto.TaskManagementDto;
import com.railse.hiring.workforcemgmt.model.TaskActivity;
import com.railse.hiring.workforcemgmt.model.TaskComment;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy =
        NullValuePropertyMappingStrategy.IGNORE)
public interface ITaskActivityMapper {
    ITaskActivityMapper INSTANCE =
            Mappers.getMapper(ITaskActivityMapper.class);

    TaskManagementDto.TaskActivityDto modelToDto(TaskActivity model);
    TaskActivity dtoToModel(TaskManagementDto.TaskActivityDto dto);

    List<TaskManagementDto.TaskActivityDto> modelListToDtoList(List<TaskActivity>
                                                       models);
}