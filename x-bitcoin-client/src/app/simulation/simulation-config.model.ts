export interface SimulationConfigModel {
  numOfNodesPerPod: number;
  maxBlockHeight: number;
  miningDifficulty: number;
  mineAttemptIntervalInMillis: number;
  createTxIntervalInMillis: number;
  blockReward: number;
  genesisBlockTxOutForEachNode: number;
  networkTopologySeed: number;
  maxInboundConnections: number;
  maxOutboundConnections: number;
  statsUpdateIntervalInMillis: number;
  blockSizeLimit: number;
}