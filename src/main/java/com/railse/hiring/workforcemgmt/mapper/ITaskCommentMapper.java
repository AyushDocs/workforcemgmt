package com.railse.hiring.workforcemgmt.mapper;

import com.railse.hiring.workforcemgmt.dto.TaskManagementDto;
import com.railse.hiring.workforcemgmt.model.TaskComment;
import com.railse.hiring.workforcemgmt.model.TaskManagement;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy =
        NullValuePropertyMappingStrategy.IGNORE)
public interface ITaskCommentMapper {
    ITaskCommentMapper INSTANCE =
            Mappers.getMapper(ITaskCommentMapper.class);

    TaskManagementDto.TaskCommentDto modelToDto(TaskComment model);
    TaskComment dtoToModel(TaskManagementDto.TaskCommentDto dto);

    List<TaskManagementDto.TaskCommentDto> modelListToDtoList(List<TaskComment>
                                                       models);
}