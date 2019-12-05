package dev.salis.dlsf.core.run;

import akka.actor.typed.ActorRef;
import dev.salis.dlsf.core.master.MasterProtocol.SimulationRunUpdate;
import dev.salis.dlsf.core.reducer.BaseReducerProtocol;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Context for all simulation runs to keep relevant information and actors about them.
 */
public class RunContext implements Serializable {

  private String simulationName;
  private List<String> workerNames;
  private Object simulationConfig;
  private String id;
  private Date startDate;
  private Date endDate;
  private ActorRef<? extends BaseReducerProtocol.Message> reducer;
  private ActorRef<SimulationRunUpdate> updateGateway;

  public String getSimulationName() {
    return simulationName;
  }

  public void setSimulationName(String simulationName) {
    this.simulationName = simulationName;
  }

  public List<String> getWorkerNames() {
    return workerNames;
  }

  public void setWorkerNames(List<String> workerNames) {
    this.workerNames = workerNames;
  }

  public Object getSimulationConfig() {
    return simulationConfig;
  }

  public <T> T getSimulationConfig(Class<T> cls) {
    return (T) simulationConfig;
  }

  public void setSimulationConfig(Object simulationConfig) {
    this.simulationConfig = simulationConfig;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public ActorRef<? extends BaseReducerProtocol.Message> getReducer() {
    return reducer;
  }

  public void setReducer(ActorRef<? extends BaseReducerProtocol.Message> reducer) {
    this.reducer = reducer;
  }

  public ActorRef<SimulationRunUpdate> getUpdateGateway() {
    return updateGateway;
  }

  public void setUpdateGateway(ActorRef<SimulationRunUpdate> updateGateway) {
    this.updateGateway = updateGateway;
  }
}
