package dev.salis.dlsf.x.bitcoinexplorer.pod.node;

import akka.actor.typed.ActorRef;
import dev.salis.dlsf.bitcoin.data.Block;
import dev.salis.dlsf.bitcoin.data.Tx;
import java.io.Serializable;
import java.util.Set;

public class NodeProtocol {

  public interface Message extends Serializable {

  }

  public static class PropagateBlock implements Message {

    private Block block;
    private ActorRef<Message> sender;

    public PropagateBlock(Block block, ActorRef<Message> sender) {
      this.block = block;
      this.sender = sender;
    }

    public PropagateBlock() {
    }

    public Block getBlock() {
      return block;
    }

    public void setBlock(Block block) {
      this.block = block;
    }

    public ActorRef<Message> getSender() {
      return sender;
    }

    public void setSender(ActorRef<Message> sender) {
      this.sender = sender;
    }
  }

  public static class PropagateTx implements Message {

    private Tx tx;
    private ActorRef<Message> sender;

    public PropagateTx(Tx tx, ActorRef<Message> sender) {
      this.tx = tx;
      this.sender = sender;
    }

    public PropagateTx() {
    }

    public Tx getTx() {
      return tx;
    }

    public void setTx(Tx tx) {
      this.tx = tx;
    }

    public ActorRef<Message> getSender() {
      return sender;
    }

    public void setSender(ActorRef<Message> sender) {
      this.sender = sender;
    }
  }

  protected interface InternalMsg extends Message {

  }

  protected static class MineBlock implements InternalMsg {

    public MineBlock() {
    }
  }

  protected static class CreateTx implements InternalMsg {

    public CreateTx() {
    }
  }

  protected static class SendStats implements InternalMsg {

    public SendStats() {
    }
  }

  protected static class ListingMsg implements InternalMsg {

    Set<ActorRef<Message>> outboundConnections;
    Set<ActorRef<Message>> inboundConnections;

    public ListingMsg(
        Set<ActorRef<Message>> outboundConnections, Set<ActorRef<Message>> inboundConnections) {
      this.outboundConnections = outboundConnections;
      this.inboundConnections = inboundConnections;
    }
  }

  public static class NotifyFinished implements InternalMsg {

    public NotifyFinished() {
    }
  }
}
