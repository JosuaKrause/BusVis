package infovis;

import infovis.data.BusDataBuilder;
import infovis.data.BusEdge;
import infovis.data.BusStation;
import infovis.data.BusStationManager;
import infovis.data.BusTime;
import infovis.gui.Canvas;
import infovis.gui.Context;
import infovis.gui.PainterAdapter;
import infovis.routing.RouteFinder;
import infovis.routing.RoutingResult;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import mdsj.StressMinimization;

/**
 * Test class.
 * 
 * @author Leo Woerteler
 */
public class Test extends PainterAdapter {
  /** Weight of the whole route's length. */
  private static final double ROUTE_WEIGHT = 1;
  /** Weight of each edges length. */
  private static final double EDGE_WEIGHT = 0.5;
  /** Focused station. */
  private static final int FOCUSED = 106;
  /** Scale factor. */
  private static final double SCALE = 8;

  /** Old station positions. */
  private final List<Ellipse2D> oldPoints;
  /** New station positions. */
  private final List<Ellipse2D> newPoints;

  /** Bus stations. */
  private final BusStationManager stations;

  /**
   * Constructor.
   * 
   * @param oldPoints old points
   * @param newPoints new points
   * @param stations stations
   */
  public Test(final List<Ellipse2D> oldPoints, final List<Ellipse2D> newPoints,
      final BusStationManager stations) {
    this.oldPoints = oldPoints;
    this.newPoints = newPoints;
    this.stations = stations;
  }

  /**
   * Main method.
   * 
   * @param args unused
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    final BusStationManager stations = BusDataBuilder.load("src/main/resources");
    final RoutingResult[] routes = new RouteFinder().findRoutes(stations,
        stations.getForId(FOCUSED), null, BusTime.MIDNIGHT, 3, 24 * 60, 15);
    final int n = stations.maxId() + 1;

    final double[][] geographical = new double[2][n];
    for(int id = 0; id < n; id++) {
      final BusStation st = stations.getForId(id);
      geographical[0][id] = st.getDefaultX() * SCALE;
      geographical[1][id] = st.getDefaultY() * SCALE;
    }
    center(geographical, FOCUSED);

    final double[][] optimized = geographical.clone();
    for(int i = 0; i < optimized.length; i++) {
      optimized[i] = optimized[i].clone();
    }

    final Pair<double[][], double[][]> distsPair = travelDists(routes, n);
    final double[][] dists = distsPair.getFirst(), weights = distsPair.getSecond();
    System.out.println(StressMinimization.majorize(optimized, dists, weights, 3, 0, 0));

    final double scaleMedian = medianLength(geographical) / medianLength(optimized);
    for(int i = 0; i < optimized[0].length; i++) {
      optimized[0][i] *= scaleMedian;
      optimized[1][i] *= scaleMedian;
    }
    center(optimized, FOCUSED);

    final List<Ellipse2D> points = new ArrayList<Ellipse2D>(), points2 = new ArrayList<Ellipse2D>();
    for(int i = 0; i < geographical[0].length; i++) {
      points.add(new Ellipse2D.Double(geographical[0][i] - 20, geographical[1][i] - 20, 40, 40));
      points2.add(new Ellipse2D.Double(optimized[0][i] - 20, optimized[1][i] - 20, 40, 40));
    }

    final Test t = new Test(points, points2, stations);
    final Canvas c = new Canvas(t, 800, 600);
    final JFrame frame = new JFrame("Test");
    frame.add(c);
    frame.pack();
    c.setBackground(Color.WHITE);
    c.reset(new Rectangle2D.Double(-1600, -1200, 3200, 2400));
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
  }

  /**
   * Calculates the distance and weight matrices for the given routes.
   * 
   * @param routes routes
   * @param n number of stations
   * @return pair of distance and weight matrix
   */
  private static Pair<double[][], double[][]> travelDists(final RoutingResult[] routes,
      final int n) {
    final BitSet[][] ds = new BitSet[n][n];
    final BitSet used = new BitSet();

    for(int id = 0; id < n; id++) {
      ds[id][id] = new BitSet();
      ds[id][id].set(0);
      used.set((n + 1) * id);
    }

    for(final RoutingResult res : routes) {
      if(!res.isStartNode() && !res.isNotReachable()) {
        BusTime before = res.getStartTime();
        for(final BusEdge e : res.getEdges()) {
          final int a = e.getFrom().getId(), b = e.getTo().getId();
          used.set(n * a + b);
          used.set(n * b + a);
          if(ds[a][b] == null) {
            ds[a][b] = ds[b][a] = new BitSet();
          }
          ds[a][b].set(before.minutesTo(e.getEnd()));
          before = e.getEnd();
        }
      }
    }

    final double[][] dists = new double[n][n], weights = new double[n][n];
    for(final double[] row : dists) {
      Arrays.fill(row, -1);
    }
    for(int i = used.nextSetBit(0); i >= 0; i = used.nextSetBit(i + 1)) {
      final int a = i / n, b = i % n;
      dists[a][b] = avg(ds[a][b]);
      weights[a][b] = EDGE_WEIGHT;
    }

    for(final RoutingResult res : routes) {
      if(!res.isStartNode() && !res.isNotReachable()) {
        final int a = res.getStart().getId(), b = res.getEnd().getId();
        final int length = res.minutes();
        dists[a][b] = dists[b][a] = length;
        weights[a][b] = weights[b][a] = ROUTE_WEIGHT;
      }
    }

    return new Pair<double[][], double[][]>(dists, weights);
  }

  @Override
  public void draw(final Graphics2D gfx, final Context ctx) {
    gfx.setColor(Color.GREEN);
    gfx.fill(new Ellipse2D.Double(-100, -100, 200, 200));
    for(final BusStation st : stations.getStations()) {
      final int id = st.getId();
      final Ellipse2D point = oldPoints.get(id);
      gfx.setColor(Color.RED);
      gfx.fill(point);
      final Ellipse2D point2 = newPoints.get(id);
      gfx.setColor(Color.BLUE);
      gfx.fill(point2);
      gfx.setColor(Color.BLACK);
      gfx.draw(new Line2D.Double(point.getCenterX(), point.getCenterY(),
          point2.getCenterX(), point2.getCenterY()));
      gfx.setFont(gfx.getFont().deriveFont(30F));
      gfx.drawString(st.getName(), (int) point.getCenterX(), (int) point.getCenterY());
    }
  }

  /**
   * Centers the given points around a given one.
   * 
   * @param points points
   * @param center position of point to center the points around
   */
  private static void center(final double[][] points, final int center) {
    final double centerX = points[0][center], centerY = points[1][center];
    for(int i = 0; i < points[0].length; i++) {
      points[0][i] -= centerX;
      points[1][i] -= centerY;
    }
  }

  /**
   * Average over all elements of a bit set.
   * 
   * @param set bit set
   * @return average
   */
  static final double avg(final BitSet set) {
    double res = 0;
    for(int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i + 1)) {
      res += i;
    }
    return res / set.cardinality();
  }

  /**
   * Calculates the median distance between all points.
   * 
   * @param points points
   * @return median distance
   */
  private static double medianLength(final double[][] points) {
    final int n = points[0].length;
    final ArrayList<Double> dists = new ArrayList<Double>();
    for(int i = 0; i < n; i++) {
      final double x1 = points[0][i], y1 = points[1][i];
      for(int j = 0; j < n; j++) {
        final double x2 = points[0][j], y2 = points[1][j];
        final double dx = x2 - x1, dy = y2 - y1;
        final double dist = Math.sqrt(dx * dx + dy * dy);
        dists.add(dist);
      }
    }

    Collections.sort(dists);
    final int m = dists.size();
    return (dists.get(m / 2) + dists.get((m + 1) / 2)) / 2;
  }

  /**
   * Simple immutable pair type.
   * 
   * @author Leo Woerteler
   * @param <F> type of the first element
   * @param <S> type of the second element
   */
  private static class Pair<F, S> {
    /** First element. */
    private final F first;
    /** Second element. */
    private final S second;

    /**
     * Constructor.
     * 
     * @param first first element
     * @param second second element
     */
    Pair(final F first, final S second) {
      this.first = first;
      this.second = second;
    }

    /**
     * Getter for the first element.
     * 
     * @return first element
     */
    public F getFirst() {
      return first;
    }

    /**
     * Getter for the second element.
     * 
     * @return second element
     */
    public S getSecond() {
      return second;
    }
  }
}
