package dev.salis.dlsf.bitcoin.node.mempool;

import dev.salis.dlsf.bitcoin.data.Tx;

public class TxWithFee extends Tx {

  private Double fee;

  public TxWithFee(Tx tx, Double fee) {
    super(tx.getHash(), tx.getInputList(), tx.getOutputList());
    this.fee = fee;
  }

  public Double getFee() {
    return fee;
  }
}
