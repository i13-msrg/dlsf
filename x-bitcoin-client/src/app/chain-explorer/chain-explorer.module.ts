import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ChainExplorerComponent} from './chain-explorer.component';
import {BlockExplorerComponent} from './block-explorer/block-explorer.component';
import {TransactionExplorerComponent} from './transaction-explorer/transaction-explorer.component';
import {BlockListExplorerComponent} from './block-list-explorer/block-list-explorer.component';
import {MaterialModule} from "../material/material.module";
import {FormsModule} from "@angular/forms";


@NgModule({
  declarations: [
    ChainExplorerComponent,
    BlockExplorerComponent,
    TransactionExplorerComponent,
    BlockListExplorerComponent
  ],
  exports: [
    ChainExplorerComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    MaterialModule,
  ]
})
export class ChainExplorerModule {
}
