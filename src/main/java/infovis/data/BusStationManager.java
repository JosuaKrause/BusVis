package infovis.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * The {@link BusStationManager} takes care of {@link BusStation}s.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class BusStationManager {

  /** A fast lookup table. BusStation ids are generally dense. */
  private final BusStation[] fastLookup;

  /** An unmodifiable collection of all bus stations. */
  private final Collection<BusStation> fastIterate;

  /** The resource path. */
  private final String path;

  /**
   * Constructor taking the map of bus stations.
   * 
   * @param stations bus station map
   * @param path path of the CSV data, possibly <code>null</code>
   */
  BusStationManager(final Collection<BusStation> stations, final String path) {
    fastIterate = Collections.unmodifiableCollection(new ArrayList<BusStation>(stations));
    int maxId = 0;
    for(final BusStation b: fastIterate) {
      final int id = b.getId();
      if(id > maxId) {
        maxId = id;
      }
    }
    fastLookup = new BusStation[maxId + 1];
    for(final BusStation b: fastIterate) {
      fastLookup[b.getId()] = b;
    }
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
    return fastLookup[id];
  }

  /**
   * Getter.
   * 
   * @return All registered {@link BusStation}s.
   */
  public Collection<BusStation> getStations() {
    return fastIterate;
  }

  /**
   * Getter.
   * 
   * @return The maximal bus station id.
   */
  public int maxId() {
    return fastLookup.length - 1;
  }

  /**
   * Getter.
   * 
   * @return The number of bus stations.
   */
  public int count() {
    return fastIterate.size();
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
