package com.homeprotectors.backend.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "공통 API 응답 기본 구조")
public class ResponseDTO<T> {
    private boolean success;
    private String message;
    private T data;
}