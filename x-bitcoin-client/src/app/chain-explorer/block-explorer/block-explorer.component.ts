import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {BlockModel} from "../../simulation/block.model";

@Component({
  selector: 'app-block-explorer',
  templateUrl: './block-explorer.component.html',
  styleUrls: ['./block-explorer.component.scss']
})
export class BlockExplorerComponent implements OnInit {

  @Input() block: BlockModel;

  @Input() selectedTx: string;
  @Output() selectedTxChange = new EventEmitter<string>();

  constructor() {
  }

  ngOnInit() {
  }

  handleSelection(value: string) {
    this.selectedTxChange.emit(value);
  }

}
