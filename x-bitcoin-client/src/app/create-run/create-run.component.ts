import {Component, OnInit} from '@angular/core';
import {CreateSimulationModel} from "../simulation/create-simulation.model";
import {DLSFService} from "../dlsf/dslf.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-create-run',
  templateUrl: './create-run.component.html',
  styleUrls: ['./create-run.component.scss']
})
export class CreateRunComponent implements OnInit {

  explorerTemplate: CreateSimulationModel = {
    "simulationName": "bitcoin-explorer",
    "simulationConfig": {
      "numOfNodesPerPod": 1000,
      "maxBlockHeight": 10,
      "miningDifficulty": 0.999,
      "blockReward": 25,
      "genesisBlockTxOutForEachNode": 100,
      "networkTopologySeed": 0,
      "mineAttemptIntervalInMillis": 1500,
      "createTxIntervalInMillis": 30000,
      "maxOutboundConnections": 8,
      "maxInboundConnections": 117,
      "statsUpdateIntervalInMillis": 3000,
      "blockSizeLimit": 2038
    }
  };

  txFloodTemplate: CreateSimulationModel = {
    ...this.explorerTemplate,
    simulationName: 'bitcoin-tx-flood'
  };

  txErlayTemplate: any = {
    ...this.explorerTemplate,
    simulationName: 'bitcoin-tx-erlay',
    simulationConfig: {
      ...this.explorerTemplate.simulationConfig,
      erlayReconciliationInterval: 250
    }
  };

  formModelText: string = '';

  constructor(private dlsfService: DLSFService, private router: Router) {
  }

  ngOnInit() {
    this.setTemplate(this.explorerTemplate);
  }

  setTemplate(template: any) {
    this.formModelText = JSON.stringify(template, undefined, 2);
  }

  onSubmit() {
    const config = JSON.parse(this.formModelText)
    this.dlsfService.runSimulation(config).subscribe(() => {
      this.router.navigate(['active-run']);
    }, error => {
      console.error(error);
    });
  }
}
