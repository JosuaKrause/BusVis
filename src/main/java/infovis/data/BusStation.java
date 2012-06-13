package infovis.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
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
   * The unique id of the bus station.
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
   * @param id The id.
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
   * A route is given by the best edges per bus station.
   * 
   * @author Joschi <josua.krause@googlemail.com>
   */
  public static final class Route {

    /**
     * The bus station.
     */
    private final BusStation station;

    /**
     * The best edge leading to the bus station.
     */
    private BusEdge from;

    /**
     * The start time.
     */
    private final BusTime start;

    /**
     * The parent route.
     */
    private Route parent;

    /**
     * Creates a route object.
     * 
     * @param station The bus station.
     * @param start The start time.
     */
    public Route(final BusStation station, final BusTime start) {
      this.station = station;
      this.start = start;
    }

    /**
     * Getter.
     * 
     * @return The bus station.
     */
    public BusStation getStation() {
      return station;
    }

    /**
     * Getter.
     * 
     * @return The best edge leading to the bus station or <code>null</code> if
     *         it is not assigned yet.
     */
    public BusEdge getFrom() {
      return from;
    }

    /**
     * Getter.
     * 
     * @return Whether there is already a best edge.
     */
    public boolean hasFrom() {
      return from != null;
    }

    /**
     * The cached minutes value.
     */
    private int min = -1;

    /**
     * Getter.
     * 
     * @return The minutes that are needed to arrive this bus station.
     */
    public int minutes() {
      if(min == -1) {
        min = start.minutesTo(from.getEnd());
      }
      return min;
    }

    /**
     * Getter.
     * 
     * @return Whether the bus station is reachable.
     */
    public boolean isNotReachable() {
      return parent == null;
    }

    /**
     * Getter.
     * 
     * @return Whether the bus station is the starting point.
     */
    public boolean isStart() {
      return parent == this;
    }

    /**
     * Getter.
     * 
     * @return The parent bus station or <code>null</code> if it is the start or
     *         not reachable.
     */
    public Route getParent() {
      return parent != this ? parent : null;
    }

    /**
     * Setter.
     * 
     * @param from Sets the new best edge.
     * @param parent The parent route.
     */
    public void setFrom(final BusEdge from, final Route parent) {
      this.from = from;
      this.parent = parent;
    }

    /**
     * Marks this Route as start.
     */
    public void setStart() {
      from = null;
      parent = this;
      min = 0;
    }

  }

  /**
   * Finds the shortest route to the given station.
   * 
   * @param dest The destination.
   * @param start The start time.
   * @param changeTime The time to change lines.
   * @return The shortest route to the destination or <code>null</code> if there
   *         exists no route to the given destination.
   */
  public Deque<BusEdge> routeTo(final BusStation dest, final BusTime start,
      final int changeTime) {
    final Map<Integer, Route> routes = new HashMap<Integer, Route>();
    iniRoutes(routes, start);
    if(!findRoutes(routes, dest, start, changeTime)) return null;
    return convertRoutes(routes, dest);
  }

  /**
   * Finds shortest routes to all bus stations.
   * 
   * @param start The start time.
   * @param changeTime The time to change lines.
   * @return The reachability of all bus stations.
   */
  public Collection<Route> routes(final BusTime start, final int changeTime) {
    final Map<Integer, Route> routes = new HashMap<Integer, Route>();
    iniRoutes(routes, start);
    findRoutes(routes, null, start, changeTime);
    return routes.values();
  }

  /**
   * Converts the route objects back into a meaningful path by going from the
   * destination to the start.
   * 
   * @param routes The route map.
   * @param dest The destination.
   * @return The path.
   */
  private Deque<BusEdge> convertRoutes(final Map<Integer, Route> routes,
      final BusStation dest) {
    Route cur = routes.get(dest.getId());
    final Deque<BusEdge> res = new LinkedList<BusEdge>();
    do {
      final BusEdge edge = cur.getFrom();
      res.addFirst(edge);
      cur = routes.get(edge.getFrom().getId());
    } while(!cur.getStation().equals(this));
    return res;
  }

  /**
   * Finds the shortest route.
   * 
   * @param routes The route map.
   * @param dest The destination.
   * @param start The start time.
   * @param changeTime The changing time in minutes.
   * @return Whether there exists an route to the destination.
   */
  private boolean findRoutes(final Map<Integer, Route> routes, final BusStation dest,
      final BusTime start,
      final int changeTime) {
    final Queue<BusEdge> edges = new PriorityQueue<BusEdge>(20,
        BusEdge.createRelativeComparator(start));
    routes.get(getId()).setStart();
    addAllEdges(edges, this, start, changeTime, null, start);
    while(!edges.isEmpty()) {
      final BusEdge e = edges.poll();
      final int startTime = start.minutesTo(e.getStart());
      final int endTime = start.minutesTo(e.getEnd());
      if(startTime > endTime) { // edge starts before start
        continue;
      }
      final BusStation to = e.getTo();
      if(to.equals(this)) { // edge is back to the start
        continue;
      }
      final Route next = routes.get(to.getId());
      if(next.hasFrom()) { // destination already visited
        continue;
      }
      next.setFrom(e, routes.get(e.getFrom().getId()));
      if(to.equals(dest)) { // we are done
        break;
      }
      final BusTime curEnd = e.getEnd();
      addAllEdges(edges, to, curEnd, changeTime, e.getLine(), start);
    }
    return dest == null || routes.get(dest.getId()).hasFrom();
  }

  /**
   * Adds all edges to the queue. First all edges of the same line are added and
   * then the edges of the other lines.
   * 
   * @param edges The queue.
   * @param station The station where the edges are originating.
   * @param time The current time.
   * @param changeTime The change time.
   * @param line The current bus line or <code>null</code> if there is none.
   * @param max The maximal time.
   */
  private void addAllEdges(final Queue<BusEdge> edges, final BusStation station,
      final BusTime time, final int changeTime, final BusLine line, final BusTime max) {
    final int maxTime = manager.getMaxTimeHours() * 60;
    if(line == null) {
      for(final BusEdge edge : station.getEdges(time)) {
        if(!validEdge(edge, time, maxTime, max)) {
          continue;
        }
        edges.add(edge);
      }
      return;
    }
    for(final BusEdge edge : station.getEdges(time)) {
      if(!validEdge(edge, time, maxTime, max)) {
        continue;
      }
      if(edge.getLine().equals(line)) {
        edges.add(edge);
      }
    }
    final BusTime nt = time.later(changeTime);
    for(final BusEdge edge : station.getEdges(nt)) {
      if(!validEdge(edge, nt, maxTime - changeTime, max.later(-changeTime))) {
        continue;
      }
      if(edge.getLine().equals(line)) {
        continue;
      }
      edges.add(edge);
    }
  }

  /**
   * If an edge is in a valid time span.
   * 
   * @param edge The edge.
   * @param start The start time.
   * @param maxTime The maximum time.
   * @param max The maximal time.
   * @return Whether the start time of the edge is in the given interval.
   */
  private static boolean validEdge(final BusEdge edge, final BusTime start,
      final int maxTime, final BusTime max) {
    final int time = start.minutesTo(edge.getStart());
    final int maxMin = start.minutesTo(max);
    return time < maxTime && (maxMin == 0 || maxMin > time);
  }

  /**
   * Initializes the route objects.
   * 
   * @param routes The route map.
   * @param start The start time.
   */
  private void iniRoutes(final Map<Integer, Route> routes, final BusTime start) {
    for(final BusStation station : manager.getStations()) {
      routes.put(station.getId(), new Route(station, start));
    }
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
   * The y coordinate for this bus station on the abstact map.
   */
  private final double abstractY;

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
