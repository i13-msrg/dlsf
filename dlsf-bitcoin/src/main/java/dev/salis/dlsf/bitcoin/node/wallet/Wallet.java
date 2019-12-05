package dev.salis.dlsf.bitcoin.node.wallet;

import dev.salis.dlsf.bitcoin.data.TxIn;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple implementation of a wallet that includes candidate tx inputs that can be spent in the
 * future.
 */
public class Wallet {

  private String address;
  private Set<TxIn> candidateTxInSet = new HashSet<>();

  public Wallet(String address) {
    this.address = address;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public Set<TxIn> getCandidateTxInSet() {
    return candidateTxInSet;
  }

  public void setCandidateTxInSet(Set<TxIn> candidateTxInSet) {
    this.candidateTxInSet = candidateTxInSet;
  }
}
