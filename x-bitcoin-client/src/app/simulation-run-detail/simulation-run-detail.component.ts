import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, ParamMap} from "@angular/router";
import {DLSFService} from "../dlsf/dslf.service";
import {SimulationResultModel} from "../simulation/simulation-result.model";
import {Subscription} from "rxjs";
import {BlockMapModel} from "../simulation/block-map.model";
import {SimulationRunModel} from "../dlsf/simulation-run.model";

@Component({
  selector: 'app-simulation-run-detail',
  templateUrl: './simulation-run-detail.component.html',
  styleUrls: ['./simulation-run-detail.component.scss']
})
export class SimulationRunDetailComponent implements OnInit, OnDestroy {
  private parentSubscription = new Subscription();
  showTopology = false;
  showConnectionChart = false;
  simulationRun: SimulationRunModel;
  results: SimulationResultModel;
  activeChainBlockMap: BlockMapModel = {};

  constructor(private dlsfService: DLSFService, private route: ActivatedRoute) {
  }

  ngOnInit() {
    const runIdSubscription = this.route.paramMap.subscribe((params: ParamMap) => {
      const runId = params.get('runId');
      this.dlsfService.getSimulationRun(runId).subscribe(simulationRun => {
        this.simulationRun = simulationRun;
      });
          this.dlsfService.getSimulationRunResults<SimulationResultModel>(runId)
          .subscribe(results => {
            this.results = results;
            this.activeChainBlockMap = this.createActiveChainBlockMap(results);
          });
        }
    );
    this.parentSubscription.add(runIdSubscription);
  }

  ngOnDestroy(): void {
    this.parentSubscription.unsubscribe();
  }

  private createActiveChainBlockMap(result: SimulationResultModel) {
    const activeChainBlockMap: BlockMapModel = {};
    result.mainChainBlockHashList.forEach(blockHash => {
      activeChainBlockMap[blockHash] = result.blockMap[blockHash];
    });
    return activeChainBlockMap;
  }

}
