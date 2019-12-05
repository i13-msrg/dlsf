package dev.salis.dlsf.boot;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import dev.salis.dlsf.core.master.Master;
import dev.salis.dlsf.core.master.MasterConfig;
import dev.salis.dlsf.core.worker.Worker;
import dev.salis.dlsf.core.worker.WorkerConfig;

class BootActor {

  public static final Behavior<Void> createBehavior(
      MasterConfig masterConfig, WorkerConfig workerConfig) {
    return Behaviors.setup(
        ctx -> {
          if (masterConfig != null) {
            ctx.spawn(Master.createBehavior(masterConfig), "master");
          }
          if (workerConfig != null) {
            ctx.spawn(Worker.createBehavior(workerConfig), workerConfig.getName());
          }
          return Behaviors.empty();
        });
  }
}
