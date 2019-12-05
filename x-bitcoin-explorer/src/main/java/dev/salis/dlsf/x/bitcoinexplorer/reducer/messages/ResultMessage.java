package dev.salis.dlsf.x.bitcoinexplorer.reducer.messages;

import dev.salis.dlsf.bitcoin.data.Block;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultMessage implements Serializable {

  private Map<String, Block> blockMap = new HashMap<>();
  private StatsMessage stats;
  private List<String> mainChainBlockHashList;
  private Map<String, List<String>> networkTopology;
  private Map<String, Integer> totalNumberOfConnections;

  public Map<String, Block> getBlockMap() {
    return blockMap;
  }

  public void setBlockMap(Map<String, Block> blockMap) {
    this.blockMap = blockMap;
  }

  public StatsMessage getStats() {
    return stats;
  }

  public void setStats(StatsMessage stats) {
    this.stats = stats;
  }

  public List<String> getMainChainBlockHashList() {
    return mainChainBlockHashList;
  }

  public void setMainChainBlockHashList(List<String> mainChainBlockHashList) {
    this.mainChainBlockHashList = mainChainBlockHashList;
  }

  public Map<String, List<String>> getNetworkTopology() {
    return networkTopology;
  }

  public void setNetworkTopology(Map<String, List<String>> networkTopology) {
    this.networkTopology = networkTopology;
  }

  public Map<String, Integer> getTotalNumberOfConnections() {
    return totalNumberOfConnections;
  }

  public void setTotalNumberOfConnections(Map<String, Integer> totalNumberOfConnections) {
    this.totalNumberOfConnections = totalNumberOfConnections;
  }
}
