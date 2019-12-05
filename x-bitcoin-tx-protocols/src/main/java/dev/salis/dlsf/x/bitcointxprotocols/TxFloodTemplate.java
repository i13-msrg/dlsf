package dev.salis.dlsf.x.bitcointxprotocols;

import akka.actor.typed.Behavior;
import dev.salis.dlsf.bitcoin.network.BitcoinNetworkCoordinator;
import dev.salis.dlsf.bitcoin.network.BitcoinNetworkCoordinatorConfig;
import dev.salis.dlsf.core.master.ServiceTemplate;
import dev.salis.dlsf.core.pod.AbstractPodProtocol;
import dev.salis.dlsf.core.pod.PodContext;
import dev.salis.dlsf.core.reducer.BaseReducerProtocol;
import dev.salis.dlsf.core.run.RunContext;
import dev.salis.dlsf.core.template.AbstractSimulationTemplate;
import dev.salis.dlsf.x.bitcointxprotocols.pod.Pod;
import dev.salis.dlsf.x.bitcointxprotocols.pod.node.FloodNode;
import dev.salis.dlsf.x.bitcointxprotocols.reducer.Reducer;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

public class TxFloodTemplate extends AbstractSimulationTemplate {

  @Override
  public String getName() {
    return "bitcoin-tx-flood";
  }

  @Override
  public Function<PodContext, Behavior<AbstractPodProtocol.Message>> getPodFactory() {
    return simulationContext -> {
      return Pod.createBehavior(simulationContext, FloodNode::createBehavior);
    };
  }

  @Override
  public Function<RunContext, Behavior<? extends BaseReducerProtocol.Message>> getReducerFactory() {
    return Reducer::createBehavior;
  }

  @Override
  public Collection<ServiceTemplate> getServiceTemplates() {
    ServiceTemplate bitcoinNetworkService = new ServiceTemplate();
    bitcoinNetworkService.setServiceKey(BitcoinNetworkCoordinator.SERVICE_KEY);
    bitcoinNetworkService.setFactory(
        runContext -> {
          SimulationConfig config = runContext.getSimulationConfig(SimulationConfig.class);
          BitcoinNetworkCoordinatorConfig coordinatorConfig = new BitcoinNetworkCoordinatorConfig();
          coordinatorConfig.setTotalNumOfNodes(
              runContext.getWorkerNames().size() * config.getNumOfNodesPerPod());
          coordinatorConfig.setMaxInboundConnections(config.getMaxInboundConnections());
          coordinatorConfig.setMaxOutboundConnections(config.getMaxOutboundConnections());
          if (config.getNetworkTopologySeed() != 0L) {
            coordinatorConfig.setSeed(config.getNetworkTopologySeed());
          }
          return BitcoinNetworkCoordinator.createBehavior(coordinatorConfig);
        });
    return Collections.singletonList(bitcoinNetworkService);
  }

  @Override
  public Class<?> getConfigClass() {
    return SimulationConfig.class;
  }
}
