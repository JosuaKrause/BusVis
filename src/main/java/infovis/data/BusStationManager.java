package infovis.data;

import infovis.util.Objects;
import infovis.util.Resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * The {@link BusStationManager} takes care of {@link BusStation}s.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class BusStationManager implements BusStationEnumerator {

  /** A fast lookup table. BusStation ids are dense. */
  private final BusStation[] fastLookup;

  /** An unmodifiable collection of all bus stations. */
  private final Collection<BusStation> fastIterate;

  /** The overview resource URL. */
  private final Resource overview;

  /** The edge matrix. */
  private final EdgeMatrix matrix;

  /**
   * Constructor taking the map of bus stations.
   * 
   * @param stations bus station map
   * @param overview overview resource, possibly <code>null</code>
   * @param matrix The edge matrix.
   */
  BusStationManager(final Collection<BusStation> stations,
      final Resource overview, final EdgeMatrix matrix) {
    this.overview = overview;
    this.matrix = Objects.requireNonNull(matrix);
    fastIterate = Collections.unmodifiableCollection(
        new ArrayList<BusStation>(Objects.requireNonNull(stations)));
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
  }

  /**
   * Getter.
   * 
   * @return The edge matrix of this manager.
   */
  public EdgeMatrix getEdgeMatrix() {
    return matrix;
  }

  /**
   * Getter.
   * 
   * @return The overview resource or <code>null</code> if the file does not
   *         exist.
   */
  public Resource getOverview() {
    return overview;
  }

  @Override
  public BusStation getForId(final int id) {
    return fastLookup[id];
  }

  @Override
  public Collection<BusStation> getStations() {
    return fastIterate;
  }

  @Override
  public int maxId() {
    return fastLookup.length - 1;
  }

  /** The maximum amount of time a route can take. */
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
