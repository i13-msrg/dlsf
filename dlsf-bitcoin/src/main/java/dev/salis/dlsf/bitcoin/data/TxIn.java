package dev.salis.dlsf.bitcoin.data;

import java.io.Serializable;
import java.util.Objects;

/**
 * Implementation of Bitcoin Transaction Input
 */
public class TxIn implements Serializable {

  private String prevTxHash;
  private int prevTxOutIndex;

  public TxIn() {
  }

  public TxIn(String prevTxHash, int prevTxOutIndex) {
    this.prevTxHash = prevTxHash;
    this.prevTxOutIndex = prevTxOutIndex;
  }

  public String getPrevTxHash() {
    return prevTxHash;
  }

  public void setPrevTxHash(String prevTxHash) {
    this.prevTxHash = prevTxHash;
  }

  public int getPrevTxOutIndex() {
    return prevTxOutIndex;
  }

  public void setPrevTxOutIndex(int prevTxOutIndex) {
    this.prevTxOutIndex = prevTxOutIndex;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TxIn txIn = (TxIn) o;
    return prevTxOutIndex == txIn.prevTxOutIndex && prevTxHash.equals(txIn.prevTxHash);
  }

  @Override
  public int hashCode() {
    return Objects.hash(prevTxHash, prevTxOutIndex);
  }
}
