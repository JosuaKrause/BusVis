package infovis.ctrl;

import infovis.data.BusStation;
import infovis.data.BusStationManager;
import infovis.data.BusTime;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

/**
 * The controller of the visualizations.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 *
 */
public class Controller {

  /**
   * The list of active visualizations.
   */
  private final List<BusVisualization> vis = new ArrayList<BusVisualization>();

  /**
   * The bus station manager.
   */
  private final BusStationManager manager;

  /**
   * The frame.
   */
  private final JFrame frame;

  /**
   * The currently selected bus station.
   */
  private BusStation curSelection;

  /**
   * The current start time.
   */
  private BusTime curStartTime = new BusTime(12, 0);

  /**
   * The current change time.
   */
  private int curChangeTime = 1;

  /**
   * Creates a new controller.
   * 
   * @param manager The manager.
   * @param frame The frame.
   */
  public Controller(final BusStationManager manager, final JFrame frame) {
    this.manager = manager;
    this.frame = frame;
  }

  /**
   * Getter.
   * 
   * @return The resource path.
   */
  public String getResourcePath() {
    return manager.getPath();
  }

  /**
   * Sets the window title.
   * 
   * @param title The title or <code>null</code>.
   */
  public void setTitle(final String title) {
    final StringBuilder sb = new StringBuilder("BusVis");
    if(curSelection != null) {
      sb.append(" - ");
      sb.append(curSelection.getName());
      sb.append(" at ");
      sb.append(curStartTime.pretty());
      sb.append(" with ");
      sb.append(BusTime.minutesToString(curChangeTime));
      sb.append(" change");
    }
    if(title != null) {
      sb.append(" - (");
      sb.append(title);
      sb.append(")");
    }
    frame.setTitle(sb.toString());
  }

  /**
   * Quits the application.
   */
  public void quit() {
    frame.dispose();
  }

  /**
   * Sets the maximum time window.
   * 
   * @param hours The maximal number of hours.
   */
  public void setMaxTimeHours(final int hours) {
    manager.setMaxTimeHours(hours);
    for(final BusVisualization v : vis) {
      v.undefinedChange(this);
    }
  }

  /**
   * Getter.
   * 
   * @return The maximal number of hours a route can take.
   */
  public int getMaxTimeHours() {
    return manager.getMaxTimeHours();
  }

  /**
   * Selects a bus station.
   * 
   * @param station The station.
   */
  public void selectStation(final BusStation station) {
    curSelection = station;
    for(final BusVisualization v : vis) {
      v.selectBusStation(station);
    }
    setTitle(null);
  }

  /**
   * Getter.
   * 
   * @return The currently selected station.
   */
  public BusStation getSelectedStation() {
    return curSelection;
  }

  /**
   * Setter.
   * 
   * @param start The starting time.
   */
  public void setTime(final BusTime start) {
    curStartTime = start;
    for(final BusVisualization v : vis) {
      v.setStartTime(start);
    }
    setTitle(null);
  }

  /**
   * Getter.
   * 
   * @return The current starting time.
   */
  public BusTime getTime() {
    return curStartTime;
  }

  /**
   * Setter.
   * 
   * @param time The line changing time in minutes.
   */
  public void setChangeTime(final int time) {
    curChangeTime = time;
    for(final BusVisualization v : vis) {
      v.setChangeTime(time);
    }
    setTitle(null);
  }

  /**
   * Getter.
   * 
   * @return The line changing time in minutes.
   */
  public int getChangeTime() {
    return curChangeTime;
  }

  /**
   * Adds a bus visualization.
   * 
   * @param v The visualization.
   */
  public void addBusVisualization(final BusVisualization v) {
    if(v == null) throw new NullPointerException("v");
    if(vis.contains(v)) throw new IllegalStateException("visualization already added");
    vis.add(v);
    v.setChangeTime(curChangeTime);
    v.setStartTime(curStartTime);
    v.selectBusStation(curSelection);
    v.undefinedChange(this);
  }

  /**
   * Removes a visualization.
   * 
   * @param v The visualization.
   */
  public void removeBusVisualization(final BusVisualization v) {
    vis.remove(v);
  }

  /**
   * Getter.
   * 
   * @return All bus stations.
   */
  public Iterable<BusStation> getStations() {
    return manager.getStations();
  }

  /**
   * Getter.
   * 
   * @return All bus stations.
   */
  public BusStation[] getAllStations() {
    final List<BusStation> res = new ArrayList<BusStation>();
    for(final BusStation s : manager.getStations()) {
      res.add(s);
    }
    return res.toArray(new BusStation[res.size()]);
  }

  /**
   * Sets the focus on the current selected bus station.
   */
  public void focusStation() {
    for(final BusVisualization v : vis) {
      v.focusStation();
    }
  }

}
