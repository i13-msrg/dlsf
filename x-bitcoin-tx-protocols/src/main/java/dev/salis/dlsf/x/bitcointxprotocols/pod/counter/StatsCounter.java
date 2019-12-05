package dev.salis.dlsf.x.bitcointxprotocols.pod.counter;

public class StatsCounter {

  private int receivedBlockMessages = 0;
  private int receivedTxMessages = 0;
  private int receivedRedundantTxMessages = 0;
  private int receivedReconciliationMessages = 0;

  public void receivedBlockMessage() {
    this.receivedBlockMessages++;
  }

  public void receivedTxMessage() {
    this.receivedTxMessages++;
  }

  public void receivedRedundantTxMessage() {
    this.receivedRedundantTxMessages++;
  }

  public void receivedReconciliationMessage() {
    this.receivedReconciliationMessages++;
  }

  public int getReceivedBlockMessages() {
    return receivedBlockMessages;
  }

  public void setReceivedBlockMessages(int receivedBlockMessages) {
    this.receivedBlockMessages = receivedBlockMessages;
  }

  public int getReceivedTxMessages() {
    return receivedTxMessages;
  }

  public void setReceivedTxMessages(int receivedTxMessages) {
    this.receivedTxMessages = receivedTxMessages;
  }

  public int getReceivedRedundantTxMessages() {
    return receivedRedundantTxMessages;
  }

  public void setReceivedRedundantTxMessages(int receivedRedundantTxMessages) {
    this.receivedRedundantTxMessages = receivedRedundantTxMessages;
  }

  public int getReceivedReconciliationMessages() {
    return receivedReconciliationMessages;
  }

  public void setReceivedReconciliationMessages(int receivedReconciliationMessages) {
    this.receivedReconciliationMessages = receivedReconciliationMessages;
  }
}
