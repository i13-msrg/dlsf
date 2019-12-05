import {Component, Input, OnInit} from '@angular/core';
import {StatsUpdateModel} from "../simulation/stats-update.model";

@Component({
  selector: 'app-simulation-stats',
  templateUrl: './simulation-stats.component.html',
  styleUrls: ['./simulation-stats.component.scss']
})
export class SimulationStatsComponent implements OnInit {

  statsDisplayedColums = ['txPerSecond', 'blockPerSecond', 'confirmedBlockCount', 'verifiedBlockCount', 'confirmedTransactionCount', 'verifiedTransactionCount'];
  @Input() stats: StatsUpdateModel = null;

  constructor() {
  }

  ngOnInit() {
  }

}
