package dev.salis.dlsf.core.master.dto;

import dev.salis.dlsf.core.worker.WorkerState;

public class WorkerDto {
  private String name;
  private WorkerState state;

  public WorkerDto() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public WorkerState getState() {
    return state;
  }

  public void setState(WorkerState state) {
    this.state = state;
  }
}
