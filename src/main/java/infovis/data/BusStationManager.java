package infovis.data;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * The {@link BusStationManager} takes care of {@link BusStation}s.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class BusStationManager {

  /** The backing map for bus station ids. */
  private final Map<Integer, BusStation> stations;

  /** The resource path. */
  private final String path;

  /**
   * Constructor taking the map of bus stations.
   * 
   * @param stations bus station map
   * @param path path of the CSV data, possibly <code>null</code>
   */
  BusStationManager(final Map<Integer, BusStation> stations, final String path) {
    this.stations = stations;
    this.path = path;
  }

  /**
   * Getter.
   * 
   * @return The resource path.
   */
  public String getPath() {
    return path;
  }

  /**
   * Getter.
   * 
   * @param id The id of a bus station.
   * @return The bus station with the given id.
   */
  public BusStation getForId(final int id) {
    return stations.get(id);
  }

  /**
   * Getter.
   * 
   * @return All registered {@link BusStation}s.
   */
  public Collection<BusStation> getStations() {
    return Collections.unmodifiableCollection(stations.values());
  }

  /**
   * The maximum amount of time a route can take.
   */
  private int maxTimeHours = 24;

  /**
   * Getter.
   * 
   * @return The maximum amount of time a route can take in hours. This may not
   *         be exact. The value limits the starting time of an edge.
   */
  public int getMaxTimeHours() {
    return maxTimeHours;
  }

  /**
   * Setter.
   * 
   * @param maxTimeHours Sets the maximum amount of time a route can take in
   *          hours.
   */
  public void setMaxTimeHours(final int maxTimeHours) {
    if(maxTimeHours < 0 || maxTimeHours > 24) throw new IllegalArgumentException(
        "max time out of bounds " + maxTimeHours);
    this.maxTimeHours = maxTimeHours;
  }

}
