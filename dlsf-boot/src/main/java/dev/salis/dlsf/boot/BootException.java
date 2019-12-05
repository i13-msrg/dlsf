package dev.salis.dlsf.boot;

public class BootException extends RuntimeException {

  protected BootException(String message) {
    super(message);
  }

  protected BootException(String message, Throwable cause) {
    super(message, cause);
  }
}
