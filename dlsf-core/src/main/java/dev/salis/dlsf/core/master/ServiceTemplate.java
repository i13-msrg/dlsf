package dev.salis.dlsf.core.master;

import akka.actor.typed.Behavior;
import akka.actor.typed.receptionist.ServiceKey;
import dev.salis.dlsf.core.run.RunContext;
import java.io.Serializable;
import java.util.function.Function;

public class ServiceTemplate implements Serializable {
  private ServiceKey<?> serviceKey;
  private Function<RunContext, Behavior<?>> factory;

  public ServiceKey<?> getServiceKey() {
    return serviceKey;
  }

  public void setServiceKey(ServiceKey<?> serviceKey) {
    this.serviceKey = serviceKey;
  }

  public Function<RunContext, Behavior<?>> getFactory() {
    return factory;
  }

  public void setFactory(Function<RunContext, Behavior<?>> factory) {
    this.factory = factory;
  }
}
