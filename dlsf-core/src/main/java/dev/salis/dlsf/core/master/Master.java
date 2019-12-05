package dev.salis.dlsf.core.master;

import static akka.http.javadsl.server.PathMatchers.segment;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Adapter;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.ws.TextMessage;
import akka.http.javadsl.server.Directives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.KillSwitches;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.BroadcastHub;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import ch.megard.akka.http.cors.javadsl.CorsDirectives;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.salis.dlsf.core.master.MasterProtocol.RegisterWorker;
import dev.salis.dlsf.core.master.MasterProtocol.SimulationRunResult;
import dev.salis.dlsf.core.master.MasterProtocol.SimulationRunUpdate;
import dev.salis.dlsf.core.master.MasterProtocol.UpdateWorkerState;
import dev.salis.dlsf.core.master.dto.CreateSimulationRunDto;
import dev.salis.dlsf.core.master.dto.SimulationDto;
import dev.salis.dlsf.core.master.dto.SimulationRunDto;
import dev.salis.dlsf.core.master.dto.WorkerDto;
import dev.salis.dlsf.core.reducer.BaseReducerProtocol;
import dev.salis.dlsf.core.reducer.BaseReducerProtocol.ResultRequest;
import dev.salis.dlsf.core.reducer.BaseReducerProtocol.SimulationEnded;
import dev.salis.dlsf.core.run.RunContext;
import dev.salis.dlsf.core.template.AbstractSimulationTemplate;
import dev.salis.dlsf.core.worker.WorkerProtocol;
import dev.salis.dlsf.core.worker.WorkerProtocol.RegisterSuccessful;
import dev.salis.dlsf.core.worker.WorkerState;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class Master extends AbstractBehavior<MasterProtocol.Message> {

  public static final ServiceKey<RegisterWorker> serviceKey =
      ServiceKey.create(RegisterWorker.class, "dlsf:master");
  private final ActorContext<MasterProtocol.Message> context;
  private final Map<String, WorkerInfo> workerInfoMap = new HashMap<>();
  private final Map<String, AbstractSimulationTemplate> simulationTemplateMap;
  private final Map<String, RunContext> runContextMap = new HashMap<>();
  private ActiveSimulationRunContext activeSimulationRunContext;

  public Master(ActorContext<MasterProtocol.Message> context, MasterConfig config) {
    this.context = context;
    this.simulationTemplateMap = config.getSimulationTemplateMap();
    // register actor to receptionist
    context
        .getSystem()
        .receptionist()
        .tell(Receptionist.register(serviceKey, context.getSelf().narrow()));
    startServer();
  }

  public static Behavior<MasterProtocol.Message> createBehavior(MasterConfig config) {
    return Behaviors.setup(ctx -> new Master(ctx, config));
  }

  @Override
  public Receive<MasterProtocol.Message> createReceive() {
    return newReceiveBuilder()
        .onMessage(
            RegisterWorker.class,
            msg -> {
              WorkerInfo info = new WorkerInfo(msg.getName(), msg.getRef(), WorkerState.IDLE);
              workerInfoMap.put(info.getName(), info);
              final RegisterSuccessful registerSuccessfulMsg = new RegisterSuccessful();
              registerSuccessfulMsg.setMaster(context.getSelf().narrow());
              info.getRef().tell(registerSuccessfulMsg);
              return this;
            })
        .onMessage(
            UpdateWorkerState.class,
            msg -> {
              WorkerInfo info = workerInfoMap.get(msg.getWorkerName());
              info.setState(msg.getWorkerState());
              if (doAllWorkersHaveState(WorkerState.FINISHED)) {
                workerInfoMap
                    .values()
                    .forEach(
                        worker -> {
                          worker.getRef().tell(new WorkerProtocol.Stop());
                        });
              }
              if (doAllWorkersHaveState(WorkerState.IDLE)) {
                if (activeSimulationRunContext != null) {
                  // teardown active simulation run context
                  activeSimulationRunContext.getRunContext().setEndDate(new Date());
                  activeSimulationRunContext
                      .getRunContext()
                      .getReducer()
                      .unsafeUpcast()
                      .tell(new SimulationEnded());
                  for (ActorRef<?> serviceRef : activeSimulationRunContext.getServiceRefs()) {
                    this.context.stop(serviceRef);
                  }
                  activeSimulationRunContext.getWsPublisherMetadata().getKillSwitch().shutdown();
                  this.runContextMap.put(
                      activeSimulationRunContext.getRunContext().getId(),
                      activeSimulationRunContext.getRunContext());
                  this.activeSimulationRunContext = null;
                }
                return this;
              }
              if (doAllWorkersHaveState(WorkerState.READY)) {
                if (activeSimulationRunContext != null) {
                  activeSimulationRunContext.getRunContext().setStartDate(new Date());
                  this.workerInfoMap
                      .values()
                      .forEach(worker -> worker.getRef().tell(new WorkerProtocol.Start()));
                }
                return this;
              }
              return this;
            })
        .onMessage(
            SimulationRunUpdate.class,
            msg -> {
              ObjectMapper objectMapper = new ObjectMapper();
              String s = objectMapper.writeValueAsString(msg);
              this.activeSimulationRunContext.getWsPublisherMetadata().getActorRef().tell(s);
              return this;
            })
        .build();
  }

  /**
   * Create HTTP endpoints for users to communicate with the system.
   */
  private Route createRoute() {
    Route routeDirectives =
        Directives.concat(
            Directives.path(
                "workers",
                () ->
                    Directives.get(
                        () -> {
                          List<WorkerDto> workerDtos =
                              workerInfoMap.values().stream()
                                  .map(
                                      info -> {
                                        WorkerDto dto = new WorkerDto();
                                        dto.setName(info.getName());
                                        dto.setState(info.getState());
                                        return dto;
                                      })
                                  .collect(Collectors.toList());
                          return Directives.completeOK(workerDtos, Jackson.marshaller());
                        })),
            Directives.path(
                "simulations",
                () ->
                    Directives.get(
                        () -> {
                          List<SimulationDto> simulationDtos =
                              simulationTemplateMap.keySet().stream()
                                  .map(
                                      simulationName -> {
                                        SimulationDto dto = new SimulationDto();
                                        dto.setName(simulationName);
                                        return dto;
                                      })
                                  .collect(Collectors.toList());
                          return Directives.completeOK(simulationDtos, Jackson.marshaller());
                        })),
            Directives.path(
                "active-run",
                () ->
                    Directives.concat(
                        Directives.get(
                            () -> {
                              if (activeSimulationRunContext == null) {
                                return Directives.complete(StatusCodes.NOT_FOUND);
                              }
                              SimulationRunDto dto =
                                  this.mapRunContextToRunDto(
                                      this.activeSimulationRunContext.getRunContext());
                              return Directives.completeOK(dto, Jackson.marshaller());
                            }),
                        Directives.put(
                            () ->
                                Directives.entity(
                                    Jackson.unmarshaller(CreateSimulationRunDto.class),
                                    this::prepareSimulation)),
                        Directives.delete(
                            () -> {
                              if (doAllWorkersHaveState(WorkerState.IDLE)) {
                                return Directives.complete(StatusCodes.OK);
                              }
                              this.workerInfoMap
                                  .values()
                                  .forEach(sim -> sim.getRef().tell(new WorkerProtocol.Stop()));
                              return Directives.complete(StatusCodes.ACCEPTED);
                            }))),
            Directives.path(
                segment("active-run").slash("updates"),
                () -> {
                  if (this.activeSimulationRunContext == null) {
                    return Directives.complete(StatusCodes.NOT_FOUND);
                  }
                  return Directives.handleWebSocketMessages(
                      Flow.fromSinkAndSource(
                          Sink.ignore(),
                          this.activeSimulationRunContext
                              .getWsPublisherMetadata()
                              .getSource()
                              .map(x -> TextMessage.create(x.toString()))));
                }),
            Directives.path(
                "runs",
                () ->
                    Directives.get(
                        () -> {
                          List<SimulationRunDto> runDtos =
                              runContextMap.values().stream()
                                  .map(this::mapRunContextToRunDto)
                                  .collect(Collectors.toList());
                          return Directives.completeOK(runDtos, Jackson.marshaller());
                        })),
            Directives.path(
                segment("runs").slash(segment()),
                id ->
                    Directives.concat(
                        Directives.get(
                            () -> {
                              RunContext run = runContextMap.get(id);
                              if (run == null) {
                                return Directives.complete(StatusCodes.NOT_FOUND);
                              }
                              SimulationRunDto dto = this.mapRunContextToRunDto(run);
                              return Directives.completeOK(dto, Jackson.marshaller());
                            }),
                        Directives.delete(
                            () -> {
                              RunContext runContext = runContextMap.get(id);
                              if (runContext == null) {
                                return Directives.complete(StatusCodes.NOT_FOUND);
                              }
                              deleteSimulationRun(runContext);
                              return Directives.complete(StatusCodes.OK);
                            }))),
            Directives.path(
                segment("runs").slash(segment()).slash(segment("results")),
                id ->
                    Directives.get(
                        () -> {
                          RunContext runContext = runContextMap.get(id);
                          if (runContext == null) {
                            return Directives.complete(StatusCodes.NOT_FOUND);
                          }
                          CompletionStage completionStage =
                              AskPattern.ask(
                                  runContext.getReducer().unsafeUpcast(),
                                  ref -> new ResultRequest(ref.unsafeUpcast()),
                                  Duration.ofMinutes(1),
                                  context.getSystem().scheduler());
                          return Directives.onComplete(
                              () -> completionStage,
                              response -> {
                                if (response.isFailure()) {
                                  return Directives.complete(
                                      StatusCodes.INTERNAL_SERVER_ERROR,
                                      response.get(),
                                      Jackson.marshaller());
                                }
                                SimulationRunResult reducerResult =
                                    (SimulationRunResult) response.get();
                                return Directives.completeOK(
                                    reducerResult.getValue(), Jackson.marshaller());
                              });
                        })));
    return CorsDirectives.cors(() -> routeDirectives);
  }

  private boolean doAllWorkersHaveState(WorkerState state) {
    return workerInfoMap.values().stream()
        .map(WorkerInfo::getState)
        .allMatch(workerState -> workerState == state);
  }

  private CompletionStage<ServerBinding> startServer() {
    ActorSystem untypedSystem = Adapter.toUntyped(context.getSystem());
    Http http = Http.get(untypedSystem);
    final ActorMaterializer materializer = ActorMaterializer.create(untypedSystem);
    Flow<HttpRequest, HttpResponse, NotUsed> routeFlow =
        this.createRoute().flow(untypedSystem, materializer);
    return http.bindAndHandle(routeFlow, ConnectHttp.toHost("localhost", 8080), materializer);
  }

  private Route prepareSimulation(CreateSimulationRunDto dto) {
    if (!doAllWorkersHaveState(WorkerState.IDLE)) {
      return Directives.complete(StatusCodes.CONFLICT);
    }
    AbstractSimulationTemplate template = simulationTemplateMap.get(dto.getSimulationName());
    if (template == null) {
      return Directives.complete(StatusCodes.BAD_REQUEST);
    }
    RunContext runContext = new RunContext();
    if (dto.getSimulationConfig() != null) {
      ObjectMapper objectMapper = new ObjectMapper();
      try {
        Object simulationConfig =
            objectMapper.treeToValue(dto.getSimulationConfig(), template.getConfigClass());
        runContext.setSimulationConfig(simulationConfig);
      } catch (JsonProcessingException e) {
        return Directives.complete(StatusCodes.BAD_REQUEST, e.getMessage());
      }
    }
    runContext.setId(template.getName() + ":" + UUID.randomUUID().toString());
    runContext.setUpdateGateway(this.context.getSelf().narrow());
    runContext.setSimulationName(dto.getSimulationName());
    runContext.setWorkerNames(new ArrayList<>(this.workerInfoMap.keySet()));
    // spawn reducer
    ActorRef<? extends BaseReducerProtocol.Message> reducerRef =
        this.context.spawn(
            template.getReducerFactory().apply(runContext),
            "master:" + runContext.getId() + ":reducer");
    runContext.setReducer(reducerRef);

    ActiveSimulationRunContext activeSimulationRunContext = new ActiveSimulationRunContext();
    activeSimulationRunContext.setRunContext(runContext);
    // create ws publisher
    ActorSystem untypedSystem = Adapter.toUntyped(context.getSystem());
    final ActorMaterializer materializer = ActorMaterializer.create(untypedSystem);
    WSPublisherMetadata wsPublisherMetadata =
        Source.actorRef(1, OverflowStrategy.dropTail())
            .viaMat(
                KillSwitches.single(),
                (actorRef, killSwitch) -> {
                  WSPublisherMetadata metadata = new WSPublisherMetadata();
                  metadata.setActorRef(Adapter.toTyped(actorRef));
                  metadata.setKillSwitch(killSwitch);
                  return metadata;
                })
            .toMat(
                BroadcastHub.of(Object.class, 1),
                (metadata, source) -> {
                  metadata.setSource(source);
                  return metadata;
                })
            .run(materializer);
    activeSimulationRunContext.setWsPublisherMetadata(wsPublisherMetadata);
    // spawn and register master services
    List<ActorRef<?>> serviceRefs = new ArrayList<>(template.getServiceTemplates().size());
    for (ServiceTemplate serviceTemplate : template.getServiceTemplates()) {
      ActorRef<?> actorRef =
          this.context.spawn(
              serviceTemplate.getFactory().apply(runContext),
              "master:" + runContext.getId() + ":service:" + serviceTemplate.getServiceKey().id());
      this.context
          .getSystem()
          .receptionist()
          .tell(Receptionist.register(serviceTemplate.getServiceKey(), actorRef.unsafeUpcast()));
      serviceRefs.add(actorRef);
    }
    activeSimulationRunContext.setServiceRefs(serviceRefs);
    this.activeSimulationRunContext = activeSimulationRunContext;
    // prepare simulation
    this.workerInfoMap
        .values()
        .forEach(sim -> sim.getRef().tell(new WorkerProtocol.Prepare(runContext)));
    SimulationRunDto responseDto = this.mapRunContextToRunDto(runContext);
    return Directives.complete(StatusCodes.ACCEPTED, responseDto, Jackson.marshaller());
  }

  private void deleteSimulationRun(RunContext simulationRun) {
    context.stop(simulationRun.getReducer());
    runContextMap.remove(simulationRun.getId());
  }

  private SimulationRunDto mapRunContextToRunDto(RunContext runContext) {
    SimulationRunDto runDto = new SimulationRunDto();
    runDto.setSimulationName(runContext.getSimulationName());
    runDto.setRunId(runContext.getId());
    runDto.setStartDate(runContext.getStartDate());
    runDto.setEndDate(runContext.getEndDate());
    runDto.setConfig(runContext.getSimulationConfig());
    return runDto;
  }
}
