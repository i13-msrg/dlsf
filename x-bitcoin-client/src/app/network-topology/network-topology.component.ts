import {Component, Input, OnInit} from '@angular/core';
import {Edge, Node} from '@swimlane/ngx-graph';
import {TopologyMapModel} from "../simulation/topology-map.model";
import * as escape from 'css.escape';

@Component({
  selector: 'app-network-topology',
  templateUrl: './network-topology.component.html',
  styleUrls: ['./network-topology.component.scss']
})
export class NetworkTopologyComponent implements OnInit {
  private _topology: TopologyMapModel;

  @Input() set topology(value: TopologyMapModel) {
    this._topology = value;
    if (value) {
      this.updateGraph(value);
    }
  }

  get topology() {
    return this._topology;
  }

  graphLinks: Edge[] = [];
  graphNodes: Node[] = [];

  constructor() {
  }

  ngOnInit() {
  }

  private updateGraph(topologyMap: TopologyMapModel) {
    this.graphNodes = [];
    this.graphLinks = [];
    const linkSet: Set<String> = new Set<String>();
    const nodes: string[] = Object.keys(topologyMap);
    nodes.forEach((node) => {

      this.graphNodes.push({id: escape(node), label: escape(node)});
      const neighbors = topologyMap[node];
      neighbors.forEach(n => {
        // prevent duplicate links
        const linkId = `${node}+${n}`;
        const reverseLinkId = `${n}+${node}`;
        if (linkSet.has(reverseLinkId) || linkSet.has(linkId)) {
          return;
        }
        linkSet.add(linkId);
        this.graphLinks.push({
          id: escape(linkId),
          source: escape(node),
          target: escape(n),
          label: escape(linkId)
        })
      });
    })
  }
}

