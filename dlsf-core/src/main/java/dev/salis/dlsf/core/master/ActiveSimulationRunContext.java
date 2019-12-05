package dev.salis.dlsf.core.master;

import akka.actor.typed.ActorRef;
import dev.salis.dlsf.core.run.RunContext;
import java.util.List;

public class ActiveSimulationRunContext {
  private RunContext runContext;
  private WSPublisherMetadata wsPublisherMetadata;
  private List<ActorRef<?>> serviceRefs;

  public ActiveSimulationRunContext() {}

  public RunContext getRunContext() {
    return runContext;
  }

  public void setRunContext(RunContext runContext) {
    this.runContext = runContext;
  }

  public WSPublisherMetadata getWsPublisherMetadata() {
    return wsPublisherMetadata;
  }

  public void setWsPublisherMetadata(WSPublisherMetadata wsPublisherMetadata) {
    this.wsPublisherMetadata = wsPublisherMetadata;
  }

  public List<ActorRef<?>> getServiceRefs() {
    return serviceRefs;
  }

  public void setServiceRefs(List<ActorRef<?>> serviceRefs) {
    this.serviceRefs = serviceRefs;
  }
}
