package infovis.routing.test;

import static org.junit.Assert.*;
import infovis.data.BusData;
import infovis.data.BusEdge;
import infovis.data.BusLine;
import infovis.data.BusStation;
import infovis.data.BusStationManager;
import infovis.data.BusTime;
import infovis.routing.FastRouteFinder;

import java.awt.Color;
import java.util.Arrays;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

/**
 * Tests for the {@link FastRouteFinder} class.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class FastRouteFinderTests {

  /**
   * Checks if the line is changed when advantageous.
   */
  @Test
  public void shouldChange() {
    final BusStationManager man = new BusStationManager(null);
    final BusLine s1 = new BusLine("B1", Color.RED), s2 = new BusLine("B2",
        Color.BLUE);
    final BusStation a = man.createStation("A", 0, 0, 0, 0, 0), b =
        man.createStation("B", 1, 0, 0, 0, 0), c = man.createStation("C", 2, 0, 0, 0, 0);

    final BusEdge ab = a.addEdge(s1, 1, b, new BusTime(0, 0), new BusTime(0, 1));
    b.addEdge(s1, 1, c, new BusTime(0, 1), new BusTime(0, 5));
    final BusEdge bc = b.addEdge(s2, 1, c, new BusTime(0, 3), new BusTime(0, 4));

    final Deque<BusEdge> route = FastRouteFinder.routeTo(a, c, new BusTime(0, 0), 2,
        man.getMaxTimeHours() * 60);

    assertEquals(Arrays.asList(ab, bc), route);
  }

  /**
   * Tests the routing.
   */
  @Test
  public void generalTest() {
    final BusStationManager manager = new BusStationManager(null);
    final int mth = manager.getMaxTimeHours() * BusTime.MINUTES_PER_HOUR;
    final BusLine line = new BusLine("1", Color.RED);
    final BusStation a = manager.createStation("a", 0, 0, 0, 0, 0);
    final BusStation b = manager.createStation("b", 1, 0, 0, 0, 0);
    final BusStation c = manager.createStation("c", 2, 0, 0, 0, 0);
    final BusStation d = manager.createStation("d", 3, 0, 0, 0, 0);
    final BusStation e = manager.createStation("e", 4, 0, 0, 0, 0);
    a.addEdge(line, 0, c, new BusTime(3, 10), new BusTime(3, 13));
    a.addEdge(line, 1, b, new BusTime(3, 10), new BusTime(3, 12));
    a.addEdge(line, 2, d, new BusTime(3, 10), new BusTime(3, 11));
    b.addEdge(line, 0, a, new BusTime(3, 10), new BusTime(3, 20));
    b.addEdge(line, 2, c, new BusTime(3, 9), new BusTime(3, 10));
    c.addEdge(line, 0, a, new BusTime(2, 0), new BusTime(2, 1));
    d.addEdge(line, 5, a, new BusTime(0, 1), new BusTime(0, 2));
    d.addEdge(line, 6, b, new BusTime(0, 2), new BusTime(0, 3));
    d.addEdge(line, 7, c, new BusTime(0, 3), new BusTime(0, 4));
    d.addEdge(line, 8, e, new BusTime(0, 4), new BusTime(0, 5));
    final Deque<BusEdge> routeTo = FastRouteFinder.routeTo(c, e, new BusTime(2, 0), 0,
        mth);
    final int[] ids = { 2, 0, 3, 4};
    int i = 0;
    assertEquals(ids[i++], routeTo.getFirst().getFrom().getId());
    for(final BusEdge edge : routeTo) {
      assertEquals(ids[i++], edge.getTo().getId());
    }
    assertNull(FastRouteFinder.routeTo(e, c, new BusTime(2, 0), 0, mth));
  }

  /**
   * Tests with line changes and different max time.
   */
  @Test
  public void lineChanging() {
    final BusStationManager manager = new BusStationManager(null);
    final int mth = manager.getMaxTimeHours() * BusTime.MINUTES_PER_HOUR;
    final BusLine line = new BusLine("1", Color.RED);
    final BusLine other = new BusLine("2", Color.BLUE);
    final BusStation e = manager.createStation("e", 4, 0, 0, 0, 0);
    final BusStation f = manager.createStation("f", 5, 0, 0, 0, 0);
    final BusStation g = manager.createStation("g", 6, 0, 0, 0, 0);
    final BusStation h = manager.createStation("h", 7, 0, 0, 0, 0);
    e.addEdge(line, 0, h, new BusTime(23, 59), new BusTime(0, 1));
    e.addEdge(line, 1, h, new BusTime(0, 7), new BusTime(0, 0));
    e.addEdge(line, 2, h, new BusTime(0, 0), new BusTime(0, 6));
    e.addEdge(line, 3, h, new BusTime(0, 6), new BusTime(0, 8));
    e.addEdge(line, 4, h, new BusTime(0, 50), new BusTime(1, 0));
    e.addEdge(line, 5, f, new BusTime(0, 0), new BusTime(0, 2));
    e.addEdge(other, 0, f, new BusTime(0, 0), new BusTime(0, 1));
    e.addEdge(line, 6, g, new BusTime(0, 1), new BusTime(0, 3));
    f.addEdge(line, 7, h, new BusTime(1, 2), new BusTime(1, 3));
    f.addEdge(line, 5, h, new BusTime(0, 2), new BusTime(0, 5));
    g.addEdge(other, 0, h, new BusTime(0, 3), new BusTime(0, 4));
    g.addEdge(line, 6, h, new BusTime(0, 4), new BusTime(0, 7));
    g.addEdge(line, 8, h, new BusTime(0, 1), new BusTime(0, 2));
    assertEquals(4,
        getLastEndMinute(FastRouteFinder.routeTo(e, h, new BusTime(0, 0), 0, mth)));
    assertEquals(5,
        getLastEndMinute(FastRouteFinder.routeTo(e, h, new BusTime(0, 0), 1, mth)));
    assertNull(FastRouteFinder.routeTo(e, h, new BusTime(0, 0), 0, 0));
    assertEquals(4, getLastEndMinute(FastRouteFinder.routeTo(e, h, new BusTime(0, 0), 0,
        BusTime.MINUTES_PER_HOUR)));
    assertEquals(5, getLastEndMinute(FastRouteFinder.routeTo(e, h, new BusTime(0, 0), 1,
        BusTime.HOURS_PER_DAY * BusTime.MINUTES_PER_HOUR)));
  }

  /**
   * The end point of the last edge.
   * 
   * @param route The route.
   * @return The end point.
   */
  private static BusTime getLastEnd(final Deque<BusEdge> route) {
    return !route.isEmpty() ? route.getLast().getEnd() : null;
  }

  /**
   * The minute of the end point of the last edge.
   * 
   * @param route The route.
   * @return The minute of the end point.
   */
  private static int getLastEndMinute(final Deque<BusEdge> route) {
    return getLastEnd(route).getMinute();
  }

  /**
   * Finds all shortest routes from all stations at 12:00 AM. This test checks
   * also the performance by letting each route take up to 100 milliseconds.
   * 
   * @throws Exception exception
   */
  @Test
  public void at12Am() throws Exception {
    final BusStationManager man = BusData.load("src/main/resources");
    final AtomicBoolean fail = new AtomicBoolean(false);
    int num = 0;
    for(@SuppressWarnings("unused")
    final BusStation a : man.getStations()) {
      ++num;
    }
    final int count = num;
    final Thread t = new Thread() {

      @Override
      public void run() {
        try {
          synchronized(this) {
            wait(count * 100);
            fail.getAndSet(true);
          }
        } catch(final InterruptedException e) {
          // interrupted
        }
      }

    };
    t.start();

    for(final BusStation a : man.getStations()) {
      if(fail.get()) {
        System.out.println("failed");
        break;
      }
      System.out.println(a
          + ", "
          + FastRouteFinder.routes(a, new BusTime(12, 0), 5,
              man.getMaxTimeHours() * BusTime.MINUTES_PER_HOUR).size());
    }

    t.interrupt();
    assertFalse(fail.get());
  }

}
