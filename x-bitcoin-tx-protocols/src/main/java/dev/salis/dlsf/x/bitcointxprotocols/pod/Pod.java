package dev.salis.dlsf.x.bitcointxprotocols.pod;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.Receptionist;
import dev.salis.dlsf.bitcoin.network.BitcoinNetworkCoordinator;
import dev.salis.dlsf.core.pod.PodContext;
import dev.salis.dlsf.core.worker.WorkerProtocol;
import dev.salis.dlsf.core.worker.WorkerProtocol.Finished;
import dev.salis.dlsf.x.bitcointxprotocols.SimulationConfig;
import dev.salis.dlsf.x.bitcointxprotocols.pod.PodProtocol.ListingMsg;
import dev.salis.dlsf.x.bitcointxprotocols.pod.PodProtocol.NodeFinished;
import dev.salis.dlsf.x.bitcointxprotocols.pod.PodProtocol.Prepare;
import dev.salis.dlsf.x.bitcointxprotocols.pod.node.NodeConfig;
import dev.salis.dlsf.x.bitcointxprotocols.pod.node.NodeProtocol;
import dev.salis.dlsf.x.bitcointxprotocols.pod.node.NodeProtocol.Message;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class Pod {

  private static int numOfNodes;

  public static Behavior<PodProtocol.Message> createBehavior(
      PodContext podContext, Function<NodeConfig, Behavior<Message>> nodeFactoryFn) {
    return Behaviors.setup(
        ctx -> {
          // subscribe to master service listing messages
          final ActorRef<Receptionist.Listing> listingMsgAdapter =
              ctx.messageAdapter(Receptionist.Listing.class, PodProtocol.ListingMsg::new);
          ctx.getSystem()
              .receptionist()
              .tell(
                  Receptionist.subscribe(BitcoinNetworkCoordinator.SERVICE_KEY, listingMsgAdapter));

          final SimulationConfig simulationConfig =
              podContext.getRunContext().getSimulationConfig(SimulationConfig.class);
          numOfNodes = simulationConfig.getNumOfNodesPerPod();
          Set<ActorRef<NodeProtocol.Message>> nodeRefs = new HashSet<>(numOfNodes);
          Set<String> stoppedNodes = new HashSet<>(numOfNodes);
          return Behaviors.receive(PodProtocol.Message.class)
              .onMessage(
                  ListingMsg.class,
                  (context, msg) -> {
                    final var coordinatorOptional =
                        msg.getListing().getServiceInstances(BitcoinNetworkCoordinator.SERVICE_KEY)
                            .stream()
                            .findFirst();
                    if (coordinatorOptional.isPresent() && nodeRefs.isEmpty()) {
                      context.getSelf().tell(new Prepare(coordinatorOptional.get()));
                    }
                    return Behaviors.same();
                  })
              .onMessage(
                  Prepare.class,
                  (context, msg) -> {
                    for (int i = 0; i < numOfNodes; i++) {
                      NodeConfig nodeConfig = new NodeConfig();
                      nodeConfig.setNetworkCoordinator(msg.getCoordinator());
                      nodeConfig.setNodeIndex(i);
                      nodeConfig.setSendStatsToReducer(i == 0);
                      nodeConfig.setPod(context.getSelf());
                      nodeConfig.setSimulationConfig(simulationConfig);
                      nodeConfig.setWorkerName(podContext.getWorkerName());
                      nodeConfig.setReducer(podContext.getRunContext().getReducer().unsafeUpcast());
                      nodeConfig.setWorkerNames(podContext.getRunContext().getWorkerNames());
                      ActorRef<NodeProtocol.Message> actorRef =
                          ctx.spawn(
                              nodeFactoryFn.apply(nodeConfig),
                              podContext.getWorkerName() + ":" + i);
                      nodeRefs.add(actorRef);
                    }
                    podContext.getWorker().tell(new WorkerProtocol.Ready());
                    return Behavior.same();
                  })
              .onMessage(
                  PodProtocol.Start.class,
                  (context, msg) -> {
                    return Behaviors.same();
                  })
              .onMessage(
                  NodeFinished.class,
                  (context, msg) -> {
                    stoppedNodes.add(msg.getNodeName());
                    if (stoppedNodes.size() == numOfNodes) {
                      podContext.getWorker().tell(new Finished());
                    }
                    return Behaviors.same();
                  })
              .build();
        });
  }
}
