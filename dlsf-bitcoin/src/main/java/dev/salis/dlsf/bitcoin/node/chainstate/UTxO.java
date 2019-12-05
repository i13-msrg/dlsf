package dev.salis.dlsf.bitcoin.node.chainstate;

import java.util.Objects;

/**
 * Unspent transaction output data structure.
 */
public class UTxO {

  private String txHash;
  private int txOutIndex;

  public UTxO() {}

  public UTxO(String txHash, int txOutIndex) {
    this.txHash = txHash;
    this.txOutIndex = txOutIndex;
  }

  public String getTxHash() {
    return txHash;
  }

  public void setTxHash(String txHash) {
    this.txHash = txHash;
  }

  public int getTxOutIndex() {
    return txOutIndex;
  }

  public void setTxOutIndex(int txOutIndex) {
    this.txOutIndex = txOutIndex;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UTxO uTxO = (UTxO) o;
    return getTxOutIndex() == uTxO.getTxOutIndex() && getTxHash().equals(uTxO.getTxHash());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getTxHash(), getTxOutIndex());
  }
}
