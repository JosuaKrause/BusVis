package infovis.ctrl;

import infovis.data.BusStation;
import infovis.data.BusTime;

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
   * @param time The routing time.
   */
  void setStartTime(BusTime time);

  /**
   * Sets the time needed to change bus lines.
   * 
   * @param minutes The time to change lines in minutes.
   */
  void setChangeTime(int minutes);

}
