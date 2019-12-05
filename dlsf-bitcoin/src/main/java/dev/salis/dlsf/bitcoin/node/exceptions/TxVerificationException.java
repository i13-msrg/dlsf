package dev.salis.dlsf.bitcoin.node.exceptions;

public class TxVerificationException extends Exception {

  public TxVerificationException() {
    super();
  }

  public TxVerificationException(String message) {
    super(message);
  }

  public TxVerificationException(String message, Throwable cause) {
    super(message, cause);
  }

  public TxVerificationException(Throwable cause) {
    super(cause);
  }
}
