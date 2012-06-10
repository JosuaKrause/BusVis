package infovis.embed;

import infovis.data.BusEdge;
import infovis.data.BusStation;
import infovis.data.BusTime;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Weights the station network after the distance from one start station.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class StationDistance implements Weighter {

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
  private double factor = 15.0;

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

  @Override
  public double weight(final SpringNode f, final SpringNode t) {
    if(from == null || t == f) return 0;
    final BusStation to = map.get(t);
    if(to.equals(from)) return weight(t, f);
    final BusStation fr = map.get(f);
    if(fr.equals(from)) {
      final Double d = distance.get(to);
      if(d == null) return 0;
      return factor * d;
    }
    return -factor;
  }

  @Override
  public boolean hasWeight(final SpringNode f, final SpringNode t) {
    if(from == null || t == f) return false;
    final BusStation to = map.get(t);
    if(to.equals(from)) return hasWeight(t, f);
    final BusStation fr = map.get(f);
    if(fr.equals(from)) return distance.containsKey(to);
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

}
