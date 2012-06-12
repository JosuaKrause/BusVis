package infovis.embed;

import infovis.data.BusEdge;
import infovis.data.BusStation;
import infovis.data.BusStationManager;
import infovis.data.BusTime;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Weights the station network after the distance from one start station.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class StationDistance implements Weighter, NodeDrawer {

  /**
   * The backing map for the spring nodes.
   */
  private final Map<SpringNode, BusStation> map;

  /**
   * The reverse backing map for the spring nodes.
   */
  private final Map<BusStation, SpringNode> rev;

  /**
   * The distances from the bus station.
   */
  private final Map<BusStation, Integer> distance;

  /**
   * The current reference time.
   */
  private BusTime time = new BusTime(12, 0);

  /**
   * The change time for lines.
   */
  private int changeTime = 5;

  /**
   * The start bus station or <code>null</code> if there is none.
   */
  private BusStation from;

  /**
   * The factor to scale the distances.
   */
  private double factor = .1;

  /**
   * The bus station manager.
   */
  private final BusStationManager manager;

  /**
   * Creates a station distance without a reference station.
   * 
   * @param manager The bus station manager.
   */
  public StationDistance(final BusStationManager manager) {
    this.manager = manager;
    distance = new ConcurrentHashMap<BusStation, Integer>();
    map = new HashMap<SpringNode, BusStation>();
    rev = new HashMap<BusStation, SpringNode>();
    for(final BusStation s : manager.getStations()) {
      final SpringNode node = new SpringNode();
      node.setPosition(s.getDefaultX(), s.getDefaultY());
      map.put(node, s);
      rev.put(s, node);
    }
  }

  /**
   * Sets the values for the distance.
   * 
   * @param from The reference station.
   * @param time The reference time.
   * @param changeTime The change time.
   */
  public void set(final BusStation from, final BusTime time, final int changeTime) {
    final Map<BusStation, Integer> dist = distance;
    dist.clear();
    if(from != null) {
      final ExecutorService pool = Executors.newCachedThreadPool();
      for(final BusStation s : manager.getStations()) {
        if(s.equals(from)) {
          continue;
        }
        pool.execute(new Runnable() {

          @Override
          public void run() {
            final Deque<BusEdge> route = from.routeTo(s, time, changeTime);
            if(route == null) return;
            dist.put(s, time.minutesTo(route.getLast().getEnd()));
          }

        });
      }
      pool.shutdown();
      try {
        while(!pool.isTerminated()) {
          pool.awaitTermination(1, TimeUnit.SECONDS);
        }
      } catch(final InterruptedException e) {
        pool.shutdownNow();
        Thread.currentThread().interrupt();
        return;
      }
    } else {
      for(final Entry<SpringNode, BusStation> e : map.entrySet()) {
        final BusStation station = e.getValue();
        e.getKey().setPosition(station.getDefaultX(), station.getDefaultY());
      }
    }
    this.from = from;
    this.time = time;
    this.changeTime = changeTime;
  }

  /**
   * Setter.
   * 
   * @param from Sets the reference station.
   */
  public void setFrom(final BusStation from) {
    set(from, time, changeTime);
  }

  /**
   * Getter.
   * 
   * @return The reference station.
   */
  public BusStation getFrom() {
    return from;
  }

  /**
   * Setter.
   * 
   * @param time Sets the reference time.
   */
  public void setTime(final BusTime time) {
    set(from, time, changeTime);
  }

  /**
   * Getter.
   * 
   * @return The reference time.
   */
  public BusTime getTime() {
    return time;
  }

  /**
   * Setter.
   * 
   * @param changeTime Sets the change time.
   */
  public void setChangeTime(final int changeTime) {
    set(from, time, changeTime);
  }

  /**
   * Getter.
   * 
   * @return The change time.
   */
  public int getChangeTime() {
    return changeTime;
  }

  /**
   * Setter.
   * 
   * @param factor Sets the distance factor.
   */
  public void setFactor(final double factor) {
    this.factor = factor;
  }

  /**
   * Getter.
   * 
   * @return The distance factor.
   */
  public double getFactor() {
    return factor;
  }

  /**
   * The minimal distance between nodes.
   */
  private double minDist = 15;

  /**
   * Setter.
   * 
   * @param minDist Sets the minimal distance between nodes.
   */
  public void setMinDist(final double minDist) {
    this.minDist = minDist;
  }

  /**
   * Getter.
   * 
   * @return The minimal distance between nodes.
   */
  public double getMinDist() {
    return minDist;
  }

  @Override
  public double weight(final SpringNode f, final SpringNode t) {
    if(from == null || t == f) return 0;
    final BusStation fr = map.get(f);
    if(fr.equals(from)) return 0;
    final BusStation to = map.get(t);
    if(to.equals(from)) {
      final Integer d = distance.get(fr);
      if(d == null) return 0;
      return factor * d;
    }
    return -minDist;
  }

  @Override
  public boolean hasWeight(final SpringNode f, final SpringNode t) {
    if(from == null || t == f) return false;
    final BusStation fr = map.get(f);
    if(fr.equals(from)) return false;
    final BusStation to = map.get(t);
    if(to.equals(from)) return distance.containsKey(fr);
    return true;
  }

  @Override
  public Iterable<SpringNode> nodes() {
    return map.keySet();
  }

  @Override
  public double springConstant() {
    return 0.75;
  }

  @Override
  public void drawNode(final Graphics2D g, final SpringNode n) {
    final BusStation station = map.get(n);
    g.setColor(!station.equals(from) ? Color.BLUE : Color.RED);
    g.fill(nodeClickArea(n));
    final double x = n.getX();
    final double y = n.getY();
    final Graphics2D gfx = (Graphics2D) g.create();
    gfx.setColor(Color.BLACK);
    gfx.translate(x, y);
    gfx.drawString(station.getName(), 0, 0);
    gfx.dispose();
  }

  @Override
  public void dragNode(final SpringNode n, final double startX, final double startY,
      final double dx, final double dy) {
    final BusStation station = map.get(n);
    if(!station.equals(from)) {
      setFrom(station);
    }
    n.setPosition(startX + dx, startY + dy);
  }

  @Override
  public Shape nodeClickArea(final SpringNode n) {
    return new Ellipse2D.Double(n.getX() - 5, n.getY() - 5, 10, 10);
  }

  @Override
  public void drawBackground(final Graphics2D g) {
    final SpringNode ref = getReferenceNode();
    if(ref == null) return;
    final Point2D center = ref.getPos();
    boolean b = true;
    for(int i = 12; i > 0; --i) {
      final double radius = factor * 5 * i;
      final double r2 = radius * 2;
      final Ellipse2D circ = new Ellipse2D.Double(center.getX() - radius, center.getY()
          - radius, r2, r2);
      g.setColor(b ? Color.LIGHT_GRAY : Color.WHITE);
      b = !b;
      g.fill(circ);
    }
  }

  @Override
  public Point2D getDefaultPosition(final SpringNode node) {
    final BusStation station = map.get(node);
    return new Point2D.Double(station.getDefaultX(), station.getDefaultY());
  }

  @Override
  public SpringNode getReferenceNode() {
    return from == null ? null : rev.get(from);
  }

  @Override
  public String getTooltipText(final SpringNode node) {
    final BusStation station = map.get(node);
    String dist;
    if(from != null && from != station) {
      final SpringNode ref = getReferenceNode();
      if(hasWeight(node, ref)) {
        dist = " (" + BusTime.minutesToString(distance.get(station)) + ")";
      } else {
        dist = " (not reachable)";
      }
    } else {
      dist = "";
    }
    return station.getName() + dist;
  }

}
