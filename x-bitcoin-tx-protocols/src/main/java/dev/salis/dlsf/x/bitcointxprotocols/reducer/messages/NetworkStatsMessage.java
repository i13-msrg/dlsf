package dev.salis.dlsf.x.bitcointxprotocols.reducer.messages;

public class NetworkStatsMessage {

  private double receivedBlockMessageCount = 0;
  private double receivedTxMessageCount = 0;
  private double receivedRedundantTxMessageCount = 0;
  private double receivedReconciliationMessageCount = 0;

  public double getReceivedBlockMessageCount() {
    return receivedBlockMessageCount;
  }

  public void setReceivedBlockMessageCount(double receivedBlockMessageCount) {
    this.receivedBlockMessageCount = receivedBlockMessageCount;
  }

  public double getReceivedTxMessageCount() {
    return receivedTxMessageCount;
  }

  public void setReceivedTxMessageCount(double receivedTxMessageCount) {
    this.receivedTxMessageCount = receivedTxMessageCount;
  }

  public double getReceivedRedundantTxMessageCount() {
    return receivedRedundantTxMessageCount;
  }

  public void setReceivedRedundantTxMessageCount(double receivedRedundantTxMessageCount) {
    this.receivedRedundantTxMessageCount = receivedRedundantTxMessageCount;
  }

  public double getReceivedReconciliationMessageCount() {
    return receivedReconciliationMessageCount;
  }

  public void setReceivedReconciliationMessageCount(double receivedReconciliationMessageCount) {
    this.receivedReconciliationMessageCount = receivedReconciliationMessageCount;
  }
}
