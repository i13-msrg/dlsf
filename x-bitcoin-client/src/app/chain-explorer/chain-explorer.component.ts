import {Component, Input, OnInit} from '@angular/core';
import {BlockMapModel} from "../simulation/block-map.model";
import {BlockModel} from "../simulation/block.model";
import {TxModel} from "../simulation/tx.model";

@Component({
  selector: 'app-chain-explorer',
  templateUrl: './chain-explorer.component.html',
  styleUrls: ['./chain-explorer.component.scss']
})
export class ChainExplorerComponent implements OnInit {
  private _blockMap: BlockMapModel;
  @Input() set blockMap(value: BlockMapModel) {
    this._blockMap = value;
    this.update();
  }

  get blockMap() {
    return this._blockMap;
  }

  private txToBlockMap: Map<string, string> = new Map();
  private txMap: Map<string, TxModel> = new Map();

  blockHashList: string[];
  selectedBlockHash: string;
  selectedBlock: BlockModel;
  selectedTxHash: string;
  selectedTx: TxModel;

  constructor() {
  }

  ngOnInit() {
  }

  private update() {
    this.blockHashList = Object.keys(this.blockMap);

    this.txToBlockMap = new Map();
    for (let blockHash of Object.keys(this.blockMap)) {
      const block = this.blockMap[blockHash];
      for (let tx of block.transactions) {
        this.txMap.set(tx.hash, tx);
        this.txToBlockMap.set(tx.hash, blockHash);
      }
    }
  }

  setSelectedBlockHash(value: string) {
    this.setSelectedTxHash(null);
    this.selectedBlockHash = value;
    this.selectedBlock = this.blockMap[this.selectedBlockHash];
  }

  setSelectedTxHash(value: string) {
    this.selectedTxHash = value;
    this.selectedTx = this.txMap.get(this.selectedTxHash);
  }

  navigateToTx(txHash: string) {
    const blockHash = this.txToBlockMap.get(txHash);
    this.setSelectedBlockHash(blockHash);
    this.setSelectedTxHash(txHash);
  }
}
