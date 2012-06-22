package infovis.routing;

import infovis.data.BusStation;
import infovis.data.BusStationManager;
import infovis.data.BusTime;

import java.util.BitSet;
import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * A thread that calculates routes on demand.
 * 
 * @author Leo Woerteler
 */
public final class RoutingManager {

  /** Currently executed thread, may be {@code null}. */
  protected volatile Thread current;

  /** Hidden default constructor. */
  private RoutingManager() {
    // empty
  }

  /**
   * Creates a new routing manager.
   * 
   * @return new instance
   */
  public static RoutingManager newInstance() {
    return new RoutingManager();
  }

  /**
   * Registers the given request for routes with the routing thread. If the
   * routing finishes without being interrupted, the given callback is called
   * with the result as argument.
   * 
   * @param bsm The bus station manager.
   * @param station start station
   * @param dests IDs of requested destinations
   * @param start start time
   * @param wait minimum waiting time when changing bus lines
   * @param maxDuration maximum time in minutes that a route may take
   * @param algo The routing algorithm.
   * @param call callback for results
   */
  public void findRoutes(final BusStationManager bsm, final BusStation station,
      final BitSet dests,
      final BusTime start, final int wait, final int maxDuration,
      final RoutingAlgorithm algo, final CallBack<Collection<RoutingResult>> call) {
    registerTask(new Callable<Collection<RoutingResult>>() {
      @Override
      public Collection<RoutingResult> call() throws InterruptedException {
        return algo.findRoutes(bsm, station, dests, start, wait, maxDuration);
      }
    }, call);
  }

  /**
   * Registers a new task and tries to terminate the old one if one exists.
   * 
   * @param <T> result type
   * @param task task to be registered
   * @param callback callback to be called
   */
  public synchronized <T> void registerTask(final Callable<T> task,
      final CallBack<T> callback) {
    // try to terminate the old computation early
    if(current != null) {
      current.interrupt();
    }

    current = new Thread() {
      {
        setDaemon(true);
      }

      @Override
      public void run() {
        try {
          final T res = task.call();
          synchronized(RoutingManager.this) {
            if(current == this) {
              callback.callBack(res);
            }
          }
        } catch(final InterruptedException e) {
          // terminate
        } catch(final Exception e) {
          // TODO what to do here?
          e.printStackTrace();
        }
      }
    };
    current.start();
  }

  /**
   * Interface for callbacks that are called when the result was calculated.
   * 
   * @author Leo Woerteler
   * @param <T> result type
   */
  public interface CallBack<T> {
    /**
     * Method that's called when the routes are calculated.
     * 
     * @param result result
     */
    void callBack(final T result);
  }
}
