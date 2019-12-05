import {Component, Input, OnInit} from '@angular/core';
import {NetworkStatsModel} from "../simulation/network-stats.model";

@Component({
  selector: 'app-simulation-network-stats',
  templateUrl: './simulation-network-stats.component.html',
  styleUrls: ['./simulation-network-stats.component.scss']
})
export class SimulationNetworkStatsComponent implements OnInit {

  statsDisplayedColums = ['receivedBlockMessageCount', 'receivedTxMessageCount', 'receivedNonredundantTxMessageCount', 'receivedRedundantTxMessageCount', 'receivedReconciliationMessageCount'];
  @Input() stats: NetworkStatsModel = null;

  constructor() {
  }

  ngOnInit() {
  }

}
