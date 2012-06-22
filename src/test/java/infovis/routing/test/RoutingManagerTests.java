package infovis.routing.test;

import static org.junit.Assert.*;
import infovis.ctrl.Controller;
import infovis.data.BusDataBuilder;
import infovis.data.BusStationManager;
import infovis.data.BusTime;
import infovis.routing.RouteFinder;
import infovis.routing.RoutingManager;
import infovis.routing.RoutingManager.CallBack;
import infovis.routing.RoutingResult;

import java.util.Collection;
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
    final BusStationManager man = BusDataBuilder.load("src/main/resources/");
    final Controller ctrl = new Controller(man, null);
    final RoutingManager rm = RoutingManager.newInstance();
    final Semaphore sem = new Semaphore(0);
    final AtomicReference<Collection<RoutingResult>> ref =
        new AtomicReference<Collection<RoutingResult>>();

    rm.findRoutes(ctrl, man.getForId(1), null, new BusTime(12, 00), 1, 24 * 60,
        new RouteFinder(), new CallBack<Collection<RoutingResult>>() {
      @Override
      public void callBack(final Collection<RoutingResult> result) {
        ref.set(result);
        sem.release();
      }
    });

    sem.acquire();
    assertEquals(122, ref.get().size());
  }

  /**
   * Tests if a new task cancels routing.
   * 
   * @throws Exception exception
   */
  @Test
  public void terminateRouting() throws Exception {
    final BusStationManager man = BusDataBuilder.load("src/main/resources/");
    final Controller ctrl = new Controller(man, null);
    final CountDownLatch cd = new CountDownLatch(2);
    final AtomicBoolean ref = new AtomicBoolean(false);
    final RoutingManager rm = RoutingManager.newInstance();
    rm.registerTask(new Callable<Collection<RoutingResult>>() {
      @Override
      public Collection<RoutingResult> call() throws InterruptedException {
        cd.countDown();
        final Collection<RoutingResult> routes = new RouteFinder().findRoutes(ctrl,
            man.getForId(1), null, new BusTime(12, 00), 1, 24 * 60);
        ref.getAndSet(true); // should not exit normally
        return routes;
      }
    }, new CallBack<Collection<RoutingResult>>() {
      @Override
      public void callBack(final Collection<RoutingResult> result) {
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
