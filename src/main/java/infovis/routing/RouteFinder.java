package infovis.routing;

import infovis.data.BusEdge;
import infovis.data.BusLine;
import infovis.data.BusStation;
import infovis.data.BusStationEnumerator;
import infovis.data.BusTime;
import infovis.util.Stopwatch;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Algorithm for finding shortest routes from a given bus station.
 * 
 * @author Leo Woerteler
 */
public final class RouteFinder implements RoutingAlgorithm {

  /** Comparator for comparing Routes by travel time, length, and walking time. */
  private static final Comparator<Route> CMP = new Comparator<Route>() {
    @Override
    public int compare(final Route o1, final Route o2) {
      final int timeDiff = o1.travelTime - o2.travelTime;
      if(timeDiff != 0) return timeDiff;
      final int stepsDiff = o1.getLength() - o2.getLength();
      return stepsDiff != 0 ? stepsDiff : o1.walkTimeSecs() - o2.walkTimeSecs();
    }
  };

  @Override
  public RoutingResult[] findRoutes(final BusStationEnumerator bse,
      final BusStation station, final BitSet dests, final BusTime start, final int wait,
      final int maxDuration, final int maxWalk) throws InterruptedException {
    final int sid = station.getId();
    final BusEdge[][] map = findRoutesFrom(bse, station, dests,
        start, wait, maxDuration, maxWalk);
    final RoutingResult[] res = new RoutingResult[bse.maxId() + 1];
    for(int id = 0; id < res.length; ++id) {
      final BusStation to = bse.getForId(id);
      if(id == sid) {
        res[id] = new RoutingResult(station);
        continue;
      }
      final BusEdge[] list = map[id];
      if(list == null) {
        res[id] = new RoutingResult(station, to);
        continue;
      }
      res[id] = new RoutingResult(station, to, list, start,
          start.secondsTo(list[list.length - 1].getEnd()));
    }
    return res;
  }

  /**
   * Finds shortest routes to all reachable stations from the given start
   * station at the given start time.
   * 
   * @param bse The bus station enumerator.
   * @param station start position
   * @param dests set of IDs of stations that should be reached,
   *          <code>null</code> means all stations of the
   *          {@link BusStationEnumerator}.
   * @param start start time
   * @param wait waiting time when changing lines
   * @param maxDuration maximum allowed duration of a route
   * @param maxWalk maximum allowed walking time
   * @return map from station id to shortest route
   * @throws InterruptedException if the current thread was interrupted during
   *           the computation
   */
  public static BusEdge[][] findRoutesFrom(
      final BusStationEnumerator bse, final BusStation station, final BitSet dests,
      final BusTime start, final int wait, final int maxDuration, final int maxWalk)
          throws InterruptedException {
    final Stopwatch t = new Stopwatch();
    long edgeCount = 0;
    long enqueued = 0;

    final int maxWalkSecs = maxWalk * BusTime.SECONDS_PER_MINUTE;
    final int maxDurSecs = maxDuration * BusTime.SECONDS_PER_MINUTE;
    final int waitSecs = wait * BusTime.SECONDS_PER_MINUTE;
    // set of stations yet to be found
    final BitSet notFound;
    if(dests == null) {
      notFound = new BitSet();
      for(final BusStation s : bse.getStations()) {
        notFound.set(s.getId());
      }
    } else {
      notFound = (BitSet) dests.clone();
    }
    notFound.set(station.getId(), false);

    final int stationCount = bse.maxId() + 1;
    final int[] bestTimes = new int[stationCount]; // change time included
    Arrays.fill(bestTimes, -1);

    final Route[] bestRoutes = new Route[stationCount];
    final int initialQueueSize = stationCount; // yields good avg performance
    final PriorityQueue<Route> queue = new PriorityQueue<Route>(initialQueueSize, CMP);
    for(final BusEdge e : station.getEdges(start)) {
      final Route route = new Route(start, e);
      if(route.travelTime <= maxDurSecs) {
        if(maybeEnqueue(queue, bestTimes, waitSecs, route)) {
          ++enqueued;
        }
      }
    }

    for(final BusStation dest : bse.getStations()) {
      if(!dest.equals(station)) {
        final int walkSecs = station.walkingSeconds(dest);
        if(0 <= walkSecs && walkSecs <= maxWalkSecs) {
          final BusTime end = start.later(0, walkSecs);
          final Route route = new Route(start, BusEdge.walking(station, dest, start, end));
          if(route.travelTime <= maxDurSecs) {
            if(maybeEnqueue(queue, bestTimes, waitSecs, route)) {
              ++enqueued;
            }
          }
        }
      }
    }

    for(Route current; !notFound.isEmpty() && (current = queue.poll()) != null;) {
      ++edgeCount;

      if(Thread.interrupted()) throw new InterruptedException();
      final BusEdge last = current.last;
      final BusStation dest = last.getTo();

      final Route best = bestRoutes[dest.getId()];
      if(best == null) {
        bestRoutes[dest.getId()] = current;
        notFound.set(dest.getId(), false);
      }

      final BusTime arrival = last.getEnd();
      for(final BusEdge e : dest.getEdges(arrival)) {
        if(current.timePlus(e) > maxDurSecs || current.contains(e.getTo())) {
          // violates general invariants
          continue;
        }

        final boolean sameTour = last.sameTour(e);
        if(!sameTour && arrival.secondsTo(e.getStart()) < waitSecs) {
          // bus is missed
          continue;
        }

        if(best != null
            && !(sameTour && best.last.getEnd().secondsTo(last.getEnd()) < waitSecs)) {
          // one could just change the bus from the optimal previous route
          continue;
        }

        if(maybeEnqueue(queue, bestTimes, waitSecs, current.extendedBy(e))) {
          ++enqueued;
        }
      }

      if(last.getLine() != BusLine.WALK) {
        for(final BusStation st : bse.getStations()) {
          if(!current.contains(st) && bestRoutes[st.getId()] == null) {
            final int secs = dest.walkingSeconds(st);
            if(secs < 0 || secs > maxWalkSecs) {
              continue;
            }

            final BusTime mid = last.getEnd(), end = mid.later(0, secs);
            final BusEdge e = BusEdge.walking(dest, st, mid, end);

            if(current.timePlus(e) > maxDurSecs) {
              // violates general invariants
              continue;
            }

            if(maybeEnqueue(queue, bestTimes, waitSecs, current.extendedBy(e))) {
              ++enqueued;
            }
          }
        }
      }
    }

    final BusEdge[][] res = new BusEdge[bse.maxId() + 1][];
    res[station.getId()] = new BusEdge[0];
    for(int id = 0; id < bestRoutes.length; ++id) {
      if(bestRoutes[id] == null) {
        continue;
      }
      res[id] = bestRoutes[id].asArray();
    }

    System.out.println("Routing (thread: " + Thread.currentThread().getName()
        + " time: " + t.current() + " edges: " + edgeCount
        + " enqueued: " + enqueued + ")");
    return res;
  }

  /**
   * Checks if a route may be better than the current optimum and enqueues it if
   * so. Also the optimum gets updated.
   * 
   * @param queue The queue.
   * @param bestTimes The map for current optima.
   * @param wait The bus change time in seconds.
   * @param r The route to maybe add.
   * @return <code>true</code> if the route was enqueued.
   */
  private static boolean maybeEnqueue(final Queue<Route> queue,
      final int[] bestTimes, final int wait, final Route r) {
    if(!mayBeBetter(bestTimes, r)) return false;
    updateBestTime(bestTimes, r.last.getTo(), r.travelTime, wait);
    queue.add(r);
    return true;
  }

  /**
   * Updates the optimal time to reach a station.
   * 
   * @param bestTimes The map for current optima.
   * @param station The station.
   * @param curTime The time in seconds.
   * @param wait The bus change time in seconds.
   */
  private static void updateBestTime(final int[] bestTimes,
      final BusStation station, final int curTime, final int wait) {
    final int id = station.getId();
    final int bestTime = bestTimes[id];
    final int newTime = curTime + Math.max(wait, 0);
    if(bestTime < 0 || newTime < bestTime) {
      bestTimes[id] = newTime;
    }
  }

  /**
   * Whether a route may be better than the current optimum.
   * 
   * @param bestTimes The map for current optima.
   * @param r The route.
   * @return Whether the route may be faster.
   */
  private static boolean mayBeBetter(final int[] bestTimes, final Route r) {
    return mayBeBetter(bestTimes, r.last.getTo(), r.travelTime);
  }

  /**
   * Whether a travel time may be better than the current optimum.
   * 
   * @param bestTimes The map for current optima.
   * @param station The station.
   * @param time The time in seconds.
   * @return Whether the travel time may be better.
   */
  private static boolean mayBeBetter(final int[] bestTimes,
      final BusStation station, final int time) {
    final int id = station.getId();
    final int bestTime = bestTimes[id];
    return bestTime < 0 || bestTime >= time;
  }

  /**
   * Finds a single shortest route from the start station to the destination.
   * 
   * @param bse The bus station enumerator.
   * @param station start station
   * @param dest destination
   * @param start start time
   * @param wait waiting time when changing bus lines
   * @param maxDuration maximum allowed travel time
   * @param maxWalk maximum allowed walking time
   * @return shortest route if found, <code>null</code> otherwise
   * @throws InterruptedException if the current thread was interrupted
   */
  public static BusEdge[] findRoute(final BusStationEnumerator bse,
      final BusStation station, final BusStation dest, final BusTime start,
      final int wait, final int maxDuration, final int maxWalk)
          throws InterruptedException {
    final BitSet set = new BitSet();
    set.set(dest.getId());
    return findRoutesFrom(bse, station, set, start, wait, maxDuration, maxWalk)[dest.getId()];
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
    protected final Route before;
    /** Last edge in the route. */
    protected final BusEdge last;
    /** Overall travel time in seconds. */
    protected final int travelTime;
    /** The length of this route. */
    private final int length;
    /** Stations in this route. */
    private final BitSet stations;

    /**
     * Creates a new route with given waiting time and first edge.
     * 
     * @param start start time of the route
     * @param first first edge
     */
    Route(final BusTime start, final BusEdge first) {
      before = null;
      last = first;
      travelTime = start.secondsTo(first.getStart()) + first.travelSeconds();
      stations = new BitSet();
      stations.set(first.getFrom().getId());
      stations.set(first.getTo().getId());
      length = 1;
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
      length = before.length + 1;
      travelTime = before.timePlus(last);
      stations = (BitSet) before.stations.clone();
      stations.set(last.getTo().getId());
    }

    /**
     * Creates a new route by extending this one by the given edge.
     * 
     * @param next edge to be added
     * @return new route
     */
    public Route extendedBy(final BusEdge next) {
      assert loopFree(next); // loop detection
      return new Route(this, next);
    }

    /**
     * Tests whether a route with the given next edge would have no loops. This
     * is a general invariant and the method should only be called for debugging
     * purposes.
     * 
     * @param next The possible next route.
     * @return Whether the resulting route has no loops.
     */
    private boolean loopFree(final BusEdge next) {
      for(Route r = this; r != null; r = r.before) {
        if(r.last.equals(next)) return false;
      }
      return true;
    }

    /**
     * Checks if this route contains the given bus station.
     * 
     * @param s bus station
     * @return <code>true</code> if the station is contained, <code>false</code>
     *         otherwise
     */
    public boolean contains(final BusStation s) {
      return stations.get(s.getId());
    }

    /**
     * Getter.
     * 
     * @return number of edges in this route
     */
    public int getLength() {
      return length;
    }

    /**
     * Total time walked in this route.
     * 
     * @return number of seconds spent walking
     */
    public int walkTimeSecs() {
      int walked = 0;
      for(Route route = this; route != null; route = route.before) {
        if(route.last.getLine() == BusLine.WALK) {
          walked += route.last.travelSeconds();
        }
      }
      return walked;
    }

    /**
     * Calculates the overall travel time of this route as extended by the given
     * edge.
     * 
     * @param next next edge
     * @return time in seconds
     */
    public int timePlus(final BusEdge next) {
      return travelTime + last.getEnd().secondsTo(next.getStart()) + next.travelSeconds();
    }

    /**
     * Creates an array containing all edges of this route.
     * 
     * @return array containing all edges
     */
    public BusEdge[] asArray() {
      final BusEdge[] res = new BusEdge[length];
      Route cur = this;
      int i = length;
      while(--i >= 0) {
        res[i] = cur.last;
        cur = cur.before;
      }
      return res;
    }

    @Override
    public String toString() {
      return last.toString();
    }

  } // Route

}
