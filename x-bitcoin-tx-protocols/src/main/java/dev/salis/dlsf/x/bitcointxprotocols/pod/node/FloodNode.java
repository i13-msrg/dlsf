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
import dev.salis.dlsf.x.bitcointxprotocols.pod.node.NodeProtocol.Message;
import dev.salis.dlsf.x.bitcointxprotocols.pod.node.NodeProtocol.PropagateTx;

public class FloodNode extends Node {

  public static Behavior<Message> createBehavior(NodeConfig config) {
    return Behaviors.withTimers(
        timers -> Behaviors.setup(ctx -> new FloodNode(timers, ctx, config)));
  }

  public FloodNode(
      TimerScheduler<Message> timers, ActorContext<Message> context, NodeConfig config) {
    super(timers, context, config);
  }

  @Override
  public Receive<Message> createReceive() {
    return super.createReceiveBuilder()
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
              for (ActorRef<Message> neighbor : this.neighbors) {
                if (neighbor.equals(msg.getSender())) {
                  continue;
                }
                neighbor.tell(new PropagateTx(msg.getTx(), context.getSelf()));
              }
              return this;
            })
        .build();
  }
}
