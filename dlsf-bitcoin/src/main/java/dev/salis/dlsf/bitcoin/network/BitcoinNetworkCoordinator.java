package dev.salis.dlsf.bitcoin.network;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.TimerScheduler;
import akka.actor.typed.receptionist.ServiceKey;
import dev.salis.dlsf.bitcoin.network.BitcoinNetworkCoordinatorProtocol.Listing;
import dev.salis.dlsf.bitcoin.network.BitcoinNetworkCoordinatorProtocol.Message;
import dev.salis.dlsf.bitcoin.network.BitcoinNetworkCoordinatorProtocol.NetworkTopologyRequest;
import dev.salis.dlsf.bitcoin.network.BitcoinNetworkCoordinatorProtocol.NetworkTopologyResponse;
import dev.salis.dlsf.bitcoin.network.BitcoinNetworkCoordinatorProtocol.Register;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Actor that can be used in Bitcoin simulations to generate network topologies. This actor provides
 * registered simulated nodes their neighbors. It also provides created topologies as a data
 * structure. Usage examples can be seen in example simulations.
 */
public class BitcoinNetworkCoordinator extends AbstractBehavior<Message> {

  public static final ServiceKey<BitcoinNetworkCoordinatorProtocol.Message> SERVICE_KEY =
      ServiceKey.create(
          BitcoinNetworkCoordinatorProtocol.Message.class, "bitcoin-network-coordinator");

  private Random random = new Random();
  private TimerScheduler<Message> scheduler;
  private ActorContext<Message> context;
  private BitcoinNetworkCoordinatorConfig config;
  private boolean areConnectionsSet = false;
  private Map<String, Client> clientsMap = new HashMap<>();
  private List<ActorRef<NetworkTopologyResponse>> topologyRequesterList = new ArrayList<>();

  public BitcoinNetworkCoordinator(
      TimerScheduler<Message> scheduler,
      ActorContext<Message> context,
      BitcoinNetworkCoordinatorConfig config) {
    this.scheduler = scheduler;
    this.context = context;
    this.config = config;
    if (config.getSeed() != null) {
      this.random.setSeed(config.getSeed());
    }
  }

  public static Behavior<Message> createBehavior(BitcoinNetworkCoordinatorConfig params) {
    return Behaviors.withTimers(
        scheduler ->
            Behaviors.setup(context -> new BitcoinNetworkCoordinator(scheduler, context, params)));
  }

  @Override
  public Receive<Message> createReceive() {
    return newReceiveBuilder()
        .onMessage(
            Register.class,
            msg -> {
              if (this.areConnectionsSet) {
                // ignored
                return this;
              }
              final var client = new Client(msg.getListener(), msg.getNode());
              clientsMap.put(client.getId(), client);
              if (clientsMap.size() == this.config.getTotalNumOfNodes()) {
                this.sendConnectionInformation();
                this.areConnectionsSet = true;
              }
              return this;
            })
        .onMessage(
            NetworkTopologyRequest.class,
            msg -> {
              if (!this.areConnectionsSet) {
                this.topologyRequesterList.add(msg.getReplyTo());
                return this;
              }
              NetworkTopologyResponse networkTopologyResponse = new NetworkTopologyResponse();
              networkTopologyResponse.setOutboundConnections(generateOutboundConnectionsMap());
              networkTopologyResponse.setTotalNumOfConnections(generateTotalNumOfConnectionsMap());
              msg.getReplyTo().tell(networkTopologyResponse);
              return this;
            })
        .build();
  }

  private void sendConnectionInformation() {
    // create connections
    List<String> inGraph = new ArrayList<>(clientsMap.size());
    List<String> outGraph = new ArrayList<>(clientsMap.keySet());

    while (inGraph.size() <= this.config.getMaxOutboundConnections() && !outGraph.isEmpty()) {
      inGraph.add(getRandomValue(outGraph, true));
    }
    List<Client> inClients = inGraph.stream().map(clientsMap::get).collect(Collectors.toList());
    for (Client inClient : inClients) {
      for (Client other : inClients) {
        if (inClient.getId().equals(other.getId())) {
          continue;
        }
        inClient.getOutboundConnections().add(other);
        other.getInboundConnections().add(inClient);
        if (other.getInboundConnections().size() >= this.config.getMaxInboundConnections()) {
          inGraph.remove(other.getId());
        }
      }
    }

    while (outGraph.size() > 0) {
      var out = clientsMap.get(getRandomValue(outGraph, true));
      while (out.getOutboundConnections().size() < this.config.getMaxOutboundConnections()) {
        var in = clientsMap.get(getRandomValue(inGraph, false));
        if (in.getInboundConnections().size() >= this.config.getMaxInboundConnections()) {
          inGraph.remove(in.getId());
          continue;
        }
        out.getOutboundConnections().add(in);
        in.getInboundConnections().add(out);
      }
      inGraph.add(out.getId());
    }
    // send updates
    for (Client c : clientsMap.values()) {
      Set<akka.actor.typed.ActorRef> outboundNodes =
          c.getOutboundConnections().stream().map(Client::getNode).collect(Collectors.toSet());
      Set<akka.actor.typed.ActorRef> inboundNodes =
          c.getInboundConnections().stream().map(Client::getNode).collect(Collectors.toSet());
      c.getListener().tell(new Listing(outboundNodes, inboundNodes));
    }
    // send topology to requesters
    Map<String, List<String>> outboundConnectionsMap = generateOutboundConnectionsMap();
    Map<String, Integer> totalNumOfConnectionsMap = generateTotalNumOfConnectionsMap();
    for (ActorRef<NetworkTopologyResponse> ref : this.topologyRequesterList) {
      NetworkTopologyResponse networkTopologyResponse = new NetworkTopologyResponse();
      networkTopologyResponse.setOutboundConnections(outboundConnectionsMap);
      networkTopologyResponse.setTotalNumOfConnections(totalNumOfConnectionsMap);
      ref.tell(networkTopologyResponse);
    }
    this.topologyRequesterList.clear();
  }

  private <T> T getRandomValue(List<T> collection, boolean remove) {
    int size = collection.size();
    final var randIndex = random.nextInt(size);
    if (remove) {
      return collection.remove(randIndex);
    }
    return collection.get(randIndex);
  }

  private Map<String, List<String>> generateOutboundConnectionsMap() {
    Map<String, List<String>> outboundConnections = new HashMap<>();
    for (Client client : this.clientsMap.values()) {
      List<String> neighbors =
          client.getOutboundConnections().stream().map(Client::getId).collect(Collectors.toList());
      outboundConnections.put(client.getId(), neighbors);
    }
    return outboundConnections;
  }

  private Map<String, Integer> generateTotalNumOfConnectionsMap() {
    Map<String, Integer> totalNumOfConnectionsMap = new HashMap<>();
    for (Client client : this.clientsMap.values()) {
      Set<String> connections = new HashSet<>();
      connections.addAll(
          client.getOutboundConnections().stream().map(Client::getId).collect(Collectors.toList()));
      connections.addAll(
          client.getInboundConnections().stream().map(Client::getId).collect(Collectors.toList()));
      totalNumOfConnectionsMap.put(client.getId(), connections.size());
    }
    return totalNumOfConnectionsMap;
  }
}
