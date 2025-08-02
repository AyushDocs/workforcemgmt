package com.railse.hiring.workforcemgmt.common.model.response;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.railse.hiring.workforcemgmt.common.exception.StatusCode;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Response<T> {
    private T data;
    private Pagination pagination;
    private ResponseStatus status;

    public Response(T data, Pagination pagination, ResponseStatus status) {
        this.data = data;
        this.pagination = pagination;
        this.status = status;
    }

    public Response(T data) {
        this(data, null, new ResponseStatus(StatusCode.SUCCESS.getCode(),
                StatusCode.SUCCESS.getMessage()));
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Pagination {
        // Not implemented for this assignment
    }
}