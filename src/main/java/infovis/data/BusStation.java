package infovis.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
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
   */
  BusStation(final BusStationManager manager, final String name, final int id,
      final double x, final double y) {
    this.manager = manager;
    this.name = name;
    this.id = id;
    this.x = x;
    this.y = y;
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
   * A route is given by the best edges per bus station.
   * 
   * @author Joschi <josua.krause@googlemail.com>
   */
  private static final class Route {

    /**
     * The bus station.
     */
    private final BusStation station;

    /**
     * The best edge leading to the bus station.
     */
    private BusEdge from;

    /**
     * The time away from the start.
     */
    private int time = Integer.MAX_VALUE;

    /**
     * Creates a route object.
     * 
     * @param station The bus station.
     */
    public Route(final BusStation station) {
      this.station = station;
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
     * Setter.
     * 
     * @param from Sets the new best edge.
     * @param start The original starting time.
     */
    public void setFrom(final BusEdge from, final BusTime start) {
      this.from = from;
      time = start.minutesTo(from.getEnd());
    }

    /**
     * Getter.
     * 
     * @return The time in minutes from the start.
     */
    public int getTime() {
      return time;
    }

    /**
     * Marks this Route as start.
     */
    public void setStart() {
      from = null;
      time = 0;
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
    iniRoutes(routes);
    if(!findRoutes(routes, dest, start, changeTime)) return null;
    return convertRoutes(routes, dest);
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
    final Set<BusEdge> already = new HashSet<BusEdge>();
    Route cur = routes.get(dest.getId());
    final Deque<BusEdge> res = new LinkedList<BusEdge>();
    do {
      final BusEdge edge = cur.getFrom();
      res.addFirst(edge);
      if(already.contains(edge)) {
        System.err.println(res);
        throw new IllegalStateException("loop detected");
      }
      already.add(edge);
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
    int best = -1;
    final Deque<BusEdge> edges = new LinkedList<BusEdge>();
    routes.get(getId()).setStart();
    addAllEdges(edges, this, start, changeTime, null);
    for(;;) {
      if(edges.isEmpty()) {
        break;
      }
      final BusEdge e = edges.pollFirst();
      final BusStation to = e.getTo();
      if(to.equals(this)) {
        continue;
      }
      final BusTime curStart = e.getStart();
      if(best >= 0 && start.minutesTo(curStart) >= best) {
        continue;
      }
      final BusStation from = e.getFrom();
      final Route last = routes.get(from.getId());
      final int startTime = start.minutesTo(e.getStart())
          + (!last.hasFrom() || e.getLine().equals(last.getFrom().getLine()) ? 0
              : changeTime);
      if(last.getTime() > startTime) {
        continue;
      }
      final BusTime curEnd = e.getEnd();
      final int curTime = start.minutesTo(curEnd);
      final Route next = routes.get(to.getId());
      if(next.hasFrom() && next.getTime() < curTime) {
        continue;
      }
      next.setFrom(e, start);
      if(to.equals(dest)) {
        best = curTime;
      } else {
        addAllEdges(edges, to, curEnd, changeTime, e.getLine());
      }
    }
    return routes.get(dest.getId()).hasFrom();
  }

  /**
   * Adds all edges to the deque. First all edges of the same line are added and
   * then the edges of the other lines.
   * 
   * @param edges The deque.
   * @param station The station where the edges are originating.
   * @param time The current time.
   * @param changeTime The change time.
   * @param line The current bus line or <code>null</code> if there is none.
   */
  private void addAllEdges(final Deque<BusEdge> edges, final BusStation station,
      final BusTime time, final int changeTime, final BusLine line) {
    final int maxTime = manager.getMaxTimeHours() * 60;
    if(line == null) {
      for(final BusEdge edge : station.getEdges(time)) {
        if(!validEdge(edge, time, maxTime)) {
          continue;
        }
        edges.addLast(edge);
      }
      return;
    }
    for(final BusEdge edge : station.getEdges(time)) {
      if(!validEdge(edge, time, maxTime)) {
        continue;
      }
      if(edge.getLine().equals(line)) {
        edges.addLast(edge);
      }
    }
    final BusTime nt = time.later(changeTime);
    for(final BusEdge edge : station.getEdges(nt)) {
      if(!validEdge(edge, nt, maxTime - changeTime)) {
        continue;
      }
      if(edge.getLine().equals(line)) {
        continue;
      }
      edges.addLast(edge);
    }
  }

  /**
   * If an edge is in a valid time span.
   * 
   * @param edge The edge.
   * @param start The start time.
   * @param maxTime The maximum time.
   * @return Whether the start time of the edge is in the given interval.
   */
  private static boolean validEdge(final BusEdge edge, final BusTime start,
      final int maxTime) {
    return start.minutesTo(edge.getStart()) < maxTime;
  }

  /**
   * Initializes the route objects.
   * 
   * @param routes The route map.
   */
  private void iniRoutes(final Map<Integer, Route> routes) {
    for(final BusStation station : manager.getStations()) {
      routes.put(station.getId(), new Route(station));
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
