package dev.salis.dlsf.x.bitcoinexplorer.reducer;

import akka.actor.typed.receptionist.Receptionist;
import dev.salis.dlsf.bitcoin.data.Block;
import dev.salis.dlsf.bitcoin.network.BitcoinNetworkCoordinatorProtocol;
import dev.salis.dlsf.core.reducer.BaseReducerProtocol;
import java.util.List;
import java.util.Map;

public class ReducerProtocol extends BaseReducerProtocol {

  public static class BlocksUpdate implements Message {

    private Map<String, Block> blockMap;
    private List<String> mainChainBlockHashList;

    public Map<String, Block> getBlockMap() {
      return blockMap;
    }

    public void setBlockMap(Map<String, Block> blockMap) {
      this.blockMap = blockMap;
    }

    public List<String> getMainChainBlockHashList() {
      return mainChainBlockHashList;
    }

    public void setMainChainBlockHashList(List<String> mainChainBlockHashList) {
      this.mainChainBlockHashList = mainChainBlockHashList;
    }
  }

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

  interface InternalMessage extends Message {

  }

  static final class Listing implements InternalMessage {

    private Receptionist.Listing listing;

    public Listing(Receptionist.Listing listing) {
      this.listing = listing;
    }

    public Listing() {
    }

    public Receptionist.Listing getListing() {
      return listing;
    }

    public void setListing(Receptionist.Listing listing) {
      this.listing = listing;
    }
  }

  static final class NetworkTopology implements InternalMessage {

    private Map<String, List<String>> outboundConnections;
    private Map<String, Integer> totalNumOfConnections;

    public NetworkTopology(
        BitcoinNetworkCoordinatorProtocol.NetworkTopologyResponse networkTopologyResponse) {
      this.outboundConnections = networkTopologyResponse.getOutboundConnections();
      this.totalNumOfConnections = networkTopologyResponse.getTotalNumOfConnections();
    }

    public Map<String, List<String>> getOutboundConnections() {
      return outboundConnections;
    }

    public void setOutboundConnections(Map<String, List<String>> outboundConnections) {
      this.outboundConnections = outboundConnections;
    }

    public Map<String, Integer> getTotalNumOfConnections() {
      return totalNumOfConnections;
    }

    public void setTotalNumOfConnections(Map<String, Integer> totalNumOfConnections) {
      this.totalNumOfConnections = totalNumOfConnections;
    }
  }
}
