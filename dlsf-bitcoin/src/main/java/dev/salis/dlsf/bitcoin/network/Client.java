package dev.salis.dlsf.bitcoin.network;

import akka.actor.typed.ActorRef;
import dev.salis.dlsf.bitcoin.network.BitcoinNetworkCoordinatorProtocol.Listing;
import java.util.HashSet;
import java.util.Set;

class Client {

  private String id;
  private ActorRef<Listing> listener;
  private ActorRef node;
  private Set<Client> inboundConnections = new HashSet<>();
  private Set<Client> outboundConnections = new HashSet<>();

  public Client(ActorRef<Listing> listener, ActorRef node) {
    this.id = generateId(listener);
    this.listener = listener;
    this.node = node;
  }

  static String generateId(ActorRef<?> listener) {
    return listener.path().toSerializationFormat();
  }

  public String getId() {
    return id;
  }

  public ActorRef<Listing> getListener() {
    return listener;
  }

  public void setListener(ActorRef<Listing> listener) {
    this.listener = listener;
  }

  public ActorRef getNode() {
    return node;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setNode(ActorRef node) {
    this.node = node;
  }

  public Set<Client> getInboundConnections() {
    return inboundConnections;
  }

  public void setInboundConnections(Set<Client> inboundConnections) {
    this.inboundConnections = inboundConnections;
  }

  public Set<Client> getOutboundConnections() {
    return outboundConnections;
  }

  public void setOutboundConnections(Set<Client> outboundConnections) {
    this.outboundConnections = outboundConnections;
  }
}
