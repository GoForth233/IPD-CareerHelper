package com.group1.career.exception;

import com.group1.career.common.ErrorCode;
import com.group1.career.common.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle Custom Business Exception
     */
    @ExceptionHandler(BizException.class)
    public Result<?> handleBizException(BizException e) {
        log.warn("Business Exception: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * Handle JSR-303 @Valid validation failures on @RequestBody
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse(ErrorCode.PARAM_ERROR.getMessage());
        log.warn("Validation Error: {}", message);
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * Handle @Validated constraint violations on method parameters
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("Constraint Violation: {}", e.getMessage());
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), e.getMessage());
    }

    /**
     * Handle Global Exception
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("System Error", e);
        return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), "System Error: " + e.getMessage());
    }
}

