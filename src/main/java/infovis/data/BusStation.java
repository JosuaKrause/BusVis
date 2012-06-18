package infovis.data;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A {@link BusStation} contains informations about bus stations in the traffic
 * network.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class BusStation {

  /**
   * The name of the bus station.
   */
  private final String name;

  /**
   * The (non-negative) unique id of the bus station.
   */
  private final int id;

  /**
   * A sorted set of all bus edges starting with the earliest edge (0 hours 0
   * minutes).
   */
  private final SortedSet<BusEdge> edges = new TreeSet<BusEdge>();

  /**
   * The bus manager.
   */
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
   */
  BusStation(final BusStationManager manager, final String name, final int id,
      final double x, final double y, final double abstractX, final double abstractY) {
    this.manager = manager;
    this.name = name;
    this.id = id;
    this.x = x;
    this.y = y;
    this.abstractX = abstractX;
    this.abstractY = abstractY;
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
   * Adds an edge to this bus station. This method is deprecated,
   * {@link #addEdge(BusLine, int, BusStation, BusTime, BusTime)} should be used
   * instead.
   * 
   * @param line bus line
   * @param dest The destination.
   * @param start The start time.
   * @param end The end time.
   */
  @Deprecated
  public void addEdge(final BusLine line, final BusStation dest, final BusTime start,
      final BusTime end) {
    addEdge(line, -1, dest, start, end);
  }

  /**
   * Adds an edge to this bus station.
   * 
   * @param line bus line
   * @param tourNr tour number, unique per line
   * @param dest The destination.
   * @param start The start time.
   * @param end The end time.
   * @return added edge
   */
  public BusEdge addEdge(final BusLine line, final int tourNr, final BusStation dest,
      final BusTime start, final BusTime end) {
    final BusEdge edge = new BusEdge(line, tourNr, this, dest, start, end);
    edges.add(edge);
    return edge;
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
    final Comparator<BusTime> cmp = from.createRelativeComparator();
    BusEdge start = null;
    for(final BusEdge e : edges) {
      if(start == null) {
        start = e;
        continue;
      }
      if(cmp.compare(e.getStart(), start.getStart()) < 0) {
        // must be smaller to get the first one of a row of simultaneous edges
        start = e;
      }
    }
    if(start == null) // empty set
      return Collections.EMPTY_LIST;
    final BusEdge s = start;
    final SortedSet<BusEdge> e = edges;
    return new Iterable<BusEdge>() {

      @Override
      public Iterator<BusEdge> iterator() {
        return new Iterator<BusEdge>() {

          private boolean pushedBack;

          private BusEdge cur;

          private Iterator<BusEdge> it;

          {
            it = e.tailSet(s).iterator();
            cur = it.next();
            pushedBack = true;
          }

          private BusEdge pollNext() {
            if(it.hasNext()) {
              final BusEdge e = it.next();
              if(e == s) {
                it = null;
              }
              return it != null ? e : null;
            }
            it = e.iterator(); // can not be empty
            final BusEdge e = it.next();
            if(e == s) {
              it = null;
            }
            return it != null ? e : null;
          }

          @Override
          public boolean hasNext() {
            if(cur == null) return false;
            if(pushedBack) return true;
            cur = pollNext();
            pushedBack = true;
            return cur != null;
          }

          @Override
          public BusEdge next() {
            if(cur == null) return null;
            if(pushedBack) {
              pushedBack = false;
              return cur;
            }
            cur = pollNext();
            return cur;
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
