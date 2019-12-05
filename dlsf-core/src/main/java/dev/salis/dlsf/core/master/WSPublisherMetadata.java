package dev.salis.dlsf.core.master;

import akka.NotUsed;
import akka.actor.typed.ActorRef;
import akka.stream.UniqueKillSwitch;
import akka.stream.javadsl.Source;

public class WSPublisherMetadata {

  private ActorRef<Object> actorRef;
  private Source<Object, NotUsed> source;
  private UniqueKillSwitch killSwitch;

  public WSPublisherMetadata() {
  }

  public ActorRef<Object> getActorRef() {
    return actorRef;
  }

  public void setActorRef(ActorRef<Object> actorRef) {
    this.actorRef = actorRef;
  }

  public Source<Object, NotUsed> getSource() {
    return source;
  }

  public void setSource(Source<Object, NotUsed> source) {
    this.source = source;
  }

  public UniqueKillSwitch getKillSwitch() {
    return killSwitch;
  }

  public void setKillSwitch(UniqueKillSwitch killSwitch) {
    this.killSwitch = killSwitch;
  }
}
