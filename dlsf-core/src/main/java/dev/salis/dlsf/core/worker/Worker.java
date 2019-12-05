package dev.salis.dlsf.core.worker;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import dev.salis.dlsf.core.master.Master;
import dev.salis.dlsf.core.master.MasterProtocol.Message;
import dev.salis.dlsf.core.master.MasterProtocol.RegisterWorker;
import dev.salis.dlsf.core.master.MasterProtocol.UpdateWorkerState;
import dev.salis.dlsf.core.pod.AbstractPodProtocol;
import dev.salis.dlsf.core.pod.PodContext;
import dev.salis.dlsf.core.template.AbstractSimulationTemplate;
import dev.salis.dlsf.core.worker.WorkerProtocol.Finished;

public class Worker extends AbstractBehavior<WorkerProtocol.Message> {

  private final ActorContext<WorkerProtocol.Message> context;
  private final WorkerConfig config;
  private WorkerState state;
  private ActorRef<Message> master;
  private ActorRef<AbstractPodProtocol.Message> activePod;

  public Worker(ActorContext<WorkerProtocol.Message> context, WorkerConfig config) {
    this.context = context;
    this.config = config;
    this.state = WorkerState.IDLE;
    final ActorRef<Receptionist.Listing> listingMsgAdapter =
        context.messageAdapter(Receptionist.Listing.class, WorkerProtocol.ListingMsg::new);
    this.context
        .getSystem()
        .receptionist()
        .tell(Receptionist.subscribe(Master.serviceKey, listingMsgAdapter));
  }

  public static Behavior<WorkerProtocol.Message> createBehavior(WorkerConfig config) {
    return Behaviors.setup(context -> new Worker(context, config));
  }

  @Override
  public Receive<WorkerProtocol.Message> createReceive() {
    return newReceiveBuilder()
        .onMessage(
            WorkerProtocol.ListingMsg.class,
            msg -> {
              final var master =
                  msg.getListing().getServiceInstances(Master.serviceKey).stream().findFirst();
              if (master.isEmpty()) {
                if (this.master != null) {
                  context.getLog().info("Master went offline.");
                }
                context.getLog().info("Seeking cluster master");
                this.master = null;
                this.context.getSelf().tell(new Finished());
                return this;
              }
              context.getLog().info("Cluster master found");
              master
                  .get()
                  .tell(new RegisterWorker(this.config.getName(), this.context.getSelf().narrow()));
              return this;
            })
        .onMessage(
            WorkerProtocol.RegisterSuccessful.class,
            msg -> {
              this.master = msg.master;
              return this;
            })
        .onMessage(
            WorkerProtocol.Prepare.class,
            msg -> {
              this.changeStateAndNotifyMaster(WorkerState.PREPARING);
              AbstractSimulationTemplate simulationTemplate =
                  this.config
                      .getSimulationTemplateMap()
                      .get(msg.getRunContext().getSimulationName());
              final PodContext podContext = new PodContext();
              podContext.setRunContext(msg.getRunContext());
              podContext.setWorker(this.context.getSelf().narrow());
              podContext.setWorkerName(this.config.getName());
              this.activePod =
                  context.spawn(
                      simulationTemplate.getPodFactory().apply(podContext), "active-simulation");
              return this;
            })
        .onMessage(
            WorkerProtocol.Ready.class,
            msg -> {
              this.changeStateAndNotifyMaster(WorkerState.READY);
              return this;
            })
        .onMessage(
            WorkerProtocol.Start.class,
            msg -> {
              this.activePod.tell(new AbstractPodProtocol.Start());
              this.changeStateAndNotifyMaster(WorkerState.RUNNING);
              return this;
            })
        .onMessage(
            WorkerProtocol.Stop.class,
            msg -> {
              if (this.activePod != null) {
                context.stop(this.activePod);
              }
              this.changeStateAndNotifyMaster(WorkerState.IDLE);
              return this;
            })
        .onMessage(
            Finished.class,
            msg -> {
              this.changeStateAndNotifyMaster(WorkerState.FINISHED);
              return this;
            })
        .build();
  }

  private void changeStateAndNotifyMaster(final WorkerState state) {
    this.state = state;
    if (this.master == null) {
      return;
    }
    this.master.tell(new UpdateWorkerState(this.config.getName(), state));
    System.out.println(
        "Worker " + this.config.getName() + " state changed to : " + state.toString());
  }
}
