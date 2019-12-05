export interface StatsUpdateModel {
  txPerSecond: number;
  blockPerSecond: number;
  confirmedBlockCount: number;
  verifiedBlockCount: number;
  verifiedTransactionCount: number;
  confirmedTransactionCount: number;
}