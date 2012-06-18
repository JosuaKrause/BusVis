package infovis.routing;

import infovis.data.BusEdge;
import infovis.data.BusStation;
import infovis.data.BusStationManager;
import infovis.data.BusTime;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

/**
 * Algorithm for finding shortest routes from a given bus station.
 * 
 * @author Leo Woerteler
 */
public final class RouteFinder implements RoutingAlgorithm {
  /** Comparator for comparing {@link Route}s by travel time. */
  private static final Comparator<Route> COMP = new Comparator<Route>() {
    @Override
    public int compare(final Route o1, final Route o2) {
      return o1.travelTime - o2.travelTime;
    }
  };

  @Override
  public Collection<RoutingResult> findRoutes(final BusStation station,
      final BitSet dests,
      final BusTime start, final int wait, final int maxDuration)
          throws InterruptedException {
    final Map<BusStation, List<BusEdge>> map = findRoutesFrom(station, dests, start,
        wait, maxDuration);
    final List<RoutingResult> res = new ArrayList<RoutingResult>(map.size());
    for(final Entry<BusStation, List<BusEdge>> e : map.entrySet()) {
      final List<BusEdge> list = e.getValue();
      final BusStation to = e.getKey();
      if(list.isEmpty()) {
        res.add(new RoutingResult(station, to));
      } else {
        res.add(new RoutingResult(station, to,
            start.minutesTo(list.get(list.size() - 1).getEnd()), list));
      }
    }
    return res;
  }

  /**
   * Finds shortest routes to all reachable stations from the given start
   * station at the given start time.
   * 
   * @param station start position
   * @param dests set of IDs of stations that should be reached,
   *          <code>null</code> means all stations of the start station's
   *          {@link BusStationManager}
   * @param start start time
   * @param wait waiting time when changing lines
   * @param maxDuration maximum allowed duration of a route
   * @return map from station to shortest route
   * @throws InterruptedException if the current thread was interrupted during
   *           the computation
   */
  public static Map<BusStation, List<BusEdge>> findRoutesFrom(final BusStation station,
      final BitSet dests, final BusTime start, final int wait, final int maxDuration)
          throws InterruptedException {
    // set of stations yet to be found
    final BitSet notFound = new BitSet();
    if(dests == null) {
      for(final BusStation s : station.getManager().getStations()) {
        notFound.set(s.getId());
      }
    } else {
      notFound.or(dests);
    }
    notFound.set(station.getId(), false);

    final Map<BusStation, Route> bestRoutes = new HashMap<BusStation, Route>();
    final PriorityQueue<Route> queue = new PriorityQueue<Route>(16, COMP);
    for(final BusEdge e : station.getEdges(start)) {
      final Route route = new Route(start, e);
      if(route.travelTime <= maxDuration) {
        queue.add(route);
      }
    }

    for(Route current; !notFound.isEmpty() && (current = queue.poll()) != null;) {
      checkInterrupt();
      final BusEdge last = current.last;
      final BusStation dest = last.getTo();

      final boolean best = !bestRoutes.containsKey(dest);
      if(best) {
        bestRoutes.put(dest, current);
        notFound.set(dest.getId(), false);
      }

      final BusTime arrival = last.getEnd();
      for(final BusEdge e : dest.getEdges(arrival)) {
        if(!(last.sameTour(e) || best && arrival.minutesTo(e.getStart()) >= wait)) {
          continue;
        }
        if(!(current.timePlus(e) <= maxDuration && !current.contains(e.getTo()))) {
          continue;
        }
        final Route r = current.extendedBy(e);
        queue.add(r);
      }
    }

    final Map<BusStation, List<BusEdge>> res = new HashMap<BusStation, List<BusEdge>>();
    res.put(station, Collections.EMPTY_LIST);
    for(final Entry<BusStation, Route> e : bestRoutes.entrySet()) {
      res.put(e.getKey(), e.getValue().asList());
    }
    return res;
  }

  /**
   * Checks the interrupt status of the current thread.
   * 
   * @throws InterruptedException If the interrupt status was set.
   */
  private static void checkInterrupt() throws InterruptedException {
    if(Thread.interrupted()) throw new InterruptedException();
  }

  /**
   * Finds a single shortest route from the start station to the destination.
   * 
   * @param station start station
   * @param dest destination
   * @param start start time
   * @param wait waiting time when changing bus lines
   * @param maxDuration maximum allowed travel time
   * @return shortest route if found, <code>null</code> otherwise
   * @throws InterruptedException if the current thread was interrupted
   */
  public static List<BusEdge> findRoute(final BusStation station, final BusStation dest,
      final BusTime start, final int wait, final int maxDuration)
          throws InterruptedException {
    final BitSet set = new BitSet();
    set.set(dest.getId());
    return findRoutesFrom(station, set, start, wait, maxDuration).get(dest);
  }

  @Override
  public String toString() {
    return "Exact route finder";
  }

  /**
   * Inner class for routes.
   * 
   * @author Leo Woerteler
   */
  private static final class Route {
    /** Route up to this point, possibly {@code null}. */
    final Route before;
    /** Last edge in the route. */
    final BusEdge last;
    /** Overall travel time in minutes. */
    final int travelTime;

    /**
     * Creates a new route with given waiting time and first edge.
     * 
     * @param start start time of the route
     * @param first first edge
     */
    Route(final BusTime start, final BusEdge first) {
      before = null;
      last = first;
      travelTime = start.minutesTo(first.getStart()) + first.travelMinutes();
    }

    /**
     * Creates a new route with given previous route and last edge.
     * 
     * @param before previously taken route
     * @param last last edge
     */
    private Route(final Route before, final BusEdge last) {
      this.before = before;
      this.last = last;
      travelTime = before.timePlus(last);
    }

    /**
     * Creates a new route by extending this one by the given edge.
     * 
     * @param next edge to be added
     * @return new route
     */
    public Route extendedBy(final BusEdge next) {
      for(Route r = this; r != null; r = r.before) {
        if(r.last.equals(next)) throw new IllegalArgumentException("loop");
      }
      return new Route(this, next);
    }

    /**
     * Checks if this route contains the given bus station.
     * 
     * @param s bus station
     * @return <code>true</code> if the station is contained, <code>false</code>
     *         otherwise
     */
    public boolean contains(final BusStation s) {
      if(last.getTo().equals(s)) return true;
      Route r = this;
      do {
        if(r.last.getFrom().equals(s)) return true;
      } while((r = r.before) != null);
      return false;
    }

    /**
     * Calculates the overall travel time of this route as extended by the given
     * edge.
     * 
     * @param next next edge
     * @return time in minutes
     */
    public int timePlus(final BusEdge next) {
      return travelTime + last.getEnd().minutesTo(next.getStart()) + next.travelMinutes();
    }

    /**
     * Creates a list containing all edges of this route.
     * 
     * @return list containing all edges
     */
    public List<BusEdge> asList() {
      final List<BusEdge> list = before == null ? new ArrayList<BusEdge>()
          : before.asList();
      list.add(last);
      return list;
    }

    @Override
    public String toString() {
      return last.toString();
    }

  }

}
