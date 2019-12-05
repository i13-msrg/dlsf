import {Component, OnDestroy, OnInit} from '@angular/core';
import {DLSFService} from "../dlsf/dslf.service";
import {BehaviorSubject, Subscription} from "rxjs";
import {TopologyMapModel} from "../simulation/topology-map.model";
import {StatsUpdateModel} from "../simulation/stats-update.model";
import {SimulationRunModel} from "../dlsf/simulation-run.model";
import {Router} from "@angular/router";

@Component({
  selector: 'app-active-run',
  templateUrl: './active-run.component.html',
  styleUrls: ['./active-run.component.scss']
})
export class ActiveRunComponent implements OnInit, OnDestroy {
  networkTopology$ = new BehaviorSubject<TopologyMapModel>({});
  statsUpdate: StatsUpdateModel;
  activeRun: SimulationRunModel;
  statsDisplayedColums = ['txPerSecond', 'blockPerSecond', 'confirmedBlockCount', 'verifiedBlockCount', 'verifiedTransactionCount', 'confirmedTransactionCount'];
  noActiveSimulationError = false;
  private parentSubscription = new Subscription();


  constructor(private dlsfService: DLSFService, private router: Router) {
  }

  ngOnInit() {
    this.noActiveSimulationError = false;
    const getActiveRunSub: Subscription = this.dlsfService.getActiveSimulationRun().subscribe(activeRun => {
      this.activeRun = activeRun;
      const getActiveRunUpdatesSub: Subscription = this.dlsfService.getActiveSimulationRunUpdates<any>().subscribe(update => {
        // const str = JSON.stringify(update, undefined, 2);
        switch (update.type) {
          case 'network-topology':
            this.networkTopology$.next(update.value);
            break;
          case 'stats':
            this.statsUpdate = update.value;
            break;
          default:
            console.log('unknown update');
            console.log(update);
        }
      }, err => {

      }, () => {
        this.showDetails();
      });
      this.parentSubscription.add(getActiveRunUpdatesSub);
    }, err => {
      this.noActiveSimulationError = true;
    });
    this.parentSubscription.add(getActiveRunSub);
  }

  stop() {
    this.dlsfService.stopActiveSimulationRun().subscribe();
  }

  ngOnDestroy(): void {
    this.parentSubscription.unsubscribe();
  }

  showDetails() {
    if (this.activeRun.simulationName === 'bitcoin-explorer') {
      this.router.navigate(['runs/bitcoin-explorer', this.activeRun.runId]);
      return;
    }
    // bitcoin-tx
    if (this.activeRun.simulationName === 'bitcoin-tx-erlay' || this.activeRun.simulationName === 'bitcoin-tx-flood') {
      this.router.navigate(['runs/bitcoin-tx', this.activeRun.runId]);
      return;
    }
    console.error("unkown simulation name: " + this.activeRun.simulationName);
  }
}
