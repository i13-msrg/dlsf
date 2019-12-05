package dev.salis.dlsf.bitcoin.node.mempool;

import dev.salis.dlsf.bitcoin.data.Tx;
import dev.salis.dlsf.bitcoin.data.TxIn;
import dev.salis.dlsf.bitcoin.data.TxOut;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Bitcoin memory pool implementation that is managed by full nodes.
 */
public class MemPool {

  private Set<TxIn> usedTxInSet = new HashSet<>();
  private Map<String, TxWithFee> verifiedTxMap = new HashMap<>();
  private Map<String, Tx> orphanTxMap = new HashMap<>();

  public Map<String, TxWithFee> getVerifiedTxMap() {
    return verifiedTxMap;
  }

  public Map<String, Tx> getOrphanTxMap() {
    return orphanTxMap;
  }

  public boolean contains(String txHash) {
    return this.verifiedTxMap.containsKey(txHash) || this.orphanTxMap.containsKey(txHash);
  }

  public Tx get(String txHash) {
    Tx tx = null;
    tx = this.verifiedTxMap.get(txHash);
    if (tx != null) {
      return tx;
    }
    return this.orphanTxMap.get(txHash);
  }

  public boolean isUnspent(TxIn txIn) {
    return !this.usedTxInSet.contains(txIn);
  }

  public void removeSpenders(TxIn txIn) {}

  public void addVerifiedTx(Tx tx, Double fee) {
    TxWithFee txWithFee = new TxWithFee(tx, fee);
    this.verifiedTxMap.put(txWithFee.getHash(), txWithFee);
    this.usedTxInSet.addAll(tx.getInputList());
  }

  public void removeVerifiedTx(String txHash) {
    TxWithFee txWithFee = this.verifiedTxMap.remove(txHash);
    if (txWithFee == null) {
      return;
    }
    this.usedTxInSet.removeAll(txWithFee.getInputList());
  }

  public void addOrphanTx(Tx tx) {
    this.orphanTxMap.put(tx.getHash(), tx);
  }

  public void removeOrphanTx(String txHash) {
    this.orphanTxMap.remove(txHash);
  }

  public TxOut findPrevTxOut(TxIn txIn) {
    TxWithFee prevVerifiedTxWithFee = this.verifiedTxMap.get(txIn.getPrevTxHash());
    if (prevVerifiedTxWithFee == null) {
      return null;
    }
    try {
      return prevVerifiedTxWithFee.getOutputList().get(txIn.getPrevTxOutIndex());
    } catch (IndexOutOfBoundsException ex) {
      return null;
    }
  }

  /**
   * Find orphan transactions that use the provided transaction hash.
   *
   * @param txHash provided tx hash.
   * @return collection of orphan transactions that use the provided tx hash as input
   */
  public Collection<Tx> findDependentOrphanTxs(String txHash) {
    return this.orphanTxMap.values().stream()
        .filter(
            orphanTx ->
                orphanTx.getInputList().stream()
                    .anyMatch(orphanTxIn -> orphanTxIn.getPrevTxHash().equals(txHash)))
        .collect(Collectors.toList());
  }

  /**
   * Find verified transactions that use the provided transaction hash.
   *
   * @param txHash provided tx hash.
   * @return collection of verified transactions that use the provided tx hash as input
   */
  public Collection<Tx> findDependentVerifiedTxs(String txHash) {
    return this.verifiedTxMap.values().stream()
        .filter(
            tx -> tx.getInputList().stream().anyMatch(txIn -> txIn.getPrevTxHash().equals(txHash)))
        .collect(Collectors.toList());
  }

  /**
   * Removes all verified and orphan transactions that use the provided tx input
   * @param checkTxIn provided tx input */
  public void removeTxsThatHasInput(TxIn checkTxIn) {
    this.verifiedTxMap
        .values()
        .removeIf(tx -> tx.getInputList().stream().anyMatch(checkTxIn::equals));
    this.orphanTxMap
        .values()
        .removeIf(tx -> tx.getInputList().stream().anyMatch(checkTxIn::equals));
  }
}
