package dev.salis.dlsf.x.bitcointxprotocols.reducer.messages;

public class ResultMessage {

  private StatsMessage stats;
  private NetworkStatsMessage networkStats;

  public StatsMessage getStats() {
    return stats;
  }

  public void setStats(StatsMessage stats) {
    this.stats = stats;
  }

  public NetworkStatsMessage getNetworkStats() {
    return networkStats;
  }

  public void setNetworkStats(NetworkStatsMessage networkStats) {
    this.networkStats = networkStats;
  }
}
