package org.example.exceptions;

/**
 * Runtime exception that describes errors which may occur during Kepler registration creations
 */
public class ListenerRegistrationFailedException extends RuntimeException {
  public ListenerRegistrationFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
