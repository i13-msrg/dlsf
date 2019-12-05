package dev.salis.dlsf.x.bitcointxprotocols.pod.node;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.TimerScheduler;
import dev.salis.dlsf.bitcoin.data.Tx;
import dev.salis.dlsf.bitcoin.node.exceptions.TxRejectedException;
import dev.salis.dlsf.bitcoin.node.exceptions.TxValidationException;
import dev.salis.dlsf.bitcoin.node.exceptions.TxVerificationException;
import dev.salis.dlsf.x.bitcointxprotocols.pod.node.NodeProtocol.InitiateReconciliation;
import dev.salis.dlsf.x.bitcointxprotocols.pod.node.NodeProtocol.Message;
import dev.salis.dlsf.x.bitcointxprotocols.pod.node.NodeProtocol.PropagateTx;
import dev.salis.dlsf.x.bitcointxprotocols.pod.node.NodeProtocol.ReconciliationRequest;
import dev.salis.dlsf.x.bitcointxprotocols.pod.node.NodeProtocol.ReconciliationResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ErlayNode extends Node {

  private int reconciliationInterval = 1000;

  public static Behavior<Message> createBehavior(NodeConfig config) {
    return Behaviors.withTimers(
        timers -> Behaviors.setup(ctx -> new ErlayNode(timers, ctx, config)));
  }

  private String reconciliationTimerKey = "reconciliationTimerKey";
  private Map<ActorRef<Message>, Set<String>> reconciliationSets = new HashMap<>();

  public ErlayNode(
      TimerScheduler<Message> timers, ActorContext<Message> context, NodeConfig config) {
    super(timers, context, config);
    this.reconciliationInterval = config.getSimulationConfig().getErlayReconciliationInterval();
    if (this.reconciliationInterval < 1) {
      // default is 1 seconds
      this.reconciliationInterval = 1000;
    }
    timers.startSingleTimer(
        reconciliationTimerKey,
        new InitiateReconciliation(),
        Duration.ofMillis(reconciliationInterval));
  }

  @Override
  public Receive<Message> createReceive() {
    return super.createReceiveBuilder()
        .onMessage(
            InitiateReconciliation.class,
            msg -> {
              timers.startSingleTimer(
                  reconciliationTimerKey,
                  new InitiateReconciliation(),
                  Duration.ofMillis(reconciliationInterval));
              if (this.outboundConnections == null || this.outboundConnections.isEmpty()) {
                return this;
              }
              ActorRef<Message> connection =
                  this.getRandomOutboundConnections(this.outboundConnections, 1).stream()
                      .findFirst()
                      .orElseThrow();
              Set<String> reconciliationSet =
                  this.reconciliationSets.getOrDefault(connection, new HashSet<>());
              connection.tell(new ReconciliationRequest(reconciliationSet, this.context.getSelf()));
              return this;
            })
        .onMessage(
            ReconciliationResponse.class,
            msg -> {
              Set<String> set =
                  this.reconciliationSets.getOrDefault(msg.getSender(), new HashSet<>());
              for (Tx tx : msg.getMissingTxSet()) {
                set.remove(tx.getHash());
                try {
                  this.fullNode.receiveTx(tx);
                } catch (TxRejectedException | TxValidationException e) {
                  // discard Tx
                  return this;
                } catch (TxVerificationException e) {
                  // propagate orphan Txs
                }
              }
              this.reconciliationSets.put(msg.getSender(), set);
              return this;
            })
        .onMessage(
            ReconciliationRequest.class,
            msg -> {
              this.statsCounter.receivedReconciliationMessage();
              Set<String> set =
                  this.reconciliationSets.getOrDefault(msg.getSender(), new HashSet<>());
              Set<Tx> missingTxSet =
                  set.stream()
                      .filter(txHash -> !msg.getReconciliationSet().contains(txHash))
                      .map(txHash -> this.fullNode.getMemPool().get(txHash))
                      .filter(tx -> tx != null)
                      .collect(Collectors.toSet());
              msg.getSender()
                  .tell(new ReconciliationResponse(missingTxSet, this.context.getSelf()));
              return this;
            })
        .onMessage(
            PropagateTx.class,
            msg -> {
              if (!this.context.getSelf().equals(msg.getSender())) {
                this.statsCounter.receivedTxMessage();
                if (this.fullNode.getChainState().containsTx(msg.getTx().getHash())
                    || this.fullNode.getMemPool().contains(msg.getTx().getHash())) {
                  this.statsCounter.receivedRedundantTxMessage();
                }
              }
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
              Set<ActorRef<Message>> outboundConnectionsNotSender =
                  new HashSet<>(this.outboundConnections);
              outboundConnectionsNotSender.remove(msg.getSender());
              Set<ActorRef<Message>> randomOutboundConnections =
                  this.getRandomOutboundConnections(outboundConnectionsNotSender, 8);
              this.neighbors.stream()
                  .filter(neighbor -> !msg.getSender().equals(neighbor))
                  .forEach(
                      neighbor -> {
                        if (randomOutboundConnections.contains(neighbor)) {
                          return;
                        }
                        Set<String> set =
                            reconciliationSets.getOrDefault(neighbor, new HashSet<>());
                        set.add(tx.getHash());
                        reconciliationSets.put(neighbor, set);
                      });
              for (ActorRef<Message> connection : randomOutboundConnections) {
                connection.tell(new PropagateTx(msg.getTx(), context.getSelf()));
              }
              return this;
            })
        .build();
  }

  private Set<ActorRef<Message>> getRandomOutboundConnections(
      Set<ActorRef<Message>> set, int count) {
    Set<ActorRef<Message>> connections = new HashSet<>();
    List<ActorRef<Message>> remaining = new ArrayList<>(set);
    // pick 8 outbound connections, as instructed in erlay
    while (!remaining.isEmpty() && connections.size() <= count) {
      int index = ThreadLocalRandom.current().nextInt(remaining.size());
      ActorRef<Message> element = remaining.remove(index);
      connections.add(element);
    }
    return connections;
  }
}
