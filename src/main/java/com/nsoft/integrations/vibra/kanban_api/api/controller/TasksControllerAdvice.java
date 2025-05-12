package com.nsoft.integrations.vibra.kanban_api.api.controller;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.nsoft.integrations.vibra.kanban_api.api.response.BadRequestResponse;
import com.nsoft.integrations.vibra.kanban_api.api.response.ErrorResponse;
import com.nsoft.integrations.vibra.kanban_api.domain.exception.TaskNotFoundException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(assignableTypes = TaskController.class)
public class TasksControllerAdvice {
  @ExceptionHandler(TaskNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(TaskNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("Internal server error"));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<BadRequestResponse> handleInvalidEnum(HttpMessageNotReadableException ex) {
    Throwable rootCause = ex.getCause();
    if (rootCause instanceof JsonMappingException mappingException) {
      Map<String, String> errors = new HashMap<>();
      mappingException
          .getPath()
          .forEach(ref -> errors.put(ref.getFieldName(), "Invalid value for enum"));

      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new BadRequestResponse("One or more fields are invalid", errors));
    }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new BadRequestResponse("Invalid request payload", Map.of()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<BadRequestResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex) {
    return buildValidationErrorResponse(ex);
  }

  private ResponseEntity<BadRequestResponse> buildValidationErrorResponse(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new BadRequestResponse("One or more fields have validation error", errors));
  }
}
