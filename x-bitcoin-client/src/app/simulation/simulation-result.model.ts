import {BlockMapModel} from "./block-map.model";
import {StatsUpdateModel} from "./stats-update.model";
import {TopologyMapModel} from "./topology-map.model";
import {TotalNumOfConnectionsModel} from "./total-num-of-connections.model";

export interface SimulationResultModel {
  blockMap: BlockMapModel;
  stats: StatsUpdateModel;
  mainChainBlockHashList: string[];
  networkTopology: TopologyMapModel;
  totalNumberOfConnections: TotalNumOfConnectionsModel;
}