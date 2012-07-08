package infovis.ctrl;

import infovis.data.BusStation;
import infovis.data.BusTime;
import infovis.layout.Layouts;

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
   * @param ffwMode Whether we are currently in the fast forward mode.
   */
  void setStartTime(BusTime time, boolean ffwMode);

  /**
   * Sets the time needed to change bus lines.
   * 
   * @param minutes The time to change lines in minutes.
   */
  void setChangeTime(int minutes);

  /** Sets the focus on the current selected bus station. */
  void focusStation();

  /** Refreshes the display. */
  void refresh();

  /**
   * Signals an undefined change.
   * 
   * @param ctrl The controller to identify the changes.
   */
  void undefinedChange(Controller ctrl);

  /**
   * Signals that the fast forward mode has changed.
   * 
   * @param fastForwardMode Whether we are currently in the fast forward mode.
   * @param fastForwardMinutes The fast forward step size in minutes.
   */
  void fastForwardChange(boolean fastForwardMode, int fastForwardMinutes);

  /**
   * Overwrites the displayed time with the given value. The time must not
   * affect the actual start time.
   * 
   * @param time The new value.
   * @param blink Whether the colon should be displayed.
   */
  void overwriteDisplayedTime(BusTime time, boolean blink);

  /**
   * Sets the currently used layout.
   * 
   * @param layout The layout.
   */
  void setLayout(Layouts layout);

}
