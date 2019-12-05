package dev.salis.dlsf.x.bitcoinserver;

import dev.salis.dlsf.boot.Boot;
import dev.salis.dlsf.boot.NodeType;
import dev.salis.dlsf.core.template.AbstractSimulationTemplate;
import dev.salis.dlsf.x.bitcoinexplorer.BitcoinExplorerSimulationTemplate;
import dev.salis.dlsf.x.bitcointxprotocols.TxErlayTemplate;
import dev.salis.dlsf.x.bitcointxprotocols.TxFloodTemplate;
import java.util.ArrayList;
import java.util.List;

public class MainWorker2 {

  public static void main(String[] args) {

    List<AbstractSimulationTemplate> simulationTemplates = new ArrayList<>();
    simulationTemplates.add(new BitcoinExplorerSimulationTemplate());
    simulationTemplates.add(new TxErlayTemplate());
    simulationTemplates.add(new TxFloodTemplate());
    Boot.start(NodeType.WORKER, "worker2", simulationTemplates);
  }
}
