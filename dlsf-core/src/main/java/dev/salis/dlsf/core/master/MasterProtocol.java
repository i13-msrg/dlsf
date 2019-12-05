package dev.salis.dlsf.core.master;

import akka.actor.typed.ActorRef;
import dev.salis.dlsf.core.worker.WorkerProtocol;
import dev.salis.dlsf.core.worker.WorkerProtocol.MasterMsg;
import dev.salis.dlsf.core.worker.WorkerState;
import java.io.Serializable;

public abstract class MasterProtocol {
  private MasterProtocol() {}

  public interface Message extends Serializable {

  }

  public interface WorkerMsg extends Message {

  }

  public static final class RegisterWorker implements WorkerMsg {
    private String name;
    private ActorRef<WorkerProtocol.MasterMsg> ref;

    public RegisterWorker(String name, ActorRef<WorkerProtocol.MasterMsg> ref) {
      this.name = name;
      this.ref = ref;
    }

    public RegisterWorker() {
    }

    public String getName() {
      return name;
    }

    public ActorRef<WorkerProtocol.MasterMsg> getRef() {
      return ref;
    }

    public void setRef(ActorRef<MasterMsg> ref) {
      this.ref = ref;
    }
  }

  public static final class UpdateWorkerState implements WorkerMsg {

    private String workerName;
    private WorkerState workerState;

    public UpdateWorkerState(String workerName, WorkerState workerState) {
      this.workerName = workerName;
      this.workerState = workerState;
    }

    public UpdateWorkerState() {
    }

    public String getWorkerName() {
      return workerName;
    }

    public void setWorkerName(String workerName) {
      this.workerName = workerName;
    }

    public WorkerState getWorkerState() {
      return workerState;
    }

    public void setWorkerState(WorkerState workerState) {
      this.workerState = workerState;
    }
  }

  public interface SimulationRunMsg extends Message {

  }

  public static class SimulationRunUpdate implements SimulationRunMsg {

    private String type;
    private Object value;

    public SimulationRunUpdate(String type, Object value) {
      this.type = type;
      this.value = value;
    }

    public SimulationRunUpdate() {
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public Object getValue() {
      return value;
    }

    public void setValue(Object value) {
      this.value = value;
    }
  }

  public static class SimulationRunResult implements SimulationRunMsg {

    private Object value;

    public SimulationRunResult() {
    }

    public SimulationRunResult(Object value) {
      this.value = value;
    }

    public Object getValue() {
      return value;
    }

    public void setValue(Object value) {
      this.value = value;
    }
  }
}
