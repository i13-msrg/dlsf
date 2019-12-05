package dev.salis.dlsf.x.bitcoinexplorer.pod.node;

import akka.actor.typed.ActorRef;
import dev.salis.dlsf.bitcoin.network.BitcoinNetworkCoordinatorProtocol;
import dev.salis.dlsf.core.pod.AbstractPodProtocol.Message;
import dev.salis.dlsf.x.bitcoinexplorer.SimulationConfig;
import dev.salis.dlsf.x.bitcoinexplorer.reducer.ReducerProtocol;
import java.util.List;

public class NodeConfig {

  private ActorRef<Message> pod;
  private ActorRef<BitcoinNetworkCoordinatorProtocol.Message> networkCoordinator;
  private ActorRef<ReducerProtocol.Message> reducer;
  private int nodeIndex;
  private boolean sendStatsToReducer;
  private String workerName;
  private List<String> workerNames;
  private SimulationConfig simulationConfig;

  public NodeConfig() {
  }

  public ActorRef<Message> getPod() {
    return pod;
  }

  public void setPod(ActorRef<Message> pod) {
    this.pod = pod;
  }

  public ActorRef<ReducerProtocol.Message> getReducer() {
    return reducer;
  }

  public void setReducer(ActorRef<ReducerProtocol.Message> reducer) {
    this.reducer = reducer;
  }

  public boolean isSendStatsToReducer() {
    return sendStatsToReducer;
  }

  public void setSendStatsToReducer(boolean sendStatsToReducer) {
    this.sendStatsToReducer = sendStatsToReducer;
  }

  public ActorRef<BitcoinNetworkCoordinatorProtocol.Message> getNetworkCoordinator() {
    return networkCoordinator;
  }

  public void setNetworkCoordinator(
      ActorRef<BitcoinNetworkCoordinatorProtocol.Message> networkCoordinator) {
    this.networkCoordinator = networkCoordinator;
  }

  public int getNodeIndex() {
    return nodeIndex;
  }

  public void setNodeIndex(int nodeIndex) {
    this.nodeIndex = nodeIndex;
  }

  public String getWorkerName() {
    return workerName;
  }

  public void setWorkerName(String workerName) {
    this.workerName = workerName;
  }

  public List<String> getWorkerNames() {
    return workerNames;
  }

  public void setWorkerNames(List<String> workerNames) {
    this.workerNames = workerNames;
  }

  public SimulationConfig getSimulationConfig() {
    return simulationConfig;
  }

  public void setSimulationConfig(SimulationConfig simulationConfig) {
    this.simulationConfig = simulationConfig;
  }
}
