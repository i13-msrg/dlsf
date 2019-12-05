package dev.salis.dlsf.x.bitcoinexplorer.reducer.messages;

import java.io.Serializable;

public class StatsMessage implements Serializable {

  private Double txPerSecond;
  private Double blockPerSecond;
  private int confirmedBlockCount;
  private int verifiedBlockCount;
  private int verifiedTransactionCount;
  private int confirmedTransactionCount;

  public Double getTxPerSecond() {
    return txPerSecond;
  }

  public void setTxPerSecond(Double txPerSecond) {
    this.txPerSecond = txPerSecond;
  }

  public Double getBlockPerSecond() {
    return blockPerSecond;
  }

  public void setBlockPerSecond(Double blockPerSecond) {
    this.blockPerSecond = blockPerSecond;
  }

  public int getConfirmedBlockCount() {
    return confirmedBlockCount;
  }

  public void setConfirmedBlockCount(int confirmedBlockCount) {
    this.confirmedBlockCount = confirmedBlockCount;
  }

  public int getVerifiedBlockCount() {
    return verifiedBlockCount;
  }

  public void setVerifiedBlockCount(int verifiedBlockCount) {
    this.verifiedBlockCount = verifiedBlockCount;
  }

  public int getVerifiedTransactionCount() {
    return verifiedTransactionCount;
  }

  public void setVerifiedTransactionCount(int verifiedTransactionCount) {
    this.verifiedTransactionCount = verifiedTransactionCount;
  }

  public int getConfirmedTransactionCount() {
    return confirmedTransactionCount;
  }

  public void setConfirmedTransactionCount(int confirmedTransactionCount) {
    this.confirmedTransactionCount = confirmedTransactionCount;
  }
}
