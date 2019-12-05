package dev.salis.dlsf.core.master.dto;

import java.util.Date;

public class SimulationRunDto {
  private String simulationName;
  private String runId;
  private Object config;
  private Date startDate;
  private Date endDate;

  public SimulationRunDto() {}

  public String getSimulationName() {
    return simulationName;
  }

  public void setSimulationName(String simulationName) {
    this.simulationName = simulationName;
  }

  public String getRunId() {
    return runId;
  }

  public void setRunId(String runId) {
    this.runId = runId;
  }

  public Object getConfig() {
    return config;
  }

  public void setConfig(Object config) {
    this.config = config;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }
}
