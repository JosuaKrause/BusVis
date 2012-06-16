package infovis.routing.test;

import static org.junit.Assert.*;
import infovis.data.BusData;
import infovis.data.BusEdge;
import infovis.data.BusStation;
import infovis.data.BusStationManager;
import infovis.data.BusTime;
import infovis.routing.RoutingManager;
import infovis.routing.RoutingManager.CallBack;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

/**
 * Tests for the {@link RoutingManager} class.
 * 
 * @author Leo Woerteler
 */
public class RoutingManagerTests {

  /**
   * Tests if a new task cancels the old one.
   * 
   * @throws InterruptedException exception
   */
  @Test
  public void terminateTest() throws InterruptedException {
    final CountDownLatch cd = new CountDownLatch(2);
    final AtomicBoolean ref = new AtomicBoolean();
    final RoutingManager rm = RoutingManager.newInstance();
    rm.registerTask(new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        try {
          Thread.sleep(1000);
          return 1;
        } catch(final InterruptedException e) {
          ref.set(true);
          throw e;
        } finally {
          cd.countDown();
        }
      }

    }, new RoutingManager.CallBack<Integer>() {
      @Override
      public void callBack(final Integer result) {
        // never executed
      }
    });

    Thread.sleep(500);

    rm.registerTask(new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        return 2;
      }

    }, new RoutingManager.CallBack<Integer>() {
      @Override
      public void callBack(final Integer result) {
        cd.countDown();
      }
    });

    cd.await();
    assertTrue(ref.get());
  }

  /**
   * Tests if all routes are found.
   * 
   * @throws Exception exception
   */
  @Test
  public void findRoutes() throws Exception {
    final BusStationManager man = BusData.load("src/main/resources/");
    final RoutingManager rm = RoutingManager.newInstance();
    final Semaphore sem = new Semaphore(0);
    final AtomicReference<Map<BusStation, List<BusEdge>>> ref =
        new AtomicReference<Map<BusStation, List<BusEdge>>>();

    rm.findRoutes(man.getForId(1), null, new BusTime(12, 00), 1, 24 * 60,
        new CallBack<Map<BusStation, List<BusEdge>>>() {
      @Override
      public void callBack(final Map<BusStation, List<BusEdge>> result) {
        ref.set(result);
        sem.release();
      }
    });

    sem.acquire();
    assertEquals(122, ref.get().size());
  }
}
