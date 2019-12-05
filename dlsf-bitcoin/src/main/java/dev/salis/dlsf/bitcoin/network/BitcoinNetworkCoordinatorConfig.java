package dev.salis.dlsf.bitcoin.network;

/**
 * Configuration that determines how BitcoinNetworkCoordinator creates a topology.
 */
public class BitcoinNetworkCoordinatorConfig {

  private Long seed;
  private int totalNumOfNodes;
  private int maxInboundConnections;
  private int maxOutboundConnections;

  public BitcoinNetworkCoordinatorConfig() {
  }

  public Long getSeed() {
    return seed;
  }

  public void setSeed(Long seed) {
    this.seed = seed;
  }

  public int getTotalNumOfNodes() {
    return totalNumOfNodes;
  }

  public void setTotalNumOfNodes(int totalNumOfNodes) {
    this.totalNumOfNodes = totalNumOfNodes;
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
}
