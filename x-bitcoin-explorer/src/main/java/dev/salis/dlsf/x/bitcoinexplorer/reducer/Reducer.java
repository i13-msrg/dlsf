package dev.salis.dlsf.x.bitcoinexplorer.reducer;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import dev.salis.dlsf.bitcoin.network.BitcoinNetworkCoordinator;
import dev.salis.dlsf.bitcoin.network.BitcoinNetworkCoordinatorProtocol;
import dev.salis.dlsf.bitcoin.network.BitcoinNetworkCoordinatorProtocol.NetworkTopologyRequest;
import dev.salis.dlsf.core.master.MasterProtocol.SimulationRunResult;
import dev.salis.dlsf.core.master.MasterProtocol.SimulationRunUpdate;
import dev.salis.dlsf.core.run.RunContext;
import dev.salis.dlsf.x.bitcoinexplorer.SimulationConfig;
import dev.salis.dlsf.x.bitcoinexplorer.reducer.ReducerProtocol.BlocksUpdate;
import dev.salis.dlsf.x.bitcoinexplorer.reducer.ReducerProtocol.Listing;
import dev.salis.dlsf.x.bitcoinexplorer.reducer.ReducerProtocol.NetworkTopology;
import dev.salis.dlsf.x.bitcoinexplorer.reducer.messages.ResultMessage;
import dev.salis.dlsf.x.bitcoinexplorer.reducer.messages.StatsMessage;
import java.util.Date;

public class Reducer extends AbstractBehavior<ReducerProtocol.Message> {

  private boolean isSimulationRunEnded = false;
  private final int numOfGenesisTxs;
  private ActorContext<ReducerProtocol.Message> context;
  private ActorRef<SimulationRunUpdate> gateway;
  private BlocksUpdate blocksUpdate;
  private StatsMessage stats;
  private Date firstUpdateTime;
  private NetworkTopology networkTopologyMessage;

  public Reducer(ActorContext<ReducerProtocol.Message> actorContext, RunContext runContext) {
    this.context = actorContext;
    SimulationConfig simulationConfig = runContext.getSimulationConfig(SimulationConfig.class);
    if (simulationConfig.getGenesisBlockTxOutForEachNode() != 0) {
      this.numOfGenesisTxs = simulationConfig.getNumOfNodesPerPod();
    } else {
      this.numOfGenesisTxs = 0;
    }
    this.gateway = runContext.getUpdateGateway();
    final ActorRef<Receptionist.Listing> listingMsgAdapter =
        context.messageAdapter(Receptionist.Listing.class, Listing::new);
    context
        .getSystem()
        .receptionist()
        .tell(Receptionist.subscribe(BitcoinNetworkCoordinator.SERVICE_KEY, listingMsgAdapter));
  }

  public static Behavior<ReducerProtocol.Message> createBehavior(RunContext runContext) {
    return Behaviors.setup(context -> new Reducer(context, runContext));
  }

  @Override
  public Receive<ReducerProtocol.Message> createReceive() {
    return newReceiveBuilder()
        .onMessage(
            ReducerProtocol.BlocksUpdate.class,
            msg -> {
              this.blocksUpdate = msg;
              return this;
            })
        .onMessage(
            Listing.class,
            msg -> {
              final var coordinatorOptional =
                  msg.getListing().getServiceInstances(BitcoinNetworkCoordinator.SERVICE_KEY)
                      .stream()
                      .findFirst();
              if (coordinatorOptional.isEmpty() || this.isSimulationRunEnded) {
                return this;
              }
              final ActorRef<BitcoinNetworkCoordinatorProtocol.NetworkTopologyResponse>
                  networkTopologyResponseMsgAdapter =
                  context.messageAdapter(
                      BitcoinNetworkCoordinatorProtocol.NetworkTopologyResponse.class,
                      NetworkTopology::new);
              NetworkTopologyRequest networkTopologyRequest = new NetworkTopologyRequest();
              networkTopologyRequest.setReplyTo(networkTopologyResponseMsgAdapter);
              coordinatorOptional.get().tell(networkTopologyRequest);
              return this;
            })
        .onMessage(
            NetworkTopology.class,
            msg -> {
              this.networkTopologyMessage = msg;
              return this;
            })
        .onMessage(
            ReducerProtocol.StatsUpdate.class,
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
                  ((double) (statsMessage.getConfirmedBlockCount()) / (double) secondsPassed));
              statsMessage.setTxPerSecond(
                  ((double) (statsMessage.getConfirmedTransactionCount() - this.numOfGenesisTxs)
                      / (double) secondsPassed));
              this.stats = statsMessage;
              this.gateway.tell(new SimulationRunUpdate("stats", statsMessage));
              return this;
            })
        .onMessage(
            ReducerProtocol.SimulationEnded.class,
            msg -> {
              this.isSimulationRunEnded = true;
              return this;
            })
        .onMessage(
            ReducerProtocol.ResultRequest.class,
            msg -> {
              ResultMessage resultMessage = new ResultMessage();
              if (this.blocksUpdate != null) {
                resultMessage.setBlockMap(this.blocksUpdate.getBlockMap());
                resultMessage.setStats(this.stats);
                resultMessage.setMainChainBlockHashList(
                    this.blocksUpdate.getMainChainBlockHashList());
                resultMessage.setNetworkTopology(
                    this.networkTopologyMessage.getOutboundConnections());
                resultMessage.setTotalNumberOfConnections(
                    this.networkTopologyMessage.getTotalNumOfConnections());
              }
              msg.getReplyTo().tell(new SimulationRunResult(resultMessage));
              return this;
            })
        .build();
  }
}
