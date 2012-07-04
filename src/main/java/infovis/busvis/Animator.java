package infovis.busvis;

import infovis.gui.Refreshable;

/**
 * An animator refreshes a {@link Refreshable} successively.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface Animator {

  /** Forces the next frame to be calculated. */
  void forceNextFrame();

}
