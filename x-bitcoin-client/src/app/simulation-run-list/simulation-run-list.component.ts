import {Component, OnInit} from '@angular/core';
import {BehaviorSubject, Subject} from "rxjs";
import {DLSFService} from "../dlsf/dslf.service";
import {SimulationRunModel} from "../dlsf/simulation-run.model";
import {Router} from "@angular/router";
import {SimulationRunConfigDetailComponent} from "../simulation-run-config-detail/simulation-run-config-detail.component";
import {MatDialog} from "@angular/material/dialog";

@Component({
  selector: 'app-simulation-run-list',
  templateUrl: './simulation-run-list.component.html',
  styleUrls: ['./simulation-run-list.component.scss']
})
export class SimulationRunListComponent implements OnInit {

  dataSource$: Subject<SimulationRunModel[]> = new BehaviorSubject([]);
  displayedColumns = ['runId', 'simulationName', 'startDate', 'endDate', 'actions'];

  constructor(private dlsfService: DLSFService, private router: Router, private dialog: MatDialog) {

  }

  ngOnInit() {
    this.updateData();
  }

  updateData() {
    this.dlsfService.getSimulationRunList().subscribe(
        runList => {
          runList.sort((a, b) => b.endDate - a.endDate);
          this.dataSource$.next(runList);
        }, error => {
          this.dataSource$.next([]);
        });
  }

  delete(runId: string) {
    this.dlsfService.deleteSimulationRun(runId).subscribe({
      'error': err => {
        this.updateData();
      },
      'complete': () => {
        this.updateData();
      }
    });
  }

  showDetails(element: SimulationRunModel) {
    if (element.simulationName === 'bitcoin-explorer') {
      this.router.navigate(['runs/bitcoin-explorer', element.runId]);
      return;
    }
    // bitcoin-tx
    if (element.simulationName === 'bitcoin-tx-erlay' || element.simulationName === 'bitcoin-tx-flood') {
      this.router.navigate(['runs/bitcoin-tx', element.runId]);
      return;
    }
    console.error("unkown simulation name: " + element.simulationName);
  }

  showConfig(element: SimulationRunModel) {
    let dialogRef = this.dialog.open(SimulationRunConfigDetailComponent, {
      data: {config: element.config},
    });
  }

}
