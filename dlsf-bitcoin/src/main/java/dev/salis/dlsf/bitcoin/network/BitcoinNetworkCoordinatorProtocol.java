package dev.salis.dlsf.bitcoin.network;

import akka.actor.typed.ActorRef;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BitcoinNetworkCoordinatorProtocol {

  public interface Message extends Serializable {

  }

  /**
   * Register a simulated node to the coordinator.
   */
  public static final class Register implements Message {

    private ActorRef<Listing> listener;
    private ActorRef node;

    /**
     * @param listener actor to be notified about its neighbors via a Listing message.
     * @param node actor to be registered as a network node.
     */
    public Register(ActorRef<Listing> listener, ActorRef node) {
      this.listener = listener;
      this.node = node;
    }

    public Register() {}

    public ActorRef<Listing> getListener() {
      return listener;
    }

    public void setListener(ActorRef<Listing> listener) {
      this.listener = listener;
    }

    public ActorRef getNode() {
      return node;
    }

    public void setNode(ActorRef node) {
      this.node = node;
    }
  }

  /** Message that contains references to the neighbor actors. */
  public static final class Listing implements Message {

    private Set outboundConnections;
    private Set inboundConnections;

    public Listing(Set<ActorRef> outboundConnections, Set<ActorRef> inboundConnections) {
      this.outboundConnections = outboundConnections;
      this.inboundConnections = inboundConnections;
    }

    public Listing() {}

    public Set getOutboundConnections() {
      return outboundConnections;
    }

    public <T> Set<ActorRef<T>> getOutboundConnections(Class<T> protocol) {
      return outboundConnections;
    }

    public void setOutboundConnections(Set outboundConnections) {
      this.outboundConnections = outboundConnections;
    }

    public Set getInboundConnections() {
      return inboundConnections;
    }

    public <T> Set<ActorRef<T>> getInboundConnections(Class<T> protocol) {
      return inboundConnections;
    }

    public void setInboundConnections(Set inboundConnections) {
      this.inboundConnections = inboundConnections;
    }
  }

  /** Message to request current network topology. */
  public static final class NetworkTopologyRequest implements Message {

    private ActorRef<NetworkTopologyResponse> replyTo;

    public ActorRef<NetworkTopologyResponse> getReplyTo() {
      return replyTo;
    }

    public void setReplyTo(ActorRef<NetworkTopologyResponse> replyTo) {
      this.replyTo = replyTo;
    }
  }

  /**
   * Response message to NetworkTopologyRequest that includes both outbound connections of each node
   * and number of connections of each node.
   */
  public static final class NetworkTopologyResponse implements Message {

    private Map<String, List<String>> outboundConnections;
    private Map<String, Integer> totalNumOfConnections;

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
