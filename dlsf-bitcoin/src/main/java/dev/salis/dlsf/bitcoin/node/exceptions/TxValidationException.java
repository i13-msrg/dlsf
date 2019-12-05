package dev.salis.dlsf.bitcoin.node.exceptions;

public class TxValidationException extends Exception {

  public TxValidationException() {
    super();
  }

  public TxValidationException(String message) {
    super(message);
  }

  public TxValidationException(String message, Throwable cause) {
    super(message, cause);
  }

  public TxValidationException(Throwable cause) {
    super(cause);
  }
}
