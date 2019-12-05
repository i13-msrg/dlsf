import {TxInModel} from "./tx-in.model";
import {TxOutModel} from "./tx-out.model";

export interface TxModel {
  hash: string;
  inputList: TxInModel[];
  outputList: TxOutModel[];
}