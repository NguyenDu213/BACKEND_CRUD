package com.backend.backend_crud.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ApiResponse<T> {
    private boolean status;
    private String message;
    private T data;
}