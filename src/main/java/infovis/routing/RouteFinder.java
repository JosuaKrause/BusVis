package infovis.routing;

import infovis.data.BusDataBuilder;
import infovis.data.BusEdge;
import infovis.data.BusStation;
import infovis.data.BusStationEnumerator;
import infovis.data.BusStationManager;
import infovis.data.BusTime;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Algorithm for finding shortest routes from a given bus station.
 * 
 * @author Leo Woerteler
 */
public final class RouteFinder implements RoutingAlgorithm {
  /** Comparator for comparing {@link Route}s by travel time. */
  private static final Comparator<Route> CMP = new Comparator<Route>() {
    @Override
    public int compare(final Route o1, final Route o2) {
      return o1.travelTime - o2.travelTime;
    }
  };

  @Override
  public RoutingResult[] findRoutes(final BusStationEnumerator bse,
      final BusStation station, final BitSet dests, final BusTime start, final int wait,
      final int maxDuration) throws InterruptedException {
    final int sid = station.getId();
    final BusEdge[][] map = findRoutesFrom(bse, station, dests, start, wait, maxDuration);
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
   * @return map from station id to shortest route
   * @throws InterruptedException if the current thread was interrupted during
   *           the computation
   */
  public static BusEdge[][] findRoutesFrom(
      final BusStationEnumerator bse, final BusStation station, final BitSet dests,
      final BusTime start, final int wait, final int maxDuration)
          throws InterruptedException {
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
   * @return shortest route if found, <code>null</code> otherwise
   * @throws InterruptedException if the current thread was interrupted
   */
  public static BusEdge[] findRoute(final BusStationEnumerator bse,
      final BusStation station, final BusStation dest,
      final BusTime start, final int wait, final int maxDuration)
          throws InterruptedException {
    final BitSet set = new BitSet();
    set.set(dest.getId());
    return findRoutesFrom(bse, station, set, start, wait, maxDuration)[dest.getId()];
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

  /**
   * A little performance test.
   * 
   * @param args No-args
   * @throws Exception No-exceptions
   */
  public static void main(final String[] args) throws Exception {
    final BusStationManager man = BusDataBuilder.load("src/main/resources");
    final BitSet set = new BitSet();
    for(final BusStation a : man.getStations()) {
      set.set(a.getId());
    }

    final boolean performance = true, store = false;

    if(performance) {
      final int numTests = 5;
      double avgFullTime = 0;
      double c = 0;
      for(int i = 0; i < numTests; ++i) {
        int count = 0;
        final long time = System.currentTimeMillis();
        for(final BusStation a : man.getStations()) {
          RouteFinder.findRoutesFrom(man, a, set, new BusTime(12, 0), 5,
              man.getMaxTimeHours()
              * BusTime.MINUTES_PER_HOUR);
          ++count;
        }
        final double fullTime = System.currentTimeMillis() - time;
        avgFullTime += fullTime;
        c = count;
      }
      final double fullTime = avgFullTime / numTests;
      final PrintWriter out = new PrintWriter(new OutputStreamWriter(
          new FileOutputStream(
              new File("performance.txt"), true), "UTF-8"));
      out.println(fullTime / 1000 + "s " + fullTime / c + "ms per line");
      out.close();
      System.out.println(fullTime / 1000 + "s");
      System.out.println(fullTime / c + "ms per line");
    } else if(store) {
      final List<BusStation> stations = new ArrayList<BusStation>(man.getStations());
      Collections.sort(stations);
      final DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(
          new FileOutputStream("res.txt")));
      for(final BusStation a : stations) {
        dos.writeByte(a.getId());
        final BusEdge[][] routes = RouteFinder.findRoutesFrom(man, a,
            set, new BusTime(12, 0), 5, man.getMaxTimeHours() * BusTime.MINUTES_PER_HOUR);
        for(final BusStation to : stations) {
          dos.writeByte(to.getId());
          final BusEdge[] route = routes[to.getId()];
          final int time = route == null ? -1 : route.length == 0 ? 0 :
            new BusTime(12, 0).minutesTo(route[route.length - 1].getEnd());
          dos.writeInt(time);
        }
        dos.writeByte(-2);
      }
      dos.close();
    } else {
      final DataInputStream in = new DataInputStream(new BufferedInputStream(
          new FileInputStream("res.txt")));
      for(int station; (station = in.read()) != -1;) {
        final BusStation a = man.getForId(station);
        final BusEdge[][] routes = RouteFinder.findRoutesFrom(man, a,
            set, new BusTime(12, 0), 5, man.getMaxTimeHours() * BusTime.MINUTES_PER_HOUR);
        for(int toId; (toId = in.read()) < 128;) {
          final BusStation to = man.getForId(toId);
          final int duration = in.readInt();
          final BusEdge[] route = routes[to.getId()];
          if(duration == -1) {
            if(route != null) {
              System.out.println("Didn't expect route from " + a + " to " + to + ": " + route);
            }
          } else if(duration == 0) {
            if(route == null || station == toId && route.length != 0) {
              System.out.println("Route from and to " + a + " should be empty: " + route);
            }
          } else if(route == null
              || new BusTime(12, 0).minutesTo(route[route.length - 1].getEnd()) != duration) {
            System.out.println("Expected " + duration + "min from " + a + " to " + to
                + ": " + route);
          }
        }
      }
    }
  }

}
