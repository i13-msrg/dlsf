package dev.salis.dlsf.core.pod;

import akka.actor.typed.ActorRef;
import dev.salis.dlsf.core.run.RunContext;
import dev.salis.dlsf.core.worker.WorkerProtocol.PodMessage;

public class PodContext {

  private String workerName;
  private ActorRef<PodMessage> worker;
  private RunContext runContext;

  public PodContext() {
  }

  public String getWorkerName() {
    return workerName;
  }

  public void setWorkerName(String workerName) {
    this.workerName = workerName;
  }

  public ActorRef<PodMessage> getWorker() {
    return worker;
  }

  public void setWorker(ActorRef<PodMessage> worker) {
    this.worker = worker;
  }

  public RunContext getRunContext() {
    return runContext;
  }

  public void setRunContext(RunContext runContext) {
    this.runContext = runContext;
  }
}
