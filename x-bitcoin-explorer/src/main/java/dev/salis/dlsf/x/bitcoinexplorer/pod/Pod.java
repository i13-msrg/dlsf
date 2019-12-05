package dev.salis.dlsf.x.bitcoinexplorer.pod;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.Receptionist;
import dev.salis.dlsf.bitcoin.network.BitcoinNetworkCoordinator;
import dev.salis.dlsf.core.pod.PodContext;
import dev.salis.dlsf.core.worker.WorkerProtocol;
import dev.salis.dlsf.core.worker.WorkerProtocol.Finished;
import dev.salis.dlsf.x.bitcoinexplorer.SimulationConfig;
import dev.salis.dlsf.x.bitcoinexplorer.pod.PodProtocol.ListingMsg;
import dev.salis.dlsf.x.bitcoinexplorer.pod.PodProtocol.NodeFinished;
import dev.salis.dlsf.x.bitcoinexplorer.pod.PodProtocol.Prepare;
import dev.salis.dlsf.x.bitcoinexplorer.pod.node.Node;
import dev.salis.dlsf.x.bitcoinexplorer.pod.node.NodeConfig;
import dev.salis.dlsf.x.bitcoinexplorer.pod.node.NodeProtocol;
import java.util.HashSet;
import java.util.Set;

public class Pod {

  private static int numOfNodes;

  public static Behavior<PodProtocol.Message> createBehavior(PodContext podContext) {
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
                      nodeConfig.setPod(context.getSelf());
                      nodeConfig.setSimulationConfig(simulationConfig);
                      nodeConfig.setWorkerName(podContext.getWorkerName());
                      nodeConfig.setReducer(podContext.getRunContext().getReducer().unsafeUpcast());
                      nodeConfig.setWorkerNames(podContext.getRunContext().getWorkerNames());
                      nodeConfig.setSendStatsToReducer(
                          i == 0
                              && podContext
                              .getRunContext()
                              .getWorkerNames()
                              .indexOf(podContext.getWorkerName())
                              == 0);
                      ActorRef<NodeProtocol.Message> actorRef =
                          ctx.spawn(
                              Node.createBehavior(nodeConfig),
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
