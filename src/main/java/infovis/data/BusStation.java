package infovis.data;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A {@link BusStation} contains informations about bus stations in the traffic
 * network.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class BusStation implements Comparable<BusStation> {

  /** The name of the bus station. */
  private final String name;

  /** The (non-negative) unique id of the bus station. */
  private final int id;

  /** The default x coordinate for this bus station. */
  private final double x;

  /** The default y coordinate for this bus station. */
  private final double y;

  /** The x coordinate for this bus station on the abstract map. */
  private final double abstractX;

  /** The y coordinate for this bus station on the abstract map. */
  private final double abstractY;

  /** A sorted list of all bus edges starting with the earliest edge (00:00). */
  private final List<BusEdge> edges;

  /** Walking distances to the other stations. */
  private final List<Integer> walkingDists;

  /**
   * Creates a bus station.
   * 
   * @param name The name.
   * @param id The id, has to be non-negative.
   * @param latitude The latitude.
   * @param longitude The longitude.
   * @param abstractX The x position on the abstract map.
   * @param abstractY The y position on the abstract map.
   * @param edges sorted list of edges
   * @param walkingDists walking distances
   * @param scale The scaling of geographic positions.
   */
  BusStation(final String name, final int id, final double latitude,
      final double longitude, final double abstractX, final double abstractY,
      final List<BusEdge> edges, final List<Integer> walkingDists, final double scale) {
    this.name = name;
    this.id = id;
    this.abstractX = abstractX;
    this.abstractY = abstractY;
    this.edges = edges;
    this.walkingDists = walkingDists;
    this.scale = scale;
    x = scaleAngle(longitude, false);
    y = scaleAngle(latitude, true);
  }

  /** The value to scale angles. */
  private final double scale;

  /**
   * Scales the given angle to a bigger representable size.
   * 
   * @param angle The angle in degrees.
   * @param lat Whether the angle is latitude or longitude.
   * @return A coordinate.
   */
  private double scaleAngle(final double angle, final boolean lat) {
    return (angle + 180) * scale * (lat ? -1 : 1);
  }

  /**
   * Unscales the given coordinate back to the original angle.
   * 
   * @param coord The coordinate.
   * @param lat Whether the angle is latitude or longitude.
   * @return The corresponding angle.
   */
  private double unscaleAngle(final double coord, final boolean lat) {
    return coord / scale * (lat ? -1 : 1) - 180;
  }

  /**
   * Getter.
   * 
   * @return The name of the bus station.
   */
  public String getName() {
    return name;
  }

  /**
   * Getter.
   * 
   * @return The id of the bus station.
   */
  public int getId() {
    return id;
  }

  /**
   * Getter.
   * 
   * @return Returns all edges associated with this bus station.
   */
  public Collection<BusEdge> getEdges() {
    return Collections.unmodifiableList(edges);
  }

  /**
   * Returns all edges associated with this bus station, starting with the edge
   * earliest after the given time. The last edge is the edge before the given
   * time.
   * 
   * @param from The time of the first returned edge.
   * @return An iterable going through the set of edges.
   */
  public Iterable<BusEdge> getEdges(final BusTime from) {
    final int first = binarySearch(from);
    final List<BusEdge> edges = this.edges;
    return new Iterable<BusEdge>() {

      @Override
      public Iterator<BusEdge> iterator() {
        return new Iterator<BusEdge>() {

          /** The current index position. */
          private int curr = first;

          @Override
          public boolean hasNext() {
            return curr != -1;
          }

          @Override
          public BusEdge next() {
            if(curr == -1) return null;
            final BusEdge next = edges.get(curr);
            curr = (curr + 1) % edges.size();
            if(curr == first) {
              curr = -1;
            }
            return next;
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }

        };
      }

    };
  }

  /**
   * Finds the first {@link BusEdge} not starting before <code>start</code>.
   * 
   * @param start start time
   * @return position if such an edge exists, <code>-1</code> otherwise
   */
  private int binarySearch(final BusTime start) {
    final int size = edges.size();
    if(size == 0) return -1;

    if(size < 8) {
      for(int i = 0; i < size; i++) {
        if(edges.get(i).getStart().compareTo(start) >= 0) return i;
      }
      return 0;
    }

    int low = 0, high = size - 1;
    while(low <= high) {
      final int mid = (low + high) >>> 1;
    final BusEdge midVal = edges.get(mid);
    if(midVal.getStart().compareTo(start) < 0) {
      low = mid + 1;
    } else {
      high = mid - 1;
    }
    }
    return low % size;
  }

  /**
   * Getter.
   * 
   * @return The default x coordinate for this bus station.
   */
  public double getDefaultX() {
    return x;
  }

  /**
   * Getter.
   * 
   * @return The longitude of this bus station.
   */
  public double getLongitude() {
    return unscaleAngle(x, false);
  }

  /**
   * Getter.
   * 
   * @return The default y coordinate for this bus station.
   */
  public double getDefaultY() {
    return y;
  }

  /**
   * Getter.
   * 
   * @return The latitude of this bus station.
   */
  public double getLatitude() {
    return unscaleAngle(y, true);
  }

  /**
   * Number of seconds it takes to walk from this station to the given one.
   * 
   * @param other other station
   * @return distance in seconds if known, {@code -1} otherwise
   */
  public int walkingSeconds(final BusStation other) {
    final int oid = other.getId();
    return walkingDists.size() <= oid ? -1 : walkingDists.get(oid);
  }

  /**
   * Getter.
   * 
   * @return Whether this bus station has an abstract position.
   */
  public boolean hasAbstractPosition() {
    return !Double.isNaN(getAbstractX());
  }

  /**
   * Getter.
   * 
   * @return The x coordinate for this bus station on the abstract map.
   */
  public double getAbstractX() {
    return abstractX;
  }

  /**
   * Getter.
   * 
   * @return The y coordinate for this bus station on the abstact map.
   */
  public double getAbstractY() {
    return abstractY;
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof BusStation && id == ((BusStation) obj).id;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public String toString() {
    return String.format("%s[%s, %d]", getClass().getSimpleName(), name, id);
  }

  @Override
  public int compareTo(final BusStation o) {
    return id - o.id;
  }

}
