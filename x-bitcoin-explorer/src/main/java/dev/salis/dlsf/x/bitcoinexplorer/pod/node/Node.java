package dev.salis.dlsf.x.bitcoinexplorer.pod.node;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.TimerScheduler;
import dev.salis.dlsf.bitcoin.data.Block;
import dev.salis.dlsf.bitcoin.data.Tx;
import dev.salis.dlsf.bitcoin.data.TxIn;
import dev.salis.dlsf.bitcoin.data.TxOut;
import dev.salis.dlsf.bitcoin.network.BitcoinNetworkCoordinatorProtocol;
import dev.salis.dlsf.bitcoin.node.FullNode;
import dev.salis.dlsf.bitcoin.node.exceptions.TxRejectedException;
import dev.salis.dlsf.bitcoin.node.exceptions.TxValidationException;
import dev.salis.dlsf.bitcoin.node.exceptions.TxVerificationException;
import dev.salis.dlsf.bitcoin.node.mempool.TxWithFee;
import dev.salis.dlsf.bitcoin.node.wallet.Wallet;
import dev.salis.dlsf.x.bitcoinexplorer.pod.PodProtocol.NodeFinished;
import dev.salis.dlsf.x.bitcoinexplorer.pod.node.NodeProtocol.NotifyFinished;
import dev.salis.dlsf.x.bitcoinexplorer.pod.node.NodeProtocol.SendStats;
import dev.salis.dlsf.x.bitcoinexplorer.reducer.ReducerProtocol.BlocksUpdate;
import dev.salis.dlsf.x.bitcoinexplorer.reducer.ReducerProtocol.StatsUpdate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Node extends AbstractBehavior<NodeProtocol.Message> {

  private final String nodeName;
  private NodeConfig config;
  private Set<ActorRef<NodeProtocol.Message>> neighbors;
  private ActorContext<NodeProtocol.Message> context;
  private TimerScheduler<NodeProtocol.Message> timers;
  private String tryToMineTimerKey = "tryToMine";
  private String tryToSendTxTimerKey = "tryToSendTx";
  private String sendStatsTimerKey = "sendStats";

  private FullNode fullNode;
  private boolean isStopped = false;

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

  public static Behavior<NodeProtocol.Message> createBehavior(NodeConfig config) {
    return Behaviors.withTimers(timers -> Behaviors.setup(ctx -> new Node(timers, ctx, config)));
  }

  @Override
  public Receive<NodeProtocol.Message> createReceive() {
    return newReceiveBuilder()
        .onMessage(
            NodeProtocol.ListingMsg.class,
            msg -> {
              if (this.neighbors == null && !msg.outboundConnections.isEmpty()) {
                this.neighbors =
                    new HashSet<>(msg.outboundConnections.size() + msg.inboundConnections.size());
                this.neighbors.addAll(msg.outboundConnections);
                this.neighbors.addAll(msg.inboundConnections);
                this.timers.startSingleTimer(
                    tryToMineTimerKey,
                    new NodeProtocol.MineBlock(),
                    Duration.ofMillis(
                        ThreadLocalRandom.current()
                            .nextLong(
                                0, config.getSimulationConfig().getMineAttemptIntervalInMillis())));
                if (config.getSimulationConfig().getCreateTxIntervalInMillis() > 0) {
                  this.timers.startSingleTimer(
                      tryToSendTxTimerKey,
                      new NodeProtocol.CreateTx(),
                      Duration.ofMillis(
                          ThreadLocalRandom.current()
                              .nextLong(
                                  0, config.getSimulationConfig().getCreateTxIntervalInMillis())));
                }
                if (this.config.isSendStatsToReducer()) {
                  this.context.getSelf().tell(new SendStats());
                }
              }
              return this;
            })
        .onMessage(
            NodeProtocol.PropagateTx.class,
            msg -> {
              if (this.isStopped) {
                return this;
              }
              Tx tx = msg.getTx();
              try {
                this.fullNode.receiveTx(tx);
              } catch (TxRejectedException | TxValidationException e) {
                // discard Tx
                return this;
              } catch (TxVerificationException e) {
                // propagate orphan Txs
              }
              if (this.neighbors == null) {
                return this;
              }
              for (ActorRef<NodeProtocol.Message> neighbor : this.neighbors) {
                if (neighbor.equals(msg.getSender())) {
                  continue;
                }
                neighbor.tell(new NodeProtocol.PropagateTx(msg.getTx(), context.getSelf()));
              }
              return this;
            })
        .onMessage(
            NodeProtocol.PropagateBlock.class,
            msg -> {
              Block block = msg.getBlock();
              boolean shouldPropagate = this.fullNode.receiveBlock(block);
              if (!shouldPropagate) {
                return this;
              }
              if (this.neighbors == null) {
                return this;
              }
              for (ActorRef<NodeProtocol.Message> neighbor : this.neighbors) {
                if (neighbor.equals(msg.getSender())) {
                  continue;
                }
                neighbor.tell(new NodeProtocol.PropagateBlock(msg.getBlock(), context.getSelf()));
              }
              if (this.fullNode.getBlockTree().getMainBranchLeafHeight()
                  >= config.getSimulationConfig().getMaxBlockHeight()) {
                this.initiateStop();
                return this;
              }
              return this;
            })
        .onMessage(
            NodeProtocol.SendStats.class,
            msg -> {
              this.timers.startSingleTimer(
                  sendStatsTimerKey,
                  new NodeProtocol.SendStats(),
                  Duration.ofMillis(config.getSimulationConfig().getStatsUpdateIntervalInMillis()));
              this.sendStatsUpdate();
              return this;
            })
        .onMessage(
            NodeProtocol.MineBlock.class,
            msg -> {
              this.timers.startSingleTimer(
                  tryToMineTimerKey,
                  new NodeProtocol.MineBlock(),
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
              context.getSelf().tell(new NodeProtocol.PropagateBlock(block, context.getSelf()));
              return this;
            })
        .onMessage(
            NodeProtocol.CreateTx.class,
            msg -> {
              this.timers.startSingleTimer(
                  tryToSendTxTimerKey,
                  new NodeProtocol.CreateTx(),
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
              context.getSelf().tell(new NodeProtocol.PropagateTx(tx, context.getSelf()));
              return this;
            })
        .onMessage(
            NotifyFinished.class,
            msg -> {
              config.getPod().tell(new NodeFinished(nodeName));
              return this;
            })
        .build();
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

  private void sendStatsUpdate() {
    StatsUpdate statsUpdate = new StatsUpdate();
    statsUpdate.setConfirmedBlockCount(this.fullNode.getBlockTree().getMainBranchLeafHeight());
    // exclude genesis block
    statsUpdate.setVerifiedBlockCount(this.fullNode.getBlockMap().size() - 1);
    statsUpdate.setConfirmedTransactionCount(
        this.fullNode.getChainState().getTransactions().size());
    statsUpdate.setVerifiedTransactionCount(this.fullNode.getMemPool().getVerifiedTxMap().size());
    this.config.getReducer().tell(statsUpdate);
  }

  private void sendBlockStatsUpdate() {
    BlocksUpdate blocksUpdate = new BlocksUpdate();
    blocksUpdate.setBlockMap(this.fullNode.getBlockMap());
    blocksUpdate.setMainChainBlockHashList(this.fullNode.getBlockTree().getMainBranch());
    this.config.getReducer().tell(blocksUpdate);
  }

  private void initiateStop() {
    if (isStopped) {
      return;
    }
    isStopped = true;
    this.timers.cancelAll();
    if (config.isSendStatsToReducer()) {
      sendStatsUpdate();
      sendBlockStatsUpdate();
    }
    this.context.getSelf().tell(new NotifyFinished());
  }
}
