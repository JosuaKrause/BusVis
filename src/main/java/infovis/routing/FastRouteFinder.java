package infovis.routing;

import infovis.data.BusEdge;
import infovis.data.BusLine;
import infovis.data.BusStation;
import infovis.data.BusStationEnumerator;
import infovis.data.BusTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * A fast but in rare cases incorrect routing algorithm.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 * @deprecated Replaced by {@link RouteFinder}. This class is not maintained
 *             anymore. No optimizations and no adaption to interface changes.
 */
@Deprecated
public final class FastRouteFinder implements RoutingAlgorithm {

  /**
   * Finds the shortest route to the given station.
   * 
   * @param bse The bus station enumerator.
   * @param from The start station.
   * @param dest The destination.
   * @param start The start time.
   * @param changeTime The time to change lines.
   * @param maxTime The maximum time in minutes.
   * @return The shortest route to the destination or <code>null</code> if there
   *         exists no route to the given destination.
   */
  public static Deque<BusEdge> routeTo(final BusStationEnumerator bse,
      final BusStation from, final BusStation dest,
      final BusTime start,
      final int changeTime, final int maxTime) {
    final Map<Integer, Route> routes = new HashMap<Integer, Route>();
    iniRoutes(bse, routes, start);
    if(!findRoutes(from, routes, dest, start, changeTime, maxTime)) return null;
    return convertRoutes(routes.get(dest.getId()));
  }

  /**
   * Finds shortest routes to all bus stations.
   * 
   * @param bse The bus station enumerator.
   * @param from The start bus station.
   * @param start The start time.
   * @param changeTime The time to change lines.
   * @param maxTime The maximum time in minutes.
   * @return The reach-ability of all bus stations.
   */
  public static Collection<RoutingResult> routes(final BusStationEnumerator bse,
      final BusStation from, final BusTime start, final int changeTime, final int maxTime) {
    final Map<Integer, Route> routes = new HashMap<Integer, Route>();
    iniRoutes(bse, routes, start);
    findRoutes(from, routes, null, start, changeTime, maxTime);
    return convert(start, from, routes.values());
  }

  /**
   * Converts routes into routing results.
   * 
   * @param startTime The start time.
   * @param from The start bus station.
   * @param routes The routes.
   * @return The routing results.
   */
  private static Collection<RoutingResult> convert(final BusTime startTime,
      final BusStation from,
      final Collection<Route> routes) {
    final List<RoutingResult> res = new ArrayList<RoutingResult>(routes.size());
    for(final Route r : routes) {
      if(r.isNotReachable()) {
        res.add(new RoutingResult(from, r.getStation()));
      } else {
        res.add(new RoutingResult(from, r.getStation(), r.minutes(), convertRoutes(r),
            startTime));
      }
    }
    return res;
  }

  /**
   * Converts the route objects back into a meaningful path by going from the
   * destination to the start.
   * 
   * @param r The route.
   * @return The path.
   */
  private static Deque<BusEdge> convertRoutes(final Route r) {
    Route cur = r;
    final Deque<BusEdge> res = new LinkedList<BusEdge>();
    do {
      final BusEdge edge = cur.getFrom();
      // if(res.contains(edge)) {
      // res.addFirst(edge);
      // System.out.println(res);
      // throw new IllegalArgumentException("loop");
      // }
      res.addFirst(edge);
      cur = cur.getParent();
    } while(cur != null && cur.hasFrom());
    return res;
  }

  /**
   * Finds the shortest route.
   * 
   * @param from The start bus station.
   * @param routes The route map.
   * @param dest The destination.
   * @param start The start time.
   * @param changeTime The changing time in minutes.
   * @param maxTime The maximum number of minutes.
   * @return Whether there exists an route to the destination.
   */
  private static boolean findRoutes(final BusStation from,
      final Map<Integer, Route> routes,
      final BusStation dest, final BusTime start, final int changeTime, final int maxTime) {
    final Queue<BusEdge> edges = new PriorityQueue<BusEdge>(20,
        BusEdge.createRelativeComparator(start));
    routes.get(from.getId()).setStart();
    addAllEdges(edges, from, start, changeTime, null, start, maxTime);
    for(BusEdge e; (e = edges.poll()) != null;) {
      // sanity check
      if(start.minutesTo(e.getStart()) > start.minutesTo(e.getEnd())) {
        // edge starts before start
        continue;
      }

      final BusStation to = e.getTo();
      if(to.equals(from)) { // edge is back to the start
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
      addAllEdges(edges, to, curEnd, changeTime, e.getLine(), start, maxTime);
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
   * @param maxTime The maximum number of minutes.
   */
  private static void addAllEdges(final Queue<BusEdge> edges, final BusStation station,
      final BusTime time, final int changeTime, final BusLine line, final BusTime max,
      final int maxTime) {
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
   * @param bse The bus station enumerator.
   * @param routes The route map.
   * @param start The start time.
   */
  private static void iniRoutes(final BusStationEnumerator bse,
      final Map<Integer, Route> routes,
      final BusTime start) {
    for(final BusStation station : bse.getStations()) {
      routes.put(station.getId(), new Route(station, start));
    }
  }

  @Override
  public RoutingResult[] findRoutes(final BusStationEnumerator bse,
      final BusStation station, final BitSet dests, final BusTime start, final int wait,
      final int maxDuration, final int maxWalk)
          throws InterruptedException {
    final Collection<RoutingResult> res = routes(bse, station, start, wait, maxDuration);
    final RoutingResult[] arr = res.toArray(new RoutingResult[res.size()]);
    Arrays.sort(arr, new Comparator<RoutingResult>() {

      @Override
      public int compare(final RoutingResult a, final RoutingResult b) {
        return a.getEnd().getId() - b.getEnd().getId();
      }

    });
    return arr;
  }

  @Override
  public String toString() {
    return "Fast route finder";
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

}
