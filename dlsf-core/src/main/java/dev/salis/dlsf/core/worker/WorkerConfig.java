package dev.salis.dlsf.core.worker;

import dev.salis.dlsf.core.template.AbstractSimulationTemplate;
import java.util.Map;

public class WorkerConfig {
  private String name;
  private Map<String, AbstractSimulationTemplate> simulationTemplateMap;

  public WorkerConfig(String name, Map<String, AbstractSimulationTemplate> simulationTemplateMap) {
    this.name = name;
    this.simulationTemplateMap = simulationTemplateMap;
  }

  public String getName() {
    return name;
  }

  public Map<String, AbstractSimulationTemplate> getSimulationTemplateMap() {
    return simulationTemplateMap;
  }
}
