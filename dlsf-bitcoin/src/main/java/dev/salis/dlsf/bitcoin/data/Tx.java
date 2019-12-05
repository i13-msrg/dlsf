package dev.salis.dlsf.bitcoin.data;

import java.io.Serializable;
import java.util.List;

/**
 * Implementation of Bitcoin Transaction
 */
public class Tx implements Serializable {

  private String hash;
  private List<TxIn> inputList;
  private List<TxOut> outputList;

  public Tx() {
  }

  public Tx(String hash, List<TxIn> inputList, List<TxOut> outputList) {
    this.hash = hash;
    this.inputList = inputList;
    this.outputList = outputList;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public List<TxIn> getInputList() {
    return inputList;
  }

  public void setInputList(List<TxIn> inputList) {
    this.inputList = inputList;
  }

  public List<TxOut> getOutputList() {
    return outputList;
  }

  public void setOutputList(List<TxOut> outputList) {
    this.outputList = outputList;
  }
}
