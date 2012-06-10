package infovis.embed;

import infovis.data.BusEdge;
import infovis.data.BusStation;
import infovis.data.BusTime;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

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
   * The distances from the bus station.
   */
  private final Map<BusStation, Double> distance;

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
   * Creates a station distance without a reference station.
   */
  public StationDistance() {
    distance = new HashMap<BusStation, Double>();
    map = new HashMap<SpringNode, BusStation>();
    for(final BusStation s : BusStation.getStations()) {
      final SpringNode node = new SpringNode();
      node.setPosition(s.getDefaultX(), s.getDefaultY());
      map.put(node, s);
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
    distance.clear();
    if(from != null) {
      for(final BusStation s : BusStation.getStations()) {
        if(s.equals(from)) {
          continue;
        }
        final Deque<BusEdge> route = from.routeTo(s, time, changeTime);
        if(route == null) {
          continue;
        }
        final double t = time.minutesTo(route.getLast().getEnd());
        distance.put(s, t);
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

  private static final double MIN_DIST = 15;

  @Override
  public double weight(final SpringNode f, final SpringNode t) {
    if(from == null || t == f) return 0;
    final BusStation fr = map.get(f);
    if(fr.equals(from)) return 0;
    final BusStation to = map.get(t);
    if(to.equals(from)) {
      final Double d = distance.get(fr);
      if(d == null) return 0;
      return factor * d;
    }
    return -MIN_DIST;
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
    g.setColor(Color.BLACK);
    g.drawString(station.getName(), (int) x, (int) y);
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

}
