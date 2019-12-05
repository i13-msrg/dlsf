package dev.salis.dlsf.bitcoin.data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Implementation of Bitcoin Block
 */
public class Block implements Serializable {

  private String hash;
  private String prevBlockHash;
  private Date time;
  private String minerId;
  private List<Tx> transactions;

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public String getPrevBlockHash() {
    return prevBlockHash;
  }

  public void setPrevBlockHash(String prevBlockHash) {
    this.prevBlockHash = prevBlockHash;
  }

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public String getMinerId() {
    return minerId;
  }

  public void setMinerId(String minerId) {
    this.minerId = minerId;
  }

  public List<Tx> getTransactions() {
    return transactions;
  }

  public void setTransactions(List<Tx> transactions) {
    this.transactions = transactions;
  }
}
