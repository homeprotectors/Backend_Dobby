package com.homeprotectors.backend.exception;

import com.homeprotectors.backend.dto.common.ResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDTO<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getAllErrors()
                .getFirst()
                .getDefaultMessage();  // 첫 번째 에러만 추출 (선택적으로 개선 가능)

        return ResponseEntity
                .badRequest()
                .body(new ResponseDTO<>(false, errorMessage, null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDTO<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity
                .badRequest()
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
    }
}
