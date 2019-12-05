import {Component, Input, OnInit} from '@angular/core';
import {SimulationRunModel} from "../dlsf/simulation-run.model";
import {SimulationRunConfigDetailComponent} from "../simulation-run-config-detail/simulation-run-config-detail.component";
import {MatDialog} from "@angular/material/dialog";

@Component({
  selector: 'app-simulation-run-info',
  templateUrl: './simulation-run-info.component.html',
  styleUrls: ['./simulation-run-info.component.scss']
})
export class SimulationRunInfoComponent implements OnInit {
  @Input()
  public simulationRun: SimulationRunModel;

  public displayedColumns = ['runId', 'simulationName', 'startDate', 'endDate', 'actions'];

  constructor(private dialog: MatDialog) {
  }

  ngOnInit() {
  }

  showConfig() {
    let dialogRef = this.dialog.open(SimulationRunConfigDetailComponent, {
      data: {config: this.simulationRun.config},
    });
  }

}
