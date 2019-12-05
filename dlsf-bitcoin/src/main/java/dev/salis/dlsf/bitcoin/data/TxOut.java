package dev.salis.dlsf.bitcoin.data;

import java.io.Serializable;

/**
 * Implementation of Bitcoin Transaction Output
 */
public class TxOut implements Serializable {

  private String recipient;
  private Double value;

  public TxOut() {
  }

  public TxOut(String recipient, Double value) {
    this.recipient = recipient;
    this.value = value;
  }

  public String getRecipient() {
    return recipient;
  }

  public void setRecipient(String recipient) {
    this.recipient = recipient;
  }

  public Double getValue() {
    return value;
  }

  public void setValue(Double value) {
    this.value = value;
  }
}
