package dev.salis.dlsf.core.reducer;

import akka.actor.typed.ActorRef;
import dev.salis.dlsf.core.master.MasterProtocol.SimulationRunResult;
import java.io.Serializable;

public class BaseReducerProtocol {

  public interface Message extends Serializable {

  }

  public static class ResultRequest implements Message {

    private ActorRef<SimulationRunResult> replyTo;

    public ResultRequest(ActorRef<SimulationRunResult> replyTo) {
      this.replyTo = replyTo;
    }

    public ResultRequest() {}

    public ActorRef<SimulationRunResult> getReplyTo() {
      return replyTo;
    }

    public void setReplyTo(ActorRef<SimulationRunResult> replyTo) {
      this.replyTo = replyTo;
    }
  }

  public static class SimulationEnded implements Message {}
}
