package dev.salis.dlsf.boot;

/**
 * Types of nodes of a simulation system. It can be either MASTER and WORKER for clustered mode or
 * STANDALONE mode.
 */
public enum NodeType {
  STANDALONE,
  MASTER,
  WORKER,
}
