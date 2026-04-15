package org.example.library.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex) {
        return build(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ProblemDetail handleBadRequest(BadRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    @ExceptionHandler(OperationNotAllowedException.class)
    public ProblemDetail handleOperationNotAllowed(OperationNotAllowedException ex) {
        return build(HttpStatus.BAD_REQUEST, "Operation Not Allowed", ex.getMessage());
    }

    @ExceptionHandler({
            ConcurrencyException.class,
            ObjectOptimisticLockingFailureException.class
    })
    public ProblemDetail handleConcurrency(ConcurrencyException ex) {
        return build(HttpStatus.CONFLICT, "Concurrency Conflict", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "Something went wrong");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {

        if (ex.getRequiredType() != null &&
                ex.getRequiredType().equals(java.time.LocalDateTime.class)) {

            return build(
                    HttpStatus.BAD_REQUEST,
                    "Invalid Date Format",
                    "Expected ISO format: yyyy-MM-dd'T'HH:mm:ss"
            );
        }

        return build(
                HttpStatus.BAD_REQUEST,
                "Invalid Parameter",
                "Request parameter type mismatch"
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining(", "));

        return build(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                message
        );
    }

    // ✅ Reusable builder (VERY IMPORTANT improvement)
    private ProblemDetail build(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setTitle(title);
        problem.setDetail(detail);
        problem.setProperty("timestamp", System.currentTimeMillis());
        return problem;
    }
}