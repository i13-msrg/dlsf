package dev.salis.dlsf.x.bitcoinexplorer.pod;

import akka.actor.typed.ActorRef;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.Receptionist.Listing;
import dev.salis.dlsf.bitcoin.network.BitcoinNetworkCoordinatorProtocol;
import dev.salis.dlsf.core.pod.AbstractPodProtocol;

public class PodProtocol extends AbstractPodProtocol {

  interface NodeMsg extends Message {}

  public static final class NodeFinished implements NodeMsg {
    private String nodeName;

    public NodeFinished(String nodeName) {
      this.nodeName = nodeName;
    }

    public String getNodeName() {
      return nodeName;
    }
  }

  interface InternalMsg extends Message {}

  static final class Prepare implements InternalMsg {
    private ActorRef<BitcoinNetworkCoordinatorProtocol.Message> coordinator;

    public Prepare(ActorRef<BitcoinNetworkCoordinatorProtocol.Message> coordinator) {
      this.coordinator = coordinator;
    }

    public ActorRef<BitcoinNetworkCoordinatorProtocol.Message> getCoordinator() {
      return coordinator;
    }

    public void setCoordinator(ActorRef<BitcoinNetworkCoordinatorProtocol.Message> coordinator) {
      this.coordinator = coordinator;
    }
  }

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
