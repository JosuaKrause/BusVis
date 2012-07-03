package infovis.routing.test;

import static org.junit.Assert.*;
import infovis.data.BusDataBuilder;
import infovis.data.BusStationManager;
import infovis.data.BusTime;
import infovis.routing.RouteFinder;
import infovis.routing.RoutingManager;
import infovis.routing.RoutingManager.CallBack;
import infovis.routing.RoutingResult;

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
        throw new IllegalStateException("should never be executed");
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
    final BusStationManager man = BusDataBuilder.load("src/main/resources/konstanz/");
    final RoutingManager rm = RoutingManager.newInstance();
    final Semaphore sem = new Semaphore(0);
    final AtomicReference<RoutingResult[]> ref = new AtomicReference<RoutingResult[]>();

    rm.findRoutes(man, man.getForId(1), null, new BusTime(12, 00), 1, 24 * 60, 0,
        new RouteFinder(), new CallBack<RoutingResult[]>() {
          @Override
          public void callBack(final RoutingResult[] result) {
            ref.set(result);
            sem.release();
          }
        });

    sem.acquire();
    assertEquals(man.maxId() + 1, ref.get().length);
  }

  /**
   * Tests if a new task cancels routing.
   * 
   * @throws Exception exception
   */
  @Test
  public void terminateRouting() throws Exception {
    final BusStationManager man = BusDataBuilder.load("src/main/resources/konstanz/");
    final CountDownLatch cd = new CountDownLatch(2);
    final AtomicBoolean ref = new AtomicBoolean(false);
    final RoutingManager rm = RoutingManager.newInstance();
    rm.registerTask(new Callable<RoutingResult[]>() {
      @Override
      public RoutingResult[] call() throws InterruptedException {
        cd.countDown();
        final RoutingResult[] routes = new RouteFinder().findRoutes(man,
            man.getForId(1), null, new BusTime(12, 00), 1, 24 * 60, 0);
        ref.getAndSet(true); // should not exit normally
        return routes;
      }
    }, new CallBack<RoutingResult[]>() {
      @Override
      public void callBack(final RoutingResult[] result) {
        throw new IllegalStateException("should not come here");
      }
    });

    Thread.sleep(50);

    rm.registerTask(new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        return 0;
      }

    }, new RoutingManager.CallBack<Integer>() {
      @Override
      public void callBack(final Integer result) {
        cd.countDown();
      }
    });

    cd.await();
    assertFalse(ref.get());
  }

}
