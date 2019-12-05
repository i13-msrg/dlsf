package dev.salis.dlsf.x.bitcointxprotocols;

import java.io.Serializable;

public class SimulationConfig implements Serializable {

  private int numOfNodesPerPod;
  private int maxBlockHeight;
  private double miningDifficulty;
  private long mineAttemptIntervalInMillis;
  private long createTxIntervalInMillis;
  private double blockReward;
  private double genesisBlockTxOutForEachNode;
  private int maxInboundConnections;
  private int maxOutboundConnections;
  private int statsUpdateIntervalInMillis;
  private long networkTopologySeed;
  private int blockSizeLimit;
  private int erlayReconciliationInterval;

  public SimulationConfig() {
  }

  public int getNumOfNodesPerPod() {
    return numOfNodesPerPod;
  }

  public void setNumOfNodesPerPod(int numOfNodesPerPod) {
    this.numOfNodesPerPod = numOfNodesPerPod;
  }

  public int getMaxBlockHeight() {
    return maxBlockHeight;
  }

  public void setMaxBlockHeight(int maxBlockHeight) {
    this.maxBlockHeight = maxBlockHeight;
  }

  public double getMiningDifficulty() {
    return miningDifficulty;
  }

  public void setMiningDifficulty(double miningDifficulty) {
    this.miningDifficulty = miningDifficulty;
  }

  public long getMineAttemptIntervalInMillis() {
    return mineAttemptIntervalInMillis;
  }

  public void setMineAttemptIntervalInMillis(long mineAttemptIntervalInMillis) {
    this.mineAttemptIntervalInMillis = mineAttemptIntervalInMillis;
  }

  public long getCreateTxIntervalInMillis() {
    return createTxIntervalInMillis;
  }

  public void setCreateTxIntervalInMillis(long createTxIntervalInMillis) {
    this.createTxIntervalInMillis = createTxIntervalInMillis;
  }

  public double getBlockReward() {
    return blockReward;
  }

  public void setBlockReward(double blockReward) {
    this.blockReward = blockReward;
  }

  public int getMaxInboundConnections() {
    return maxInboundConnections;
  }

  public void setMaxInboundConnections(int maxInboundConnections) {
    this.maxInboundConnections = maxInboundConnections;
  }

  public int getMaxOutboundConnections() {
    return maxOutboundConnections;
  }

  public void setMaxOutboundConnections(int maxOutboundConnections) {
    this.maxOutboundConnections = maxOutboundConnections;
  }

  public int getStatsUpdateIntervalInMillis() {
    return statsUpdateIntervalInMillis;
  }

  public void setStatsUpdateIntervalInMillis(int statsUpdateIntervalInMillis) {
    this.statsUpdateIntervalInMillis = statsUpdateIntervalInMillis;
  }

  public int getBlockSizeLimit() {
    return blockSizeLimit;
  }

  public void setBlockSizeLimit(int blockSizeLimit) {
    this.blockSizeLimit = blockSizeLimit;
  }

  public int getErlayReconciliationInterval() {
    return erlayReconciliationInterval;
  }

  public void setErlayReconciliationInterval(int erlayReconciliationInterval) {
    this.erlayReconciliationInterval = erlayReconciliationInterval;
  }

  public double getGenesisBlockTxOutForEachNode() {
    return genesisBlockTxOutForEachNode;
  }

  public void setGenesisBlockTxOutForEachNode(double genesisBlockTxOutForEachNode) {
    this.genesisBlockTxOutForEachNode = genesisBlockTxOutForEachNode;
  }

  public long getNetworkTopologySeed() {
    return networkTopologySeed;
  }

  public void setNetworkTopologySeed(long networkTopologySeed) {
    this.networkTopologySeed = networkTopologySeed;
  }
}
