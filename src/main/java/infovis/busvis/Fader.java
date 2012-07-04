package infovis.busvis;

import infovis.data.BusStation;

/**
 * Fades from one station to another.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface Fader {

  /**
   * Sets the next station without starting a fade.
   * 
   * @param station The next station.
   */
  void setPredict(BusStation station);

  /**
   * Starts a fade to the given station.
   * 
   * @param next The station to fade to.
   * @param duration The duration of the fade.
   */
  void initialize(BusStation next, int duration);

  /**
   * Getter.
   * 
   * @return Whether the fader is currently fading.
   */
  boolean inFade();

}
