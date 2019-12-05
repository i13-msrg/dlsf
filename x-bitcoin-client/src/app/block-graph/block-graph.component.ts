import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {BlockModel} from "../simulation/block.model";
import {Subscription} from "rxjs";
import {Edge, Node} from "@swimlane/ngx-graph";
import {BlockMapModel} from "../simulation/block-map.model";
import * as escape from 'css.escape';


@Component({
  selector: 'app-block-graph',
  templateUrl: './block-graph.component.html',
  styleUrls: ['./block-graph.component.scss']
})
export class BlockGraphComponent implements OnInit, OnDestroy {

  private _blockMap: BlockMapModel;
  @Input() set blockMap(value: BlockMapModel) {
    this._blockMap = value;
    this.updateGraph();
  };

  get blockMap() {
    return this._blockMap;
  }

  graphLinks: Edge[];
  graphNodes: Node[];

  private parentSubscription = new Subscription();

  constructor() {
  }

  ngOnInit() {
    this.resetGraph();
  }

  ngOnDestroy() {
    this.parentSubscription.unsubscribe();
  }

  private updateGraph() {
    this.resetGraph();
    if (this.blockMap) {
      Object.values(this.blockMap).forEach(block => {
        this.addBlockToGraph(block);
      });
    }
  }

  private resetGraph() {
    this.graphNodes = [];
    this.graphLinks = [];
  }

  private addBlockToGraph(block: BlockModel) {
    this.graphNodes = [...this.graphNodes, {
      id: block.hash,
      label: escape(block.hash)
    }];
    if (!block.prevBlockHash) {
      return;
    }
    const linkId = `${block.hash}${block.prevBlockHash}`;
    this.graphLinks = [...this.graphLinks, {
      id: escape(linkId),
      label: escape(linkId),
      source: block.prevBlockHash,
      target: block.hash
    }];
  }

}
