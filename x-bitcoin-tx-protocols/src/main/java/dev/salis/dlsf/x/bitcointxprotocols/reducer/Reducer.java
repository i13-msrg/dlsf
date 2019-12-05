package dev.salis.dlsf.x.bitcointxprotocols.reducer;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import dev.salis.dlsf.core.master.MasterProtocol.SimulationRunResult;
import dev.salis.dlsf.core.master.MasterProtocol.SimulationRunUpdate;
import dev.salis.dlsf.core.reducer.BaseReducerProtocol.ResultRequest;
import dev.salis.dlsf.core.reducer.BaseReducerProtocol.SimulationEnded;
import dev.salis.dlsf.core.run.RunContext;
import dev.salis.dlsf.x.bitcointxprotocols.SimulationConfig;
import dev.salis.dlsf.x.bitcointxprotocols.reducer.ReducerProtocol.NetworkStatsUpdate;
import dev.salis.dlsf.x.bitcointxprotocols.reducer.ReducerProtocol.StatsUpdate;
import dev.salis.dlsf.x.bitcointxprotocols.reducer.messages.NetworkStatsMessage;
import dev.salis.dlsf.x.bitcointxprotocols.reducer.messages.ResultMessage;
import dev.salis.dlsf.x.bitcointxprotocols.reducer.messages.StatsMessage;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Reducer extends AbstractBehavior<ReducerProtocol.Message> {

  private final int numOfGenesisTxs;
  private ActorContext<ReducerProtocol.Message> context;
  private ActorRef<SimulationRunUpdate> gateway;
  private StatsMessage stats;
  private Map<String, NetworkStatsUpdate> networkStatsUpdateMap = new HashMap<>();
  private Date firstUpdateTime;

  public Reducer(ActorContext<ReducerProtocol.Message> actorContext, RunContext runContext) {
    this.context = actorContext;
    SimulationConfig simulationConfig = runContext.getSimulationConfig(SimulationConfig.class);
    if (simulationConfig.getGenesisBlockTxOutForEachNode() != 0) {
      this.numOfGenesisTxs = simulationConfig.getNumOfNodesPerPod();
    } else {
      this.numOfGenesisTxs = 0;
    }
    this.gateway = runContext.getUpdateGateway();
  }

  public static Behavior<ReducerProtocol.Message> createBehavior(RunContext runContext) {
    return Behaviors.setup(context -> new Reducer(context, runContext));
  }

  @Override
  public Receive<ReducerProtocol.Message> createReceive() {
    return newReceiveBuilder()
        .onMessage(
            StatsUpdate.class,
            msg -> {
              StatsMessage statsMessage = new StatsMessage();
              statsMessage.setConfirmedBlockCount(msg.getConfirmedBlockCount());
              statsMessage.setConfirmedTransactionCount(msg.getConfirmedTransactionCount());
              statsMessage.setVerifiedBlockCount(msg.getVerifiedBlockCount());
              statsMessage.setVerifiedTransactionCount(msg.getVerifiedTransactionCount());
              if (this.firstUpdateTime == null) {
                this.firstUpdateTime = new Date();
                return this;
              }
              long secondsPassed = (new Date().getTime() - this.firstUpdateTime.getTime()) / 1000;
              if (secondsPassed <= 0) {
                return this;
              }
              statsMessage.setBlockPerSecond(
                  ((double) statsMessage.getConfirmedBlockCount() / (double) secondsPassed));
              statsMessage.setTxPerSecond(
                  ((double) (statsMessage.getConfirmedTransactionCount() - this.numOfGenesisTxs)
                      / (double) secondsPassed));
              this.stats = statsMessage;
              this.gateway.tell(new SimulationRunUpdate("stats", statsMessage));
              return this;
            })
        .onMessage(
            NetworkStatsUpdate.class,
            msg -> {
              this.networkStatsUpdateMap.put(msg.getNodeName(), msg);
              return this;
            })
        .onMessage(
            SimulationEnded.class,
            msg -> {
              return this;
            })
        .onMessage(
            ResultRequest.class,
            msg -> {
              ResultMessage resultMessage = new ResultMessage();
              resultMessage.setStats(this.stats);
              resultMessage.setNetworkStats(generateNetworkStatsMessage());
              msg.getReplyTo().tell(new SimulationRunResult(resultMessage));
              return this;
            })
        .build();
  }

  private NetworkStatsMessage generateNetworkStatsMessage() {
    double size = (double) this.networkStatsUpdateMap.size();
    NetworkStatsMessage networkStats = new NetworkStatsMessage();
    for (NetworkStatsUpdate update : this.networkStatsUpdateMap.values()) {
      networkStats.setReceivedBlockMessageCount(
          networkStats.getReceivedBlockMessageCount()
              + (double) update.getReceivedBlockMessageCount() / size);
      networkStats.setReceivedTxMessageCount(
          networkStats.getReceivedTxMessageCount()
              + (double) update.getReceivedTxMessageCount() / size);
      networkStats.setReceivedRedundantTxMessageCount(
          networkStats.getReceivedRedundantTxMessageCount()
              + (double) update.getReceivedRedundantTxMessageCount() / size);
      networkStats.setReceivedReconciliationMessageCount(
          networkStats.getReceivedReconciliationMessageCount()
              + (double) update.getReceivedReconciliationMessageCount() / size);
    }
    return networkStats;
  }
}
