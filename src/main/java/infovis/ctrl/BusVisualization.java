package infovis.ctrl;

import infovis.data.BusStation;
import infovis.data.BusTime;
import infovis.embed.Embedders;

/**
 * A bus visualization.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface BusVisualization {

  /**
   * Selects a certain bus station.
   * 
   * @param station The selected station.
   */
  void selectBusStation(BusStation station);

  /**
   * Sets the routing start time.
   * 
   * @param time The routing time or <code>null</code> if the time should be
   *          now.
   */
  void setStartTime(BusTime time);

  /**
   * Sets the time needed to change bus lines.
   * 
   * @param minutes The time to change lines in minutes.
   */
  void setChangeTime(int minutes);

  /**
   * Sets the focus on the current selected bus station.
   */
  void focusStation();

  /**
   * Signals an undefined change.
   * 
   * @param ctrl The controller to identify the changes.
   */
  void undefinedChange(Controller ctrl);

  /**
   * Overwrites the displayed time with the given value. The time must not
   * affect the actual start time.
   * 
   * @param time The new value.
   * @param blink Whether the colon should be displayed.
   */
  void overwriteDisplayedTime(BusTime time, boolean blink);

  /**
   * Sets the currently used embedder technique.
   * 
   * @param embed The embedder technique.
   */
  void setEmbedder(Embedders embed);

}
