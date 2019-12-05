import {Component, OnInit} from '@angular/core';
import {DLSFService} from "../dlsf/dslf.service";
import {WorkerModel} from "../dlsf/worker.model";
import {BehaviorSubject, Subject} from "rxjs";
import {SimulationModel} from "../dlsf/simulation.model";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  workersDataSource$: Subject<WorkerModel[]> = new BehaviorSubject([]);
  simulationsDataSource$: Subject<SimulationModel[]> = new BehaviorSubject([]);
  workersDisplayedColumns = ['name', 'state'];
  simulationsDisplayedColumns = ['name'];

  constructor(private dlsfService: DLSFService) {
  }

  ngOnInit() {
    this.updateWorkersData();
    this.updateSimulationsData();
  }

  updateWorkersData() {
    this.dlsfService.getWorkers().subscribe(
        workers => {
          this.workersDataSource$.next(workers);
        }, error => {
          this.workersDataSource$.next([]);
        });
  }

  updateSimulationsData() {
    this.dlsfService.getSimulations().subscribe(simulations => {
      this.simulationsDataSource$.next(simulations);
    }, error => {
      this.simulationsDataSource$.next([]);
    });
  }
}
