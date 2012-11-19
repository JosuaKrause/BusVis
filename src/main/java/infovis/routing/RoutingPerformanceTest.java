package infovis.routing;

import infovis.data.BusDataBuilder;
import infovis.data.BusEdge;
import infovis.data.BusStation;
import infovis.data.BusStationManager;
import infovis.data.BusTime;
import infovis.util.Resource;

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
import java.util.List;

/**
 * Performance tests for the routing algorithm.
 * 
 * @author Leo Woerteler
 */
public final class RoutingPerformanceTest {

  /** No constructor. */
  private RoutingPerformanceTest() {
    // no constructor
  }

  /**
   * A little performance test.
   * 
   * @param args No-args
   * @throws Exception No-exceptions
   */
  public static void main(final String[] args) throws Exception {
    final BusStationManager man = BusDataBuilder.load(new Resource("konstanz"));
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
              man.getMaxTimeHours() * BusTime.MINUTES_PER_HOUR, 0);
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
        final BusEdge[][] routes = RouteFinder.findRoutesFrom(man, a, set,
            new BusTime(12, 0), 5, man.getMaxTimeHours() * BusTime.MINUTES_PER_HOUR, 0);
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
        final BusEdge[][] routes = RouteFinder.findRoutesFrom(man, a, set,
            new BusTime(12, 0), 5, man.getMaxTimeHours() * BusTime.MINUTES_PER_HOUR, 0);
        for(int toId; (toId = in.read()) < 128;) {
          final BusStation to = man.getForId(toId);
          final int duration = in.readInt();
          final BusEdge[] route = routes[to.getId()];
          if(duration == -1) {
            if(route != null) {
              System.out.println("Didn't expect route from " + a + " to " + to + ": "
                  + route);
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
