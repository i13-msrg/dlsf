package dev.salis.dlsf.core.master;

import dev.salis.dlsf.core.template.AbstractSimulationTemplate;
import java.util.Map;

public class MasterConfig {

  private Map<String, AbstractSimulationTemplate> simulationTemplateMap;

  public MasterConfig(Map<String, AbstractSimulationTemplate> simulationTemplateMap) {
    this.simulationTemplateMap = simulationTemplateMap;
  }

  public Map<String, AbstractSimulationTemplate> getSimulationTemplateMap() {
    return simulationTemplateMap;
  }
}
