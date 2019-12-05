package dev.salis.dlsf.core.worker;

import akka.actor.typed.ActorRef;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.Receptionist.Listing;
import dev.salis.dlsf.core.master.MasterProtocol;
import dev.salis.dlsf.core.run.RunContext;
import java.io.Serializable;

public abstract class WorkerProtocol {

  private WorkerProtocol() {
  }

  interface Message extends Serializable {

  }

  public interface PodMessage extends Message {

  }

  public interface MasterMsg extends Message {}

  public static final class RegisterSuccessful implements MasterMsg {
    ActorRef<MasterProtocol.Message> master;

    public RegisterSuccessful(ActorRef<MasterProtocol.Message> master) {
      this.master = master;
    }

    public RegisterSuccessful() {}

    public ActorRef<MasterProtocol.Message> getMaster() {
      return master;
    }

    public void setMaster(ActorRef<MasterProtocol.Message> master) {
      this.master = master;
    }
  }

  public static final class Prepare implements MasterMsg {
    private RunContext runContext;

    public Prepare() {}

    public Prepare(RunContext runContext) {
      this.runContext = runContext;
    }

    public RunContext getRunContext() {
      return runContext;
    }

    public void setRunContext(RunContext runContext) {
      this.runContext = runContext;
    }
  }

  public static final class Start implements MasterMsg {
    public Start() {}
  }

  public static final class Stop implements MasterMsg {
    public Stop() {}
  }

  public static final class Ready implements PodMessage {
    public Ready() {}
  }

  public static final class Finished implements PodMessage {

    public Finished() {
    }
  }

  interface InternalMsg extends Message {}

  static final class ListingMsg implements InternalMsg {

    private Receptionist.Listing listing;

    public ListingMsg(Receptionist.Listing listing) {
      this.listing = listing;
    }

    public ListingMsg() {}

    public Receptionist.Listing getListing() {
      return listing;
    }

    public void setListing(Listing listing) {
      this.listing = listing;
    }
  }
}
