package dev.salis.dlsf.bitcoin.node;

import dev.salis.dlsf.bitcoin.data.Block;
import dev.salis.dlsf.bitcoin.data.Tx;
import dev.salis.dlsf.bitcoin.data.TxIn;
import dev.salis.dlsf.bitcoin.data.TxOut;
import dev.salis.dlsf.bitcoin.node.blocktree.BlockTree;
import dev.salis.dlsf.bitcoin.node.blocktree.ChainDiff;
import dev.salis.dlsf.bitcoin.node.chainstate.ChainState;
import dev.salis.dlsf.bitcoin.node.exceptions.TxRejectedException;
import dev.salis.dlsf.bitcoin.node.exceptions.TxValidationException;
import dev.salis.dlsf.bitcoin.node.exceptions.TxVerificationException;
import dev.salis.dlsf.bitcoin.node.mempool.MemPool;
import dev.salis.dlsf.bitcoin.node.wallet.Wallet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Bitcoin full node implementation.
 */
public class FullNode {

  private Wallet wallet;
  private Map<String, Block> blockMap;
  private BlockTree blockTree;
  private ChainState chainState;
  private MemPool memPool;

  public FullNode(Block genesisBlock, String walletAddress) {
    this.wallet = new Wallet(walletAddress);
    this.blockMap = new HashMap<>();
    this.blockTree = new BlockTree(genesisBlock.getHash());
    this.chainState = new ChainState(genesisBlock.getTransactions());
    this.memPool = new MemPool();
    this.blockMap.put(genesisBlock.getHash(), genesisBlock);
  }

  public Wallet getWallet() {
    return wallet;
  }

  public Map<String, Block> getBlockMap() {
    return blockMap;
  }

  public BlockTree getBlockTree() {
    return blockTree;
  }

  public ChainState getChainState() {
    return chainState;
  }

  public MemPool getMemPool() {
    return memPool;
  }

  /**
   * Add a block to the full node and update states of all the internal components.
   *
   * @param block block to be added
   * @return whether the block should be propagated or not. When true is returned, this means
   *     provided block is newly discovered and should be propagated.
   */
  public boolean receiveBlock(Block block) {
    boolean isAlreadyKnown = this.blockMap.containsKey(block.getHash());
    if (isAlreadyKnown) {
      return false;
    }
    this.blockMap.put(block.getHash(), block);
    ChainDiff diff = this.blockTree.addBlock(block.getHash(), block.getPrevBlockHash());
    if (diff.getAdded().isEmpty() && diff.getRemoved().isEmpty()) {
      // added an orphan block
      return true;
    }
    this.digestActiveChainDiffChanges(diff);
    return true;
  }

  /**
   * Add a transaction to the full node and update states of all the internal components. An
   * exception is thrown to indicate the validity and other properties of the provided transaction.
   * Exceptions include a message indicating the exact reason.
   *
   * @param tx transaction to be added.
   * @throws TxRejectedException transaction is rejected. It can already be in the MemPool or on the
   *     main branch, or one of the inputs are already spent.
   * @throws TxValidationException transaction is not valid based on bitcoin protocol rules.
   * @throws TxVerificationException transaction is not verified, meaning one of the inputs is
   *     unknown.
   */
  public void receiveTx(Tx tx)
      throws TxRejectedException, TxValidationException, TxVerificationException {
    try {
      Double fee = this.verifyAndFindTxFee(tx);
      this.memPool.addVerifiedTx(tx, fee);
    } catch (TxVerificationException e) {
      this.memPool.addOrphanTx(tx);
      throw e;
    }
  }

  private void digestActiveChainDiffChanges(ChainDiff diff) {
    // digest removed blocks
    for (String blockHash : diff.getRemoved()) {
      Block removedBlock = this.blockMap.get(blockHash);
      // update chain state
      this.chainState.removeTxs(removedBlock.getTransactions());
      for (int txIndex = 0; txIndex < removedBlock.getTransactions().size(); txIndex++) {
        Tx tx = removedBlock.getTransactions().get(txIndex);
        // update mempool
        Collection<Tx> dependentVerifiedTxs = this.memPool.findDependentVerifiedTxs(tx.getHash());
        for (Tx dependentVerifiedTx : dependentVerifiedTxs) {
          this.memPool.removeVerifiedTx(dependentVerifiedTx.getHash());
        }
        // update wallet
        if (txIndex != 0) {
          for (TxIn txIn : tx.getInputList()) {
            TxOut prevTxOut = this.chainState.findPrevTxOut(txIn);
            if (this.wallet.getAddress().equals(prevTxOut.getRecipient())) {
              this.wallet.getCandidateTxInSet().add(txIn);
            }
          }
        }
        for (int i = 0; i < tx.getOutputList().size(); i++) {
          if (this.wallet.getAddress().equals(tx.getOutputList().get(i).getRecipient())) {
            this.wallet.getCandidateTxInSet().remove(new TxIn(tx.getHash(), i));
          }
        }
      }
    }
    // digest added blocks
    for (String blockHash : diff.getAdded()) {
      Block addedBlock = this.blockMap.get(blockHash);
      // update chain state
      this.chainState.addTxs(addedBlock.getTransactions());
      // update mempool
      for (int txIndex = 1; txIndex < addedBlock.getTransactions().size(); txIndex++) {
        Tx tx = addedBlock.getTransactions().get(txIndex);
        this.memPool.removeVerifiedTx(tx.getHash());
        this.memPool.removeOrphanTx(tx.getHash());
        // remove mempool transaction that uses one of the inputs
        for (TxIn txIn : tx.getInputList()) {
          this.memPool.removeTxsThatHasInput(txIn);
        }
      }
      for (int txIndex = 0; txIndex < addedBlock.getTransactions().size(); txIndex++) {
        Tx tx = addedBlock.getTransactions().get(txIndex);
        // try to verify dependent orphan transactions
        Collection<Tx> dependentOrphanTxs = this.memPool.findDependentOrphanTxs(tx.getHash());
        for (Tx dependentOrphanTx : dependentOrphanTxs) {
          // try to verify orphan transactions
          this.memPool.removeOrphanTx(dependentOrphanTx.getHash());
          try {
            this.receiveTx(tx);
          } catch (TxRejectedException | TxVerificationException | TxValidationException ignored) {
          }
        }
        // update wallet
        if (txIndex != 0) {
          for (TxIn txIn : tx.getInputList()) {
            TxOut prevTxOut = this.chainState.findPrevTxOut(txIn);
            if (this.wallet.getAddress().equals(prevTxOut.getRecipient())) {
              this.wallet.getCandidateTxInSet().remove(txIn);
            }
          }
        }
        for (int i = 0; i < tx.getOutputList().size(); i++) {
          if (this.wallet.getAddress().equals(tx.getOutputList().get(i).getRecipient())) {
            this.wallet.getCandidateTxInSet().add(new TxIn(tx.getHash(), i));
          }
        }
      }
    }
  }

  private Double verifyAndFindTxFee(Tx tx)
      throws TxRejectedException, TxVerificationException, TxValidationException {
    // Reject if we already have matching tx in the pool, or in a block in the main branch
    if (this.chainState.containsTx(tx.getHash()) || this.memPool.contains(tx.getHash())) {
      throw new TxRejectedException("Tx is already in the main branch or MemPool");
    }
    // checks for double spending
    boolean areAllInputsUnspentInMemPool =
        tx.getInputList().stream().allMatch(this.memPool::isUnspent);
    if (!areAllInputsUnspentInMemPool) {
      throw new TxRejectedException(
          "Tx contains an input which is already used in one of the Txs in the MemPool");
    }
    // For each input, look in the main branch to find the referenced output transaction.
    Double fee = this.chainState.verifyAndCalculateTxFee(tx);

    boolean areAllInputsUnspentInChainState =
        tx.getInputList().stream().allMatch(this.chainState::isUnspent);
    if (!areAllInputsUnspentInChainState) {
      throw new TxRejectedException(
          "Tx contains an input which is already used in one of the Txs in the main branch");
    }
    return fee;
  }
}
