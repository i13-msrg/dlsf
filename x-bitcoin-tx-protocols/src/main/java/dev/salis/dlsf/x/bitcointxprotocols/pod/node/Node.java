package dev.salis.dlsf.x.bitcointxprotocols.pod.node;

import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.ReceiveBuilder;
import akka.actor.typed.javadsl.TimerScheduler;
import dev.salis.dlsf.bitcoin.data.Block;
import dev.salis.dlsf.bitcoin.data.Tx;
import dev.salis.dlsf.bitcoin.data.TxIn;
import dev.salis.dlsf.bitcoin.data.TxOut;
import dev.salis.dlsf.bitcoin.network.BitcoinNetworkCoordinatorProtocol;
import dev.salis.dlsf.bitcoin.node.FullNode;
import dev.salis.dlsf.bitcoin.node.mempool.TxWithFee;
import dev.salis.dlsf.bitcoin.node.wallet.Wallet;
import dev.salis.dlsf.x.bitcointxprotocols.pod.PodProtocol.NodeFinished;
import dev.salis.dlsf.x.bitcointxprotocols.pod.counter.StatsCounter;
import dev.salis.dlsf.x.bitcointxprotocols.pod.node.NodeProtocol.CreateTx;
import dev.salis.dlsf.x.bitcointxprotocols.pod.node.NodeProtocol.ListingMsg;
import dev.salis.dlsf.x.bitcointxprotocols.pod.node.NodeProtocol.Message;
import dev.salis.dlsf.x.bitcointxprotocols.pod.node.NodeProtocol.MineBlock;
import dev.salis.dlsf.x.bitcointxprotocols.pod.node.NodeProtocol.NotifyFinished;
import dev.salis.dlsf.x.bitcointxprotocols.pod.node.NodeProtocol.PropagateBlock;
import dev.salis.dlsf.x.bitcointxprotocols.pod.node.NodeProtocol.PropagateTx;
import dev.salis.dlsf.x.bitcointxprotocols.pod.node.NodeProtocol.SendStats;
import dev.salis.dlsf.x.bitcointxprotocols.reducer.ReducerProtocol.NetworkStatsUpdate;
import dev.salis.dlsf.x.bitcointxprotocols.reducer.ReducerProtocol.StatsUpdate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public abstract class Node extends AbstractBehavior<NodeProtocol.Message> {

  protected final String nodeName;
  protected NodeConfig config;
  protected Set<ActorRef<NodeProtocol.Message>> neighbors;
  protected Set<ActorRef<NodeProtocol.Message>> inboundConnections;
  protected Set<ActorRef<NodeProtocol.Message>> outboundConnections;
  protected ActorContext<NodeProtocol.Message> context;
  protected StatsCounter statsCounter = new StatsCounter();
  protected TimerScheduler<NodeProtocol.Message> timers;
  protected String tryToMineTimerKey = "tryToMine";
  protected String tryToSendTxTimerKey = "tryToSendTx";
  protected String sendStatsTimerKey = "sendStats";

  protected FullNode fullNode;
  protected boolean isStopped = false;

  public Node(
      TimerScheduler<NodeProtocol.Message> timers,
      ActorContext<NodeProtocol.Message> context,
      NodeConfig config) {
    this.context = context;
    this.config = config;
    this.timers = timers;
    this.nodeName = this.createAddressForNode(config.getWorkerName(), config.getNodeIndex());
    String walletAddress = this.createAddressForNode(config.getWorkerName(), config.getNodeIndex());
    Block genesisBlock = new Block();
    genesisBlock.setHash("genesis");
    genesisBlock.setPrevBlockHash(null);
    genesisBlock.setMinerId(null);
    genesisBlock.setTime(new Date(1557156838954L));
    List<Tx> genesisTransactions = null;
    TxIn genesisTxInCandidate = null;
    // add initial txout for each node in genesis block
    if (this.config.getSimulationConfig().getGenesisBlockTxOutForEachNode() != 0.0) {
      genesisTransactions =
          new ArrayList<>(
              this.config.getWorkerNames().size()
                  * this.config.getSimulationConfig().getNumOfNodesPerPod());
      for (String workerName : this.config.getWorkerNames()) {
        for (int i = 0; i < this.config.getSimulationConfig().getNumOfNodesPerPod(); i++) {
          final String address = this.createAddressForNode(workerName, i);
          TxOut txOut =
              new TxOut(
                  address, this.config.getSimulationConfig().getGenesisBlockTxOutForEachNode());
          Tx tx = new Tx("GENESIS_TX_" + address, new ArrayList<>(), List.of(txOut));
          genesisTransactions.add(tx);
          if (walletAddress.equals(address)) {
            genesisTxInCandidate = new TxIn();
            genesisTxInCandidate.setPrevTxHash(tx.getHash());
            genesisTxInCandidate.setPrevTxOutIndex(0);
          }
        }
      }
    } else {
      genesisTransactions = new ArrayList<>();
    }
    genesisBlock.setTransactions(genesisTransactions);
    this.fullNode = new FullNode(genesisBlock, walletAddress);
    if (genesisTxInCandidate != null) {
      Wallet wallet = this.fullNode.getWallet();
      wallet.getCandidateTxInSet().add(genesisTxInCandidate);
    }
    final ActorRef<BitcoinNetworkCoordinatorProtocol.Listing> listingMsgAdapter =
        context.messageAdapter(
            BitcoinNetworkCoordinatorProtocol.Listing.class,
            msg ->
                new NodeProtocol.ListingMsg(
                    msg.getOutboundConnections(NodeProtocol.Message.class),
                    msg.getInboundConnections(NodeProtocol.Message.class)));
    config
        .getNetworkCoordinator()
        .tell(
            new BitcoinNetworkCoordinatorProtocol.Register(
                listingMsgAdapter, this.context.getSelf()));
  }

  //  public static Behavior<NodeProtocol.Message> createBehavior(NodeConfig config) {
  //    return Behaviors.withTimers(timers -> Behaviors.setup(ctx -> new Node(timers, ctx,
  // config)));
  //  }

  protected ReceiveBuilder<Message> createReceiveBuilder() {
    return newReceiveBuilder()
        .onMessage(
            ListingMsg.class,
            msg -> {
              if (this.neighbors == null && !msg.outboundConnections.isEmpty()) {
                this.inboundConnections = msg.inboundConnections;
                this.outboundConnections = msg.outboundConnections;
                this.neighbors =
                    new HashSet<>(msg.outboundConnections.size() + msg.inboundConnections.size());
                this.neighbors.addAll(msg.outboundConnections);
                this.neighbors.addAll(msg.inboundConnections);
                this.timers.startSingleTimer(
                    tryToMineTimerKey,
                    new MineBlock(),
                    Duration.ofMillis(
                        ThreadLocalRandom.current()
                            .nextLong(
                                0, config.getSimulationConfig().getMineAttemptIntervalInMillis())));
                this.timers.startSingleTimer(
                    tryToSendTxTimerKey,
                    new CreateTx(),
                    Duration.ofMillis(
                        ThreadLocalRandom.current()
                            .nextLong(
                                0, config.getSimulationConfig().getCreateTxIntervalInMillis())));
                if (this.config.isSendStatsToReducer()) {
                  this.context.getSelf().tell(new SendStats());
                }
              }
              return this;
            })
        .onMessage(
            PropagateBlock.class,
            msg -> {
              if (!this.context.getSelf().equals(msg.getSender())) {
                this.statsCounter.receivedBlockMessage();
              }
              Block block = msg.getBlock();
              boolean shouldPropagate = this.fullNode.receiveBlock(block);
              if (!shouldPropagate) {
                return this;
              }
              if (this.neighbors == null) {
                return this;
              }
              for (ActorRef<Message> neighbor : this.neighbors) {
                if (neighbor.equals(msg.getSender())) {
                  continue;
                }
                neighbor.tell(new PropagateBlock(msg.getBlock(), context.getSelf()));
              }
              if (this.fullNode.getBlockTree().getMainBranchLeafHeight()
                  >= config.getSimulationConfig().getMaxBlockHeight()) {
                this.initiateStop();
                return this;
              }
              return this;
            })
        .onMessage(
            SendStats.class,
            msg -> {
              this.timers.startSingleTimer(
                  sendStatsTimerKey,
                  new SendStats(),
                  Duration.ofMillis(config.getSimulationConfig().getStatsUpdateIntervalInMillis()));
              this.sendStats();
              return this;
            })
        .onMessage(
            MineBlock.class,
            msg -> {
              this.timers.startSingleTimer(
                  tryToMineTimerKey,
                  new MineBlock(),
                  Duration.ofMillis(config.getSimulationConfig().getMineAttemptIntervalInMillis()));
              boolean isMiningSuccessful =
                  ThreadLocalRandom.current().nextDouble()
                      >= config.getSimulationConfig().getMiningDifficulty();
              if (!isMiningSuccessful) {
                return this;
              }
              Block block = new Block();
              block.setTime(new Date());
              block.setMinerId(this.nodeName);
              block.setHash(this.getRandomHash());
              block.setPrevBlockHash(this.fullNode.getBlockTree().getMainBranchLeaf());
              List<TxWithFee> txWithFeeList =
                  this.fullNode.getMemPool().getVerifiedTxMap().values().stream()
                      .limit(this.config.getSimulationConfig().getBlockSizeLimit())
                      .collect(Collectors.toList());
              Double totalFee = 0.0;
              for (TxWithFee txWithFee : txWithFeeList) {
                totalFee += txWithFee.getFee();
              }
              List<Tx> txList = new ArrayList<>(txWithFeeList.size() + 1);
              Tx coinbaseTx = new Tx();
              coinbaseTx.setHash(this.getRandomHash());
              coinbaseTx.setInputList(new ArrayList<>());
              coinbaseTx.setOutputList(
                  List.of(
                      new TxOut(
                          this.fullNode.getWallet().getAddress(),
                          this.config.getSimulationConfig().getBlockReward() + totalFee)));
              txList.add(0, coinbaseTx);
              txList.addAll(txWithFeeList);
              block.setTransactions(txList);
              context.getSelf().tell(new PropagateBlock(block, context.getSelf()));
              return this;
            })
        .onMessage(
            CreateTx.class,
            msg -> {
              this.timers.startSingleTimer(
                  tryToSendTxTimerKey,
                  new CreateTx(),
                  Duration.ofMillis(config.getSimulationConfig().getCreateTxIntervalInMillis()));
              List<TxIn> candidateTxInList =
                  new ArrayList<>(this.fullNode.getWallet().getCandidateTxInSet());
              List<TxIn> unspentCandidateTxInList =
                  candidateTxInList.stream()
                      .filter(txIn -> this.fullNode.getMemPool().isUnspent(txIn))
                      .collect(Collectors.toList());
              if (unspentCandidateTxInList.isEmpty()) {
                return this;
              }
              int txInsToSpend =
                  ThreadLocalRandom.current().nextInt(0, unspentCandidateTxInList.size());
              // List returned from sublist is not serializable, hence the ArrayList constructor
              List<TxIn> inputList =
                  new ArrayList<>(unspentCandidateTxInList.subList(0, txInsToSpend + 1));
              Double totalValue = 0.0;
              for (TxIn txIn : inputList) {
                Double value = this.fullNode.getChainState().findPrevTxOut(txIn).getValue();
                totalValue += value;
              }
              Integer txOutCount = ThreadLocalRandom.current().nextInt(1, 6);
              List<TxOut> outputList = new ArrayList<>();
              for (int i = 0; i < txOutCount; i++) {
                String recipientNodeAddress = this.getRandomNodeAddress(this.config);
                outputList.add(
                    new TxOut(recipientNodeAddress, totalValue / txOutCount.doubleValue()));
              }
              Tx tx = new Tx(this.getRandomHash(), inputList, outputList);
              context.getSelf().tell(new PropagateTx(tx, context.getSelf()));
              return this;
            })
        .onMessage(
            NotifyFinished.class,
            msg -> {
              config.getPod().tell(new NodeFinished(nodeName));
              return Behaviors.same();
            });
  }

  private String getRandomHash() {
    return UUID.randomUUID().toString().replaceAll("-", "");
  }

  private String createAddressForNode(String workerName, int index) {
    return workerName + "-" + index;
  }

  private String getRandomNodeAddress(NodeConfig config) {
    int randomWorkerIndex = ThreadLocalRandom.current().nextInt(config.getWorkerNames().size());
    String randomWorkerName = config.getWorkerNames().get(randomWorkerIndex);
    int randomNodeIndex =
        ThreadLocalRandom.current().nextInt(config.getSimulationConfig().getNumOfNodesPerPod());
    return createAddressForNode(randomWorkerName, randomNodeIndex);
  }

  private void sendStats() {
    StatsUpdate statsUpdate = new StatsUpdate();
    statsUpdate.setConfirmedBlockCount(this.fullNode.getBlockTree().getMainBranchLeafHeight());
    // exclude genesis block
    statsUpdate.setVerifiedBlockCount(this.fullNode.getBlockMap().size() - 1);
    statsUpdate.setConfirmedTransactionCount(
        this.fullNode.getChainState().getTransactions().size());
    statsUpdate.setVerifiedTransactionCount(this.fullNode.getMemPool().getVerifiedTxMap().size());
    this.config.getReducer().tell(statsUpdate);
  }

  private void sendNetworkStats() {
    NetworkStatsUpdate update = new NetworkStatsUpdate();
    update.setNodeName(this.nodeName);
    update.setReceivedBlockMessageCount(statsCounter.getReceivedBlockMessages());
    update.setReceivedTxMessageCount(statsCounter.getReceivedTxMessages());
    update.setReceivedRedundantTxMessageCount(statsCounter.getReceivedRedundantTxMessages());
    update.setReceivedReconciliationMessageCount(statsCounter.getReceivedReconciliationMessages());
    this.config.getReducer().tell(update);
  }

  private void initiateStop() {
    if (isStopped) {
      return;
    }
    isStopped = true;
    this.timers.cancel(tryToMineTimerKey);
    this.timers.cancel(tryToSendTxTimerKey);
    this.timers.cancel(sendStatsTimerKey);
    this.sendNetworkStats();
    if (config.isSendStatsToReducer()) {
      sendStats();
    }
    this.context.getSelf().tell(new NotifyFinished());
  }
}
