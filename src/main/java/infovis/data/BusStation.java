package infovis.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A {@link BusStation} contains informations about bus stations in the traffic
 * network.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class BusStation {

  /** The name of the bus station. */
  private final String name;

  /** The (non-negative) unique id of the bus station. */
  private final int id;

  /** A sorted list of all bus edges starting with the earliest edge (00:00). */
  private final List<BusEdge> edges;

  /** The bus manager. */
  private final BusStationManager manager;

  /**
   * Creates a bus station.
   * 
   * @param manager The manager.
   * @param name The name.
   * @param id The id, has to be non-negative.
   * @param x The x position.
   * @param y The y position.
   * @param abstractX The x position on the abstract map.
   * @param abstractY The y position on the abstract map.
   * @param edges sorted list of edges
   */
  BusStation(final BusStationManager manager, final String name, final int id,
      final double x, final double y, final double abstractX, final double abstractY,
      final List<BusEdge> edges) {
    this.manager = manager;
    this.name = name;
    this.id = id;
    this.x = x;
    this.y = y;
    this.abstractX = abstractX;
    this.abstractY = abstractY;
    this.edges = edges;
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
      int curr = first;
      @Override
      public Iterator<BusEdge> iterator() {
        return new Iterator<BusEdge>() {

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

    int low = 0, high = edges.size() - 1;
    while(low <= high) {
      final int mid = (low + high) >>> 1;
    final BusEdge midVal = edges.get(mid);
    if(midVal.getStart().compareTo(start) < 0) {
      low = mid + 1;
    } else {
      high = mid - 1;
    }
    }
    return low % edges.size();
  }

  /**
   * A neighbor is a bus station with lines that connect to the given station.
   * 
   * @author Joschi <josua.krause@googlemail.com>
   */
  public static final class Neighbor {

    /**
     * The neighboring station.
     */
    public final BusStation station;

    /**
     * The lines that connect to the neighbor.
     */
    public final BusLine[] lines;

    /**
     * Creates a new neighbor.
     * 
     * @param station The station.
     * @param lines The lines.
     */
    public Neighbor(final BusStation station, final BusLine[] lines) {
      this.station = station;
      this.lines = lines;
    }

  }

  /**
   * The cached neighbors of this node.
   */
  private Neighbor[] neighbors;

  /**
   * Returns all neighbors of this node.
   * 
   * @return The neighbors.
   */
  public Neighbor[] getNeighbors() {
    if(neighbors == null) {
      final Map<BusStation, Set<BusLine>> acc = new HashMap<BusStation, Set<BusLine>>();
      for(final BusEdge edge : edges) {
        final BusStation to = edge.getTo();
        final BusLine line = edge.getLine();
        if(!acc.containsKey(to)) {
          acc.put(to, new HashSet<BusLine>());
        }
        acc.get(to).add(line);
      }
      final Neighbor[] res = new Neighbor[acc.size()];
      int i = 0;
      for(final Entry<BusStation, Set<BusLine>> e : acc.entrySet()) {
        final Set<BusLine> lines = e.getValue();
        res[i++] = new Neighbor(e.getKey(), lines.toArray(new BusLine[lines.size()]));
      }
      neighbors = res;
    }
    return neighbors;
  }

  /**
   * Getter.
   * 
   * @return The maximum degree of this station.
   */
  public int getMaxDegree() {
    int max = 0;
    for(final Neighbor edge : getNeighbors()) {
      max = Math.max(max, edge.lines.length);
    }
    return max;
  }

  /**
   * The default x coordinate for this bus station.
   */
  private final double x;

  /**
   * The default y coordinate for this bus station.
   */
  private final double y;

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
   * @return The default y coordinate for this bus station.
   */
  public double getDefaultY() {
    return y;
  }

  /**
   * The x coordinate for this bus station on the abstract map.
   */
  private final double abstractX;

  /**
   * The y coordinate for this bus station on the abstract map.
   */
  private final double abstractY;

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

  /**
   * Getter.
   * 
   * @return this station's {@link BusStationManager}
   */
  public BusStationManager getManager() {
    return manager;
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
}
