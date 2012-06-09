package infovis.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
   * The backing map for bus station ids.
   */
  private static final Map<Integer, BusStation> STATIONS = new HashMap<Integer, BusStation>();

  /**
   * Getter.
   * 
   * @param id The id of a bus station.
   * @return The bus station with the given id.
   */
  public static BusStation getForId(final int id) {
    return STATIONS.get(id);
  }

  /**
   * Creates a new bus station.
   * 
   * @param name The name.
   * @param id The id. If the id is already used an
   *          {@link IllegalArgumentException} is thrown.
   * @return The newly created bus station.
   */
  public static BusStation createStation(final String name, final int id) {
    if(STATIONS.containsKey(id)) throw new IllegalArgumentException("id: " + id
        + " already in use");
    final BusStation bus = new BusStation(name, id);
    STATIONS.put(id, bus);
    return bus;
  }

  /**
   * The name of the bus station.
   */
  private final String name;

  /**
   * The unique id of the bus station.
   */
  private final int id;

  /**
   * A sorted set of all bus edges starting with the earliest edge (0 hours 0
   * minutes).
   */
  private final SortedSet<BusEdge> edges = new TreeSet<BusEdge>();

  /**
   * Creates a bus station.
   * 
   * @param name The name.
   * @param id The id.
   */
  private BusStation(final String name, final int id) {
    this.name = name;
    this.id = id;
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
   * Adds an edge to this bus station.
   * 
   * @param line bus line
   * @param dest The destination.
   * @param start The start time.
   * @param end The end time.
   */
  public void addEdge(final BusLine line, final BusStation dest, final BusTime start,
      final BusTime end) {
    edges.add(new BusEdge(line, this, dest, start, end));
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
      return new ArrayList<BusEdge>(0);
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

  @Override
  public boolean equals(final Object obj) {
    return obj == null ? false : id == ((BusStation) obj).id;
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
