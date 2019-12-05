package dev.salis.dlsf.core.pod;

import java.io.Serializable;

public abstract class AbstractPodProtocol {

  public interface Message extends Serializable {

  }

  public static final class Start implements Message {}
}
