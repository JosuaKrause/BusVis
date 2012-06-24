package infovis.routing;

import infovis.data.BusStation;
import infovis.data.BusStationEnumerator;
import infovis.data.BusStationManager;
import infovis.data.BusTime;

import java.util.BitSet;

/**
 * A routing algorithm finds routes to arbitrary bus stations.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface RoutingAlgorithm {

  /**
   * Finds shortest routes to all reachable stations from the given start
   * station at the given start time.
   * 
   * @param bse The bus station enumerator.
   * @param station start position
   * @param dests set of IDs of stations that should be reached,
   *          <code>null</code> means all stations of the start station's
   *          {@link BusStationManager}
   * @param start start time
   * @param wait waiting time when changing lines
   * @param maxDuration maximum allowed duration of a route
   * @param maxWalk maximum allowed continuous walking time
   * @return all routes
   * @throws InterruptedException if the current thread was interrupted during
   *           the computation
   */
  RoutingResult[] findRoutes(BusStationEnumerator bse,
      final BusStation station, final BitSet dests, final BusTime start, final int wait,
      final int maxDuration, final int maxWalk) throws InterruptedException;

}
