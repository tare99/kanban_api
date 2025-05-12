package com.nsoft.integrations.vibra.kanban_api.domain.exception;

public class MissingVersionException extends RuntimeException {
  public MissingVersionException(String message) {
    super(message);
  }
}
