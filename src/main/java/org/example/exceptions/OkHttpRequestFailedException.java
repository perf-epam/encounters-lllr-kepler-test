package org.example.exceptions;

/**
 * Runtime exception that describes errors which may occur during OkHttp requests
 */
public class OkHttpRequestFailedException extends RuntimeException {
  public OkHttpRequestFailedException(String message) {
    super(message);
  }

  public OkHttpRequestFailedException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
