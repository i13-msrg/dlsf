package dev.salis.dlsf.bitcoin.node.blocktree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A data structure that manages all the blocks that constructs the block tree. Main branch is the
 * active chain which is the longest.
 */
public class BlockTree {

  private BlockTreeEntity mainBranchLeaf;
  private Map<String, BlockTreeEntity> entityMap = new HashMap<>();
  private HashMap<String, BlockTreeEntity> orphanEntities = new HashMap<>();

  public BlockTree(String rootNodeHash) {
    BlockTreeEntity genesisBlock = new BlockTreeEntity(rootNodeHash, null);
    genesisBlock.setHeight(0);
    mainBranchLeaf = genesisBlock;
    entityMap.put(genesisBlock.getBlockHash(), genesisBlock);
  }

  /**
   * @return height of the main chain.
   */
  public int getMainBranchLeafHeight() {
    return mainBranchLeaf.getHeight();
  }

  /** @return hash of the block that has the longest height on the main chain. */
  public String getMainBranchLeaf() {
    return mainBranchLeaf.getBlockHash();
  }

  /** @return hashes of all the nodes ordered from the main branch leaf to the genesis block. */
  public List<String> getMainBranch() {
    if (mainBranchLeaf == null) {
      return new ArrayList<>();
    }
    BlockTreeEntity cursor = mainBranchLeaf;
    List<String> mainBranch = new ArrayList<>(cursor.getHeight());
    do {
      mainBranch.add(0, cursor.getBlockHash());
      cursor = entityMap.get(cursor.getPrevBlockHash());
    } while (cursor.getPrevBlockHash() != null);
    mainBranch.add(0, cursor.getBlockHash());
    return mainBranch;
  }

  /**
   * Checks if the given block hash is in the main branch.
   *
   * @param blockHash given block hash.
   */
  public boolean isInMainBranch(String blockHash) {
    BlockTreeEntity parent = mainBranchLeaf;
    return false;
  }

  /**
   * Returns the height of the provided block hash within the tree.
   *
   * @param blockHash provided block hash.
   * @return length of the provided block hash, or null if it is not part of the tree or unknown
   *     yet.
   */
  public Integer getBlockHeight(String blockHash) {
    BlockTreeEntity entity = this.entityMap.get(blockHash);
    if (entity == null) {
      return null;
    }
    return entity.getHeight();
  }

  /**
   * Add block to the block tree and return the difference between old and new main branches.
   *
   * @param blockHash hash of the block to be added.
   * @param prevBlockHash hash of the block that is the parent of added block hash.
   * @return difference between the old and new main branch leaves. This indicates the changes to
   *     the main branch.
   */
  public ChainDiff addBlock(String blockHash, String prevBlockHash) {
    BlockTreeEntity oldActiveChainLeaf = this.mainBranchLeaf;
    final BlockTreeEntity entity = new BlockTreeEntity(blockHash, prevBlockHash);
    final boolean isAdded = this.addEntity(entity);
    if (!isAdded) {
      orphanEntities.put(entity.getBlockHash(), entity);
      return new ChainDiff();
    }
    this.tryToAddOrphanEntities();
    return findDiff(oldActiveChainLeaf.getBlockHash(), this.mainBranchLeaf.getBlockHash());
  }

  private boolean addEntity(BlockTreeEntity node) {
    final BlockTreeEntity parent = this.entityMap.get(node.getPrevBlockHash());
    if (parent == null) {
      return false;
    }
    node.setHeight(parent.getHeight() + 1);
    entityMap.put(node.getBlockHash(), node);
    if (node.getHeight() > getMainBranchLeafHeight()) {
      this.mainBranchLeaf = node;
    }
    return true;
  }

  private void tryToAddOrphanEntities() {
    Set<BlockTreeEntity> updated = new HashSet<>();
    for (BlockTreeEntity outsideNode : orphanEntities.values()) {
      boolean isAdded = addEntity(outsideNode);
      if (isAdded) {
        updated.add(outsideNode);
      }
    }
    if (updated.isEmpty()) {
      return;
    }
    for (BlockTreeEntity updatedEntity : updated) {
      this.orphanEntities.remove(updatedEntity.getBlockHash());
    }
    tryToAddOrphanEntities();
  }

  /** Find difference between two blocks within the block tree. */
  public ChainDiff findDiff(String originHash, String destinationHash) {
    ChainDiff diff = new ChainDiff();
    BlockTreeEntity originCursor = this.entityMap.get(originHash);
    BlockTreeEntity destinationCursor = this.entityMap.get(destinationHash);
    if (originCursor == null || destinationCursor == null) {
      return null;
    }
    while (originCursor.getHeight() != destinationCursor.getHeight()) {
      if (originCursor.getHeight() > destinationCursor.getHeight()) {
        diff.appendRemoved(originCursor.getBlockHash());
        originCursor = entityMap.get(originCursor.getPrevBlockHash());
      } else {
        diff.prependAdded(destinationCursor.getBlockHash());
        destinationCursor = entityMap.get(destinationCursor.getPrevBlockHash());
      }
    }
    while (!originCursor.getBlockHash().equals(destinationCursor.getBlockHash())) {
      diff.appendRemoved(originCursor.getBlockHash());
      originCursor = entityMap.get(originCursor.getPrevBlockHash());
      diff.prependAdded(destinationCursor.getBlockHash());
      destinationCursor = entityMap.get(destinationCursor.getPrevBlockHash());
    }
    return diff;
  }
}
