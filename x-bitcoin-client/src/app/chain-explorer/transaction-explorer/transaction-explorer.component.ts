import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {TxModel} from "../../simulation/tx.model";

@Component({
  selector: 'app-transaction-explorer',
  templateUrl: './transaction-explorer.component.html',
  styleUrls: ['./transaction-explorer.component.scss']
})
export class TransactionExplorerComponent implements OnInit {

  @Input() tx: TxModel;

  @Output() txSelected: EventEmitter<string> = new EventEmitter();

  constructor() {
  }

  ngOnInit() {
  }

  onTxSelection(value: string) {
    this.txSelected.emit(value);
  }

}
