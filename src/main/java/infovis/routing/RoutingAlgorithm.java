package infovis.routing;

import infovis.ctrl.Controller;
import infovis.data.BusStation;
import infovis.data.BusStationManager;
import infovis.data.BusTime;

import java.util.BitSet;
import java.util.Collection;

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
   * @param ctrl The controller.
   * @param station start position
   * @param dests set of IDs of stations that should be reached,
   *          <code>null</code> means all stations of the start station's
   *          {@link BusStationManager}
   * @param start start time
   * @param wait waiting time when changing lines
   * @param maxDuration maximum allowed duration of a route
   * @return all routes
   * @throws InterruptedException if the current thread was interrupted during
   *           the computation
   */
  Collection<RoutingResult> findRoutes(Controller ctrl,
      final BusStation station, final BitSet dests, final BusTime start, final int wait,
      final int maxDuration) throws InterruptedException;

}
