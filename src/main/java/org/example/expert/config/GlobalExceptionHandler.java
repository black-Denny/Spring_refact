package org.example.expert.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<ValidationErrorResponse.ErrorField> errors = ex.getFieldErrors().stream()
                .map(fieldError -> new ValidationErrorResponse.ErrorField(
                        fieldError.getRejectedValue(),
                        fieldError.getField(),
                        fieldError.getDefaultMessage()))
                .toList();

        ValidationErrorResponse response = new ValidationErrorResponse("올바른 입력값을 입력해주세요.", errors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestException(InvalidRequestException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(ServerException.class)
    public ResponseEntity<ErrorResponse> handleServerException(ServerException ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message) {
        ErrorResponse response = new ErrorResponse(status.value(), status.name(), message);
        return ResponseEntity.status(status).body(response);
    }

    @AllArgsConstructor
    @Getter
    public static class ErrorResponse {
        private int code;
        private String status;
        private String message;
    }

    @AllArgsConstructor
    @Getter
    public static class ValidationErrorResponse {
        private String message;
        private List<ErrorField> errorFields;

        @AllArgsConstructor
        @Getter
        public static class ErrorField {
            private Object value;
            private String field;
            private String message;
        }
    }
}
