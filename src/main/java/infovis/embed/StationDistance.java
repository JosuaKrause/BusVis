package infovis.embed;

import static infovis.VecUtil.*;
import infovis.ctrl.Controller;
import infovis.data.BusStation;
import infovis.data.BusStation.Route;
import infovis.data.BusTime;
import infovis.embed.pol.Interpolator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Weights the station network after the distance from one start station.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class StationDistance implements Weighter, NodeDrawer {

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
  protected volatile Map<BusStation, Route> routes;

  /**
   * The current reference time.
   */
  protected BusTime time = new BusTime(12, 0);

  /**
   * The change time for lines.
   */
  protected int changeTime = 5;

  /**
   * The start bus station or <code>null</code> if there is none.
   */
  protected BusStation from;

  /**
   * The factor to scale the distances.
   */
  private double factor = .1;

  /**
   * The controller.
   */
  private final Controller ctrl;

  /**
   * Creates a station distance without a reference station.
   * 
   * @param ctrl The controller.
   */
  public StationDistance(final Controller ctrl) {
    this.ctrl = ctrl;
    routes = new HashMap<BusStation, Route>();
    map = new HashMap<SpringNode, BusStation>();
    rev = new HashMap<BusStation, SpringNode>();
    for(final BusStation s : ctrl.getStations()) {
      final SpringNode node = new SpringNode();
      node.setPosition(s.getDefaultX(), s.getDefaultY());
      map.put(node, s);
      rev.put(s, node);
    }
  }

  /**
   * The predicted next bus station.
   */
  protected volatile BusStation predict;

  /**
   * The current waiter thread.
   */
  protected volatile Thread currentCalculator;

  /**
   * The fading bus station.
   */
  protected BusStation fadeOut;

  /**
   * The fading start time.
   */
  protected long fadingStart;

  /**
   * The fading end time.
   */
  protected long fadingEnd;

  /**
   * Whether we do fade currently.
   */
  protected boolean fade;

  /**
   * Sets the values for the distance.
   * 
   * @param from The reference station.
   * @param time The reference time.
   * @param changeTime The change time.
   */
  public void set(final BusStation from, final BusTime time, final int changeTime) {
    predict = from;
    final Thread t = new Thread() {

      @Override
      public void run() {
        final Map<BusStation, Route> route = new HashMap<BusStation, Route>();
        if(from != null) {
          final Collection<Route> routes = from.routes(time, changeTime);
          for(final Route r : routes) {
            route.put(r.getStation(), r);
          }
        }
        synchronized(StationDistance.this) {
          if(currentCalculator != this) return;
          routes = route;
          if(from != StationDistance.this.from) {
            fadeOut = StationDistance.this.from;
            fadingStart = System.currentTimeMillis();
            fadingEnd = fadingStart + Interpolator.DURATION;
            fade = true;
          }
          StationDistance.this.from = from;
          StationDistance.this.time = time;
          StationDistance.this.changeTime = changeTime;
          changed = true;
        }
      }

    };
    t.setDaemon(true);
    currentCalculator = t;
    t.start();
  }

  /**
   * Whether the weights have changed.
   */
  protected volatile boolean changed;

  @Override
  public boolean hasChanged() {
    final boolean res = changed;
    changed = false;
    return res;
  }

  /**
   * Signals undefined changes.
   */
  public void changeUndefined() {
    set(from, time, changeTime);
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
      final Integer d = routes.get(fr).minutes();
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
    if(to.equals(from)) return !routes.get(fr).isNotReachable();
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
    final Route route = routes.get(station);
    if(route != null && route.isNotReachable()) return;
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
      ctrl.selectStation(station);
    }
    n.setPosition(startX + dx, startY + dy);
  }

  @Override
  public void selectNode(final SpringNode n) {
    final BusStation station = map.get(n);
    if(!station.equals(from)) {
      ctrl.selectStation(station);
    }
  }

  @Override
  public Shape nodeClickArea(final SpringNode n) {
    return new Ellipse2D.Double(n.getX() - 5, n.getY() - 5, 10, 10);
  }

  @Override
  public void drawBackground(final Graphics2D g) {
    final SpringNode ref = getReferenceNode();
    if(ref == null && !fade) return;
    Point2D center;
    Color col;
    if(fade) {
      final long time = System.currentTimeMillis();
      final double t = ((double) time - fadingStart) / ((double) fadingEnd - fadingStart);
      final double f = Interpolator.INTERPOLATOR.interpolate(t);
      final SpringNode n = f > 0.5 ? ref : rev.get(fadeOut);
      center = n != null ? n.getPos() : null;
      final double split = f > 0.5 ? (f - 0.5) * 2 : 1 - f * 2;
      final int alpha = Math.max(0, Math.min((int) (split * 255), 255));
      col = new Color(alpha << 24
          | (Color.LIGHT_GRAY.getRGB() & 0x00ffffff), true);
      if(t >= 1.0) {
        fadeOut = null;
        fade = false;
      }
    } else {
      center = ref.getPos();
      col = Color.LIGHT_GRAY;
    }
    if(center == null) return;
    boolean b = true;
    for(int i = MAX_INTERVAL; i > 0; --i) {
      final Shape circ = getCircle(i, center);
      g.setColor(b ? col : Color.WHITE);
      b = !b;
      g.fill(circ);
    }
  }

  /**
   * The highest drawn circle interval.
   */
  public static final int MAX_INTERVAL = 12;

  /**
   * Getter.
   * 
   * @param i The interval.
   * @param center The center of the circle.
   * @return The circle.
   */
  public Ellipse2D getCircle(final int i, final Point2D center) {
    final Point2D c = center != null ? center : getReferenceNode().getPos();
    final double radius = factor * 5 * i;
    final double r2 = radius * 2;
    return new Ellipse2D.Double(c.getX() - radius, c.getY() - radius, r2, r2);
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
      final Route route = routes.get(station);
      if(!route.isNotReachable()) {
        dist = " (" + BusTime.minutesToString(route.minutes()) + ")";
      } else {
        dist = " (not reachable)";
      }
    } else {
      dist = "";
    }
    return station.getName() + dist;
  }

  @Override
  public void moveMouse(final Point2D cur) {
    if(from != null) {
      ctrl.setTitle(BusTime.minutesToString((int) Math.ceil(getLength(subVec(cur,
          getReferenceNode().getPos())) / factor)));
    } else {
      ctrl.setTitle(null);
    }
  }

  /**
   * Getter.
   * 
   * @return The predicted next bus station.
   */
  public BusStation getPredict() {
    return predict;
  }

  /**
   * Getter.
   * 
   * @param station The station.
   * @return The corresponding node.
   */
  public SpringNode getNode(final BusStation station) {
    return station == null ? null : rev.get(station);
  }

}
