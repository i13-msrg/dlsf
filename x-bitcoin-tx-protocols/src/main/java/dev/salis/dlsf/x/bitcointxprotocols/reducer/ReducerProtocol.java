package dev.salis.dlsf.x.bitcointxprotocols.reducer;

import dev.salis.dlsf.core.reducer.BaseReducerProtocol;

public class ReducerProtocol extends BaseReducerProtocol {

  public static class StatsUpdate implements Message {
    private int confirmedBlockCount;
    private int verifiedBlockCount;
    private int verifiedTransactionCount;
    private int confirmedTransactionCount;

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

  public static class NetworkStatsUpdate implements Message {

    private String nodeName;
    private int receivedBlockMessageCount = 0;
    private int receivedTxMessageCount = 0;
    private int receivedRedundantTxMessageCount = 0;
    private int receivedReconciliationMessageCount = 0;

    public String getNodeName() {
      return nodeName;
    }

    public void setNodeName(String nodeName) {
      this.nodeName = nodeName;
    }

    public int getReceivedBlockMessageCount() {
      return receivedBlockMessageCount;
    }

    public void setReceivedBlockMessageCount(int receivedBlockMessageCount) {
      this.receivedBlockMessageCount = receivedBlockMessageCount;
    }

    public int getReceivedTxMessageCount() {
      return receivedTxMessageCount;
    }

    public void setReceivedTxMessageCount(int receivedTxMessageCount) {
      this.receivedTxMessageCount = receivedTxMessageCount;
    }

    public int getReceivedRedundantTxMessageCount() {
      return receivedRedundantTxMessageCount;
    }

    public void setReceivedRedundantTxMessageCount(int receivedRedundantTxMessageCount) {
      this.receivedRedundantTxMessageCount = receivedRedundantTxMessageCount;
    }

    public int getReceivedReconciliationMessageCount() {
      return receivedReconciliationMessageCount;
    }

    public void setReceivedReconciliationMessageCount(int receivedReconciliationMessageCount) {
      this.receivedReconciliationMessageCount = receivedReconciliationMessageCount;
    }
  }

  interface InternalMessage extends Message {

  }
}
