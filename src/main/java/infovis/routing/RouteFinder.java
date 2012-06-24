package infovis.routing;

import infovis.data.BusEdge;
import infovis.data.BusLine;
import infovis.data.BusStation;
import infovis.data.BusStationEnumerator;
import infovis.data.BusTime;

import java.util.BitSet;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Algorithm for finding shortest routes from a given bus station.
 * 
 * @author Leo Woerteler
 */
public final class RouteFinder implements RoutingAlgorithm {
  /** Comparator for comparing Routes by travel time, length an walking time. */
  private static final Comparator<Route> CMP = new Comparator<Route>() {
    @Override
    public int compare(final Route o1, final Route o2) {
      final int timeDiff = o1.travelTime - o2.travelTime;
      if(timeDiff != 0) return timeDiff;
      final int stepsDiff = o1.getLength() - o2.getLength();
      return stepsDiff != 0 ? timeDiff : o1.walkTime() - o2.walkTime();
    }
  };

  @Override
  public RoutingResult[] findRoutes(final BusStationEnumerator bse,
      final BusStation station, final BitSet dests, final BusTime start, final int wait,
      final int maxDuration, final int maxWalk) throws InterruptedException {
    final int sid = station.getId();
    final BusEdge[][] map = findRoutesFrom(bse, station, dests, start, wait, maxDuration,
        maxWalk);
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
      res[id] = new RoutingResult(station, to,
          start.minutesTo(list[list.length - 1].getEnd()), list, start);
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
    final int maxWalkSecs = maxWalk * BusTime.SECONDS_PER_MINUTE;
    // set of stations yet to be found
    final BitSet notFound = dests == null ? new BitSet() : (BitSet) dests.clone();
    if(dests == null) {
      for(final BusStation s : bse.getStations()) {
        notFound.set(s.getId());
      }
    }
    notFound.set(station.getId(), false);

    final Route[] bestRoutes = new Route[bse.maxId() + 1];
    final PriorityQueue<Route> queue = new PriorityQueue<Route>(16, CMP);
    for(final BusEdge e : station.getEdges(start)) {
      final Route route = new Route(start, e);
      if(route.travelTime <= maxDuration) {
        queue.add(route);
      }
    }

    for(final BusStation dest : bse.getStations()) {
      if(!dest.equals(station)) {
        final int walkSecs = station.walkingSeconds(dest);
        if(0 <= walkSecs && walkSecs <= maxWalkSecs) {
          final BusTime end = start.later((walkSecs + BusTime.SECONDS_PER_MINUTE - 1)
              / BusTime.SECONDS_PER_MINUTE);
          final Route route = new Route(start, BusEdge.walking(station, dest, start, end));
          if(route.travelTime <= maxDuration) {
            queue.add(route);
          }
        }
      }
    }

    for(Route current; !notFound.isEmpty() && (current = queue.poll()) != null;) {
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
        if(current.timePlus(e) > maxDuration || current.contains(e.getTo())) {
          // violates general invariants
          continue;
        }

        final boolean sameTour = last.sameTour(e);
        if(!sameTour && arrival.minutesTo(e.getStart()) < wait) {
          // bus is missed
          continue;
        }

        if(best != null
            && !(sameTour && best.last.getEnd().minutesTo(last.getEnd()) < wait)) {
          // one could just change the bus from the optimal previous route
          continue;
        }

        queue.add(current.extendedBy(e));
      }

      if(last.getLine() != BusLine.WALK) {
        for(final BusStation st : bse.getStations()) {
          if(!current.contains(st) && bestRoutes[st.getId()] == null) {
            final int secs = dest.walkingSeconds(st);
            if(secs < 0 || secs > maxWalk * BusTime.SECONDS_PER_MINUTE) {
              continue;
            }

            // FIXME rounding errors
            final BusTime mid = last.getEnd(), end = mid.later(
                (secs + BusTime.SECONDS_PER_MINUTE - 1) / BusTime.SECONDS_PER_MINUTE);
            final BusEdge e = BusEdge.walking(dest, st, mid, end);

            if(current.timePlus(e) > maxDuration) {
              // violates general invariants
              continue;
            }

            queue.add(current.extendedBy(e));
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
    return res;
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
      final BusStation station, final BusStation dest,
      final BusTime start, final int wait, final int maxDuration, final int maxWalk)
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
    /** Overall travel time in minutes. */
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
      travelTime = start.minutesTo(first.getStart()) + first.travelMinutes();
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
     * @return number of minutes spent walking
     */
    public int walkTime() {
      int walked = 0;
      for(Route route = this; route != null; route = route.before) {
        if(route.last.getLine() == BusLine.WALK) {
          walked += route.last.travelMinutes();
        }
      }
      return walked;
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

  }

}
