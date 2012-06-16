package infovis.routing.test;

import static org.junit.Assert.*;
import infovis.data.BusData;
import infovis.data.BusEdge;
import infovis.data.BusLine;
import infovis.data.BusStation;
import infovis.data.BusStationManager;
import infovis.data.BusTime;
import infovis.routing.RouteFinder;

import java.awt.Color;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import org.junit.Test;

/**
 * Tests for the {@link RouteFinder} class.
 * 
 * @author Leo Woerteler
 */
public class RouteFinderTests {

  /**
   * Checks if the line is changed when advantageous.
   * 
   * @throws InterruptedException exception
   */
  @Test
  public void shouldChange() throws InterruptedException {
    final BusStationManager man = new BusStationManager();
    final BusLine s1 = new BusLine("B1", Color.RED), s2 = new BusLine("B2",
        Color.BLUE);
    final BusStation a = man.createStation("A", 0, 0, 0, 0, 0), b =
        man.createStation("B", 1, 0, 0, 0, 0), c = man.createStation("C", 2, 0, 0, 0, 0);

    final BusEdge ab = a.addEdge(s1, 1, b, new BusTime(0, 0), new BusTime(0, 1));
    b.addEdge(s1, 1, c, new BusTime(0, 1), new BusTime(0, 5));
    final BusEdge bc = b.addEdge(s2, 1, c, new BusTime(0, 3), new BusTime(0, 4));

    final List<BusEdge> route = RouteFinder.findRoute(a, c, new BusTime(0, 0), 2,
        man.getMaxTimeHours() * 60);

    assertEquals(Arrays.asList(ab, bc), route);
  }

  /**
   * Tests if routes without changes are taken if suitable.
   * 
   * <pre>
   *    , 00:00 ---1--- 00:01.
   *   /                     \
   * (A) 00:01 ---2---> 00:02 (B) 00:02 ---2---> 00:03 (C)
   *                            \                      /
   *                             ` 00:03 ---3--- 00:04´
   * </pre>
   * 
   * @throws InterruptedException exception
   */
  @Test
  public void continuous() throws InterruptedException {
    final BusStationManager man = new BusStationManager();
    final BusLine s1 = new BusLine("B1", Color.RED), s2 = new BusLine("B2",
        Color.BLUE), s3 = new BusLine("B3", Color.YELLOW);
    final BusStation a = man.createStation("A", 0, 0, 0, 0, 0), b =
        man.createStation(
            "B", 1, 0, 0, 0, 0), c = man.createStation("C", 2, 0, 0, 0, 0);

    a.addEdge(s1, 1, b, new BusTime(0, 0), new BusTime(0, 1));

    final BusEdge ab = a.addEdge(s2, 1, b, new BusTime(0, 1), new BusTime(0, 2));
    final BusEdge bc = b.addEdge(s2, 1, c, new BusTime(0, 2), new BusTime(0, 3));

    b.addEdge(s3, 1, c, new BusTime(0, 3), new BusTime(0, 4));

    final List<BusEdge> route = RouteFinder.findRoute(a, c, new BusTime(0, 0),
        5, man.getMaxTimeHours() * 60);

    assertEquals(Arrays.asList(ab, bc), route);
  }

  /**
   * Finds all shortest routes from all stations at 12:00 AM.
   * 
   * @throws Exception exception
   */
  @Test
  public void at12Am() throws Exception {
    final BusStationManager man = BusData.load("src/main/resources");
    final BitSet set = new BitSet();
    for(final BusStation a : man.getStations()) {
      set.set(a.getId());
    }

    for(final BusStation a : man.getStations()) {
      System.out.println(a
          + ", "
          + RouteFinder.findRoutes(a, set, new BusTime(12, 0), 5,
              man.getMaxTimeHours() * 60).size());
    }
  }
}
