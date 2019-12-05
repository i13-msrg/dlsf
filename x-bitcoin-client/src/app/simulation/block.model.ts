import {TxModel} from "./tx.model";

export interface BlockModel {
  hash: string;
  prevBlockHash: string;
  time: Date;
  minerId: string;
  transactions: TxModel[];
}