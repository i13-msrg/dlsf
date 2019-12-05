package dev.salis.dlsf.bitcoin.node.exceptions;

public class TxRejectedException extends Exception {

  public TxRejectedException() {
    super();
  }

  public TxRejectedException(String message) {
    super(message);
  }

  public TxRejectedException(String message, Throwable cause) {
    super(message, cause);
  }

  public TxRejectedException(Throwable cause) {
    super(cause);
  }
}
