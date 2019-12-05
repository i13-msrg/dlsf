import {WorkerStateModel} from "./worker-state.model";

export interface WorkerModel {
  name: string;
  state: WorkerStateModel;
}