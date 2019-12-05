package dev.salis.dlsf.bitcoin.node.blocktree;

class BlockTreeEntity {

  private String blockHash;
  private String prevBlockHash;
  private int height;

  public BlockTreeEntity(String blockHash, String prevBlockHash) {
    this.blockHash = blockHash;
    this.prevBlockHash = prevBlockHash;
  }

  public String getBlockHash() {
    return blockHash;
  }

  public String getPrevBlockHash() {
    return prevBlockHash;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }
}
