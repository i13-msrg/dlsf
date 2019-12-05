package dev.salis.dlsf.core.master;

import akka.actor.typed.ActorRef;
import dev.salis.dlsf.core.worker.WorkerProtocol;
import dev.salis.dlsf.core.worker.WorkerState;

public class WorkerInfo {
  private String name;
  private ActorRef<WorkerProtocol.MasterMsg> ref;
  private WorkerState state;

  public WorkerInfo(String name, ActorRef<WorkerProtocol.MasterMsg> ref, WorkerState state) {
    this.name = name;
    this.ref = ref;
    this.state = state;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ActorRef<WorkerProtocol.MasterMsg> getRef() {
    return ref;
  }

  public void setRef(ActorRef<WorkerProtocol.MasterMsg> ref) {
    this.ref = ref;
  }

  public WorkerState getState() {
    return state;
  }

  public void setState(WorkerState state) {
    this.state = state;
  }
}
