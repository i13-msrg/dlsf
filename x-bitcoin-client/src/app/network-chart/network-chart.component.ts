import {Component, Input, OnInit} from '@angular/core';
import {TotalNumOfConnectionsModel} from "../simulation/total-num-of-connections.model";

@Component({
  selector: 'app-network-chart',
  templateUrl: './network-chart.component.html',
  styleUrls: ['./network-chart.component.scss']
})
export class NetworkChartComponent implements OnInit {
  chartResults: object[] = [];
  private _connections: TotalNumOfConnectionsModel;

  @Input() set connections(value: TotalNumOfConnectionsModel) {
    this._connections = value;
    if (value) {
      this.updateChartResults(value);
    }
  }

  get connections() {
    return this._connections;
  }

  constructor() {
  }

  ngOnInit() {
  }

  private updateChartResults(map: TotalNumOfConnectionsModel) {
    const connectionStatsMap = new Map();
    this.chartResults = [];
    const nodes: string[] = Object.keys(map);
    nodes.forEach(nodeId => {
      const numOfConnections = map[nodeId];
      connectionStatsMap.set(numOfConnections, (connectionStatsMap.get(numOfConnections) || 0) + 1);
    });
    connectionStatsMap.forEach((value, key) => {
      this.chartResults.push({
        name: key,
        value: value
      });
    });
    this.chartResults.sort((a: any, b: any) => {
      return a.name - b.name;
    })
  }
}
