package dev.salis.dlsf.bitcoin.node.blocktree;

import java.util.ArrayList;
import java.util.List;

/**
 * Data structure that contains the difference between two nodes on the block tree. For instance,
 * one can navigate from one node to other within the tree by first removing the removed nodes and
 * then by adding the added nodes in this data structure.
 */
public class ChainDiff {

  private List<String> removed = new ArrayList<>();
  private List<String> added = new ArrayList<>();

  public List<String> getRemoved() {
    return removed;
  }

  void appendRemoved(String blockHash) {
    this.removed.add(blockHash);
  }

  public List<String> getAdded() {
    return added;
  }

  void prependAdded(String blockHash) {
    this.added.add(0, blockHash);
  }

  public boolean isEmpty() {
    return this.added.isEmpty() && this.removed.isEmpty();
  }
}
