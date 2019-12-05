package dev.salis.dlsf.bitcoin.node.chainstate;

import dev.salis.dlsf.bitcoin.data.Tx;
import dev.salis.dlsf.bitcoin.data.TxIn;
import dev.salis.dlsf.bitcoin.data.TxOut;
import dev.salis.dlsf.bitcoin.node.exceptions.TxValidationException;
import dev.salis.dlsf.bitcoin.node.exceptions.TxVerificationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Bitcoin Chain State implementation that keeps all the confirmed transactions and unspent
 * transactions on the chain.
 */
public class ChainState {

  private Map<String, Tx> transactions = new HashMap<>();
  private Set<UTxO> UTXOSet = new HashSet<>();

  public ChainState(Collection<Tx> genesisTransactions) {
    this.addTxs(genesisTransactions);
  }

  public boolean containsTx(String txHash) {
    return this.transactions.containsKey(txHash);
  }

  public Tx getTx(String hash) {
    return this.transactions.get(hash);
  }

  public Map<String, Tx> getTransactions() {
    return transactions;
  }

  public void addTxs(Collection<Tx> transactions) {
    for (Tx tx : transactions) {
      this.transactions.put(tx.getHash(), tx);
      // remove used UTXOs
      for (TxIn txIn : tx.getInputList()) {
        UTXOSet.remove(new UTxO(txIn.getPrevTxHash(), txIn.getPrevTxOutIndex()));
      }
      // add new UTXOs
      for (int i = 0; i < tx.getOutputList().size(); i++) {
        UTXOSet.add(new UTxO(tx.getHash(), i));
      }
    }
  }

  public void removeTxs(Collection<Tx> transactions) {
    for (Tx tx : transactions) {
      this.transactions.remove(tx.getHash());
      // add back used UTXOs
      for (TxIn txIn : tx.getInputList()) {
        UTXOSet.add(new UTxO(txIn.getPrevTxHash(), txIn.getPrevTxOutIndex()));
      }
      // remove TxOuts
      for (int i = 0; i < tx.getOutputList().size(); i++) {
        UTXOSet.remove(new UTxO(tx.getHash(), i));
      }
    }
  }

  /**
   * Find the tx output that uses this tx input
   *
   * @param txIn provided tx input
   * @return found tx output
   */
  public TxOut findPrevTxOut(TxIn txIn) {
    Tx prevTx = this.transactions.get(txIn.getPrevTxHash());
    if (prevTx == null) {
      return null;
    }
    try {
      return prevTx.getOutputList().get(txIn.getPrevTxOutIndex());
    } catch (IndexOutOfBoundsException ex) {
      return null;
    }
  }

  /**
   * check if the provided tx input is unspent.
   *
   * @param txIn provided tx input
   * @return indicates whether the provided tx input it is unspent.
   */
  public boolean isUnspent(TxIn txIn) {
    return UTXOSet.contains(new UTxO(txIn.getPrevTxHash(), txIn.getPrevTxOutIndex()));
  }

  /**
   * Verifies and calculates total fee of a transaction using the current state of the chain.
   *
   * @param tx provided transaction.
   * @return total fee.
   */
  public Double verifyAndCalculateTxFee(Tx tx)
      throws TxVerificationException, TxValidationException {
    if (tx.getInputList().isEmpty()) {
      throw new TxValidationException("Input list is empty");
    }
    if (tx.getOutputList().isEmpty()) {
      throw new TxValidationException("Output list is empty");
    }
    List<TxOut> consumedTxOutList = new ArrayList<>(tx.getInputList().size());
    Set<TxIn> consumedTxInList = new HashSet<>(tx.getInputList().size());
    for (TxIn txIn : tx.getInputList()) {
      // check if TxIn is used within the Tx input list already
      if (!consumedTxInList.add(txIn)) {
        throw new TxValidationException("Tx contains duplicate TxIns");
      }
      // find txOut
      TxOut txOut = this.findPrevTxOut(txIn);
      if (txOut == null) {
        throw new TxVerificationException("One of TxIn refers to unknown TxOut");
      }
      consumedTxOutList.add(txOut);
    }
    // compare spent and used amounts
    Double totalSpent =
        tx.getOutputList().stream()
            .reduce(0.0, (acc, txOut) -> acc + txOut.getValue(), Double::sum);
    Double totalConsumed =
        consumedTxOutList.stream().reduce(0.0, (acc, txOut) -> acc + txOut.getValue(), Double::sum);
    double fee = totalConsumed - totalSpent;
    if (fee < 0.0) {
      throw new TxValidationException("Tx output total is greater than input total");
    }
    return fee;
  }
}
