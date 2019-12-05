package dev.salis.dlsf.core.template;

import akka.actor.typed.Behavior;
import dev.salis.dlsf.core.master.ServiceTemplate;
import dev.salis.dlsf.core.pod.AbstractPodProtocol.Message;
import dev.salis.dlsf.core.pod.PodContext;
import dev.salis.dlsf.core.reducer.BaseReducerProtocol;
import dev.salis.dlsf.core.run.RunContext;
import java.util.Collection;
import java.util.function.Function;

public abstract class AbstractSimulationTemplate {

  public abstract String getName();

  public abstract Function<PodContext, Behavior<Message>> getPodFactory();

  public abstract Function<RunContext, Behavior<? extends BaseReducerProtocol.Message>>
      getReducerFactory();

  public abstract Collection<ServiceTemplate> getServiceTemplates();

  public abstract Class<?> getConfigClass();
}
