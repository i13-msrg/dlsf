package dev.salis.dlsf.core.master.dto;

import com.fasterxml.jackson.databind.JsonNode;

public class CreateSimulationRunDto {
  private String simulationName;
  private JsonNode simulationConfig;

  public CreateSimulationRunDto() {}

  public String getSimulationName() {
    return simulationName;
  }

  public void setSimulationName(String simulationName) {
    this.simulationName = simulationName;
  }

  public JsonNode getSimulationConfig() {
    return simulationConfig;
  }

  public void setSimulationConfig(JsonNode simulationConfig) {
    this.simulationConfig = simulationConfig;
  }
}
