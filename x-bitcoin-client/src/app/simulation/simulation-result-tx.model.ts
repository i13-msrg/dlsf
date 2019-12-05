import {StatsUpdateModel} from "./stats-update.model";
import {NetworkStatsModel} from "./network-stats.model";

export interface SimulationResultTxModel {
  stats: StatsUpdateModel;
  networkStats: NetworkStatsModel;
}