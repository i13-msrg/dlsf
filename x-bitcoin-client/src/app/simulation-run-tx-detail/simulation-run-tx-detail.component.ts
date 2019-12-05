import {Component, OnInit} from '@angular/core';
import {Subscription} from "rxjs";
import {DLSFService} from "../dlsf/dslf.service";
import {ActivatedRoute, ParamMap} from "@angular/router";
import {SimulationResultTxModel} from "../simulation/simulation-result-tx.model";
import {SimulationRunModel} from "../dlsf/simulation-run.model";
import {NetworkStatsModel} from "../simulation/network-stats.model";

@Component({
  selector: 'app-simulation-run-tx-detail',
  templateUrl: './simulation-run-tx-detail.component.html',
  styleUrls: ['./simulation-run-tx-detail.component.scss']
})
export class SimulationRunTxDetailComponent implements OnInit {
  private parentSubscription = new Subscription();
  results: SimulationResultTxModel;
  simulationRun: SimulationRunModel
  chartData: object[];

  constructor(private dlsfService: DLSFService, private route: ActivatedRoute) {
  }

  ngOnInit() {
    const runIdSubscription = this.route.paramMap.subscribe((params: ParamMap) => {
      const runId = params.get('runId');
      this.dlsfService.getSimulationRun(runId).subscribe(simulationRun => {
        this.simulationRun = simulationRun;
      });
      this.dlsfService.getSimulationRunResults<SimulationResultTxModel>(runId).subscribe(results => {
            this.results = results;
        this.chartData = this.createChartData(results.networkStats);
          });
        }
    );
    this.parentSubscription.add(runIdSubscription);
  }

  ngOnDestroy(): void {
    this.parentSubscription.unsubscribe();
  }

  private createChartData(stats: NetworkStatsModel) {
    return [
      {
        name: 'Block Messages',
        value: stats.receivedBlockMessageCount
      },
      {
        name: 'Non Redundant Tx Messages',
        value: stats.receivedTxMessageCount - stats.receivedRedundantTxMessageCount
      },
      {
        name: 'Redundant Tx Messages',
        value: stats.receivedRedundantTxMessageCount
      },
      {
        name: 'Reconciliation Messages',
        value: stats.receivedReconciliationMessageCount
      },
    ];
  }

}
