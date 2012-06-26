package infovis.embed;

import static infovis.util.VecUtil.*;
import infovis.ctrl.Controller;
import infovis.data.BusEdge;
import infovis.data.BusLine;
import infovis.data.BusStation;
import infovis.data.BusTime;
import infovis.data.EdgeMatrix;
import infovis.data.EdgeMatrix.UndirectedEdge;
import infovis.gui.Context;
import infovis.routing.RoutingManager;
import infovis.routing.RoutingManager.CallBack;
import infovis.routing.RoutingResult;
import infovis.util.Interpolator;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import setvis.bubbleset.BubbleSet;
import setvis.shape.AbstractShapeGenerator;
import setvis.shape.BSplineShapeGenerator;
import setvis.shape.PolygonShapeGenerator;
import setvis.shape.ShapeSimplifier;

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
  private final SpringNode[] rev;

  /**
   * Dummy routes for uninitialized routings.
   */
  private final RoutingResult[] dummyRoutes;

  /**
   * The routes from the bus station, may be <code>null</code>.
   */
  protected volatile RoutingResult[] routes;

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
  protected final Controller ctrl;

  /**
   * The undirected edge matrix.
   */
  private final EdgeMatrix matrix;

  /**
   * Creates a station distance without a reference station.
   * 
   * @param ctrl The controller.
   */
  public StationDistance(final Controller ctrl) {
    this.ctrl = ctrl;
    matrix = new EdgeMatrix(ctrl.getBusStationManager());
    dummyRoutes = new RoutingResult[ctrl.maxId() + 1];
    for(int id = 0; id < dummyRoutes.length; ++id) {
      dummyRoutes[id] = new RoutingResult(ctrl.getForId(id)); // dummy results
    }
    routes = dummyRoutes;
    map = new HashMap<SpringNode, BusStation>();
    rev = new SpringNode[ctrl.maxId() + 1];
    for(final BusStation s : ctrl.getStations()) {
      final SpringNode node = new SpringNode();
      node.setPosition(s.getDefaultX(), s.getDefaultY());
      map.put(node, s);
      rev[s.getId()] = node;
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
   * The routing manager.
   */
  private final RoutingManager rm = RoutingManager.newInstance();

  /**
   * The animator to be notified when something has changed.
   */
  private Animator animator;

  /**
   * Sets the values for the distance.
   * 
   * @param from The reference station.
   * @param time The reference time.
   * @param changeTime The change time.
   */
  public void set(final BusStation from, final BusTime time, final int changeTime) {
    predict = from;
    if(from == null) {
      putSettings(dummyRoutes, from, time, changeTime);
      return;
    }
    final CallBack<RoutingResult[]> cb = new CallBack<RoutingResult[]>() {

      @Override
      public void callBack(final RoutingResult[] result) {
        putSettings(result, from, time, changeTime);
      }

    };
    rm.findRoutes(ctrl, from, null, time != null ? time : BusTime.now(), changeTime,
        ctrl.getMaxTimeHours() * BusTime.MINUTES_PER_HOUR, ctrl.getWalkTime(),
        ctrl.getRoutingAlgorithm(), cb);
  }

  /**
   * Puts the new settings.
   * 
   * @param route The routes.
   * @param from The start station.
   * @param time The start time.
   * @param changeTime The change time.
   */
  protected synchronized void putSettings(final RoutingResult[] route,
      final BusStation from, final BusTime time, final int changeTime) {
    routes = route;
    matrix.refreshHighlights(routes);
    if(from != StationDistance.this.from) {
      fadeOut = StationDistance.this.from;
      fadingStart = System.currentTimeMillis();
      fadingEnd = fadingStart + Interpolator.NORMAL;
      fade = true;
    }
    changes = ((time != null && StationDistance.this.time != null) &&
        (StationDistance.this.time != time || StationDistance.this.changeTime != changeTime))
        ? FAST_ANIMATION_CHANGE : NORMAL_CHANGE;
    StationDistance.this.from = from;
    StationDistance.this.time = time;
    StationDistance.this.changeTime = changeTime;
    animator.forceNextFrame();
  }

  @Override
  public void setAnimator(final Animator animator) {
    this.animator = animator;
  }

  /**
   * Whether the weights have changed.
   */
  protected volatile int changes;

  @Override
  public int changes() {
    final int res = changes;
    changes = NO_CHANGE;
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

  /**
   * Getter.
   * 
   * @param s The station.
   * @return The route to the station.
   */
  private RoutingResult getRoute(final BusStation s) {
    return routes[s.getId()];
  }

  @Override
  public double weight(final SpringNode f, final SpringNode t) {
    if(from == null || t == f) return 0;
    final BusStation fr = map.get(f);
    if(fr.equals(from)) return 0;
    final BusStation to = map.get(t);
    if(to.equals(from)) return factor * getRoute(fr).minutes();
    return -minDist;
  }

  @Override
  public boolean hasWeight(final SpringNode f, final SpringNode t) {
    if(from == null || t == f) return false;
    final BusStation fr = map.get(f);
    if(fr.equals(from)) return false;
    final BusStation to = map.get(t);
    if(to.equals(from)) return !getRoute(fr).isNotReachable();
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
  public void drawEdges(final Graphics2D g, final Context ctx, final SpringNode n) {
    final Rectangle2D visible = ctx.getVisibleCanvas();
    final BusStation station = map.get(n);
    final RoutingResult route = getRoute(station);
    if(route != null && route.isNotReachable()) return;

    final double x1 = n.getX();
    final double y1 = n.getY();
    for(final UndirectedEdge e : matrix.getEdgesFor(station)) {
      final int degree = e.getLineDegree() - e.walkingHighlighted();
      if(degree <= 0) {
        continue;
      }

      final BusStation neighbor = e.getLower();

      final SpringNode node = getNode(neighbor);
      final RoutingResult otherRoute = getRoute(neighbor);
      if(otherRoute != null && otherRoute.isNotReachable()) {
        continue;
      }

      final double x2 = node.getX();
      final double y2 = node.getY();
      final Line2D drawLine = new Line2D.Double(x1, y1, x2, y2);
      final BasicStroke stroke =
          new BasicStroke(degree, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
      final Rectangle2D bbox = stroke.createStrokedShape(drawLine).getBounds2D();
      if(!visible.intersects(bbox)) {
        continue;
      }

      final Graphics2D g2 = (Graphics2D) g.create();
      if(from != null) {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
      }
      synchronized(e) {
        int counter = 0;
        for(final BusLine line : e.getNonHighlightedLines()) {
          if(BusLine.WALK.equals(line)) {
            continue;
          }
          g2.setStroke(new BasicStroke(degree - counter,
              BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
          g2.setColor(line.getColor());
          g2.draw(drawLine);
          ++counter;
        }
        g2.dispose();
        for(final BusLine line : e.getHighlightedLines()) {
          g.setStroke(new BasicStroke(degree - counter,
              BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
          g.setColor(line.getColor());
          g.draw(drawLine);
          ++counter;
        }
      }
    }
  }

  /**
   * The bubble set generator.
   */
  private static final AbstractShapeGenerator BUBBLES = new ShapeSimplifier(
      new BSplineShapeGenerator(new ShapeSimplifier(new PolygonShapeGenerator(
          new BubbleSet()))), 1.0);

  static {
    BUBBLES.setRadius(0);
  }

  @Override
  public void drawNode(final Graphics2D g, final Context ctx, final SpringNode n,
      final boolean secondarySelected) {
    final BusStation station = map.get(n);
    final RoutingResult route = getRoute(station);
    if(route != null && route.isNotReachable()) return;

    if(secondarySelected) {
      System.out.println(route);
    }

    final Shape shape = nodeClickArea(n, true);
    final BasicStroke stroke = new BasicStroke(.5f);
    final Rectangle2D bbox = stroke.createStrokedShape(shape).getBounds2D();

    if(ctx.getVisibleCanvas().intersects(bbox)) {
      final Graphics2D g2 = (Graphics2D) g.create();
      g2.setColor(!station.equals(from) ?
          (secondarySelected && from != null ? Color.BLUE : Color.WHITE) : Color.RED);
      g2.fill(shape);
      g2.setStroke(stroke);
      g2.setColor(Color.BLACK);
      g2.draw(shape);
      g2.dispose();
    }

    if(secondarySelected) {
      final Collection<BusEdge> edges = route.getEdges();
      if(edges == null) return;
      final int size = edges.size();
      final Rectangle2D[] rects = new Rectangle2D[size + 1];
      final Line2D[] lines = new Line2D[size];
      final SpringNode start = getReferenceNode();
      rects[0] = nodeClickArea(start, true).getBounds2D();
      Point2D pos = start.getPos();
      int i = 0;
      for(final BusEdge e : edges) {
        final SpringNode cur = getNode(e.getTo());
        final Point2D curPos = cur.getPos();
        lines[i] = new Line2D.Double(pos, curPos);
        rects[i + 1] = nodeClickArea(cur, true).getBounds2D();
        pos = curPos;
        ++i;
      }
      final Shape bubble = BUBBLES.createShapeFor(rects, new Rectangle2D[0], lines);
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
      g.setColor(Color.BLUE);
      g.fill(bubble);
      g.setColor(Color.BLACK);
      g.draw(bubble);
    }
  }

  @Override
  public void drawLabel(final Graphics2D g, final Context ctx, final SpringNode n) {
    final BusStation station = map.get(n);
    if(matrix.getDegree(station) == 2) return;

    final Rectangle2D node = nodeClickArea(n, true).getBounds2D();
    final Point2D pos = ctx.toComponentCoordinates(
        new Point2D.Double(node.getMaxX(), node.getMinY()));
    final double x = pos.getX();
    final double y = pos.getY();

    final FontMetrics fm = g.getFontMetrics();
    final String label = station.getName();
    final Rectangle2D bbox = fm.getStringBounds(label, g);
    // translate the rectangle
    bbox.setRect(x + bbox.getMinX(), y + bbox.getMinY(), bbox.getWidth(),
        bbox.getHeight());

    if(!ctx.getVisibleComponent().intersects(bbox)) return;

    final double z = ctx.toComponentLength(1);
    final float d = (float) (z * z);

    final Graphics2D g2 = (Graphics2D) g.create();
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f *
        (d < 1 ? d : 1)));
    g2.setColor(Color.WHITE);
    g2.fill(bbox);
    g2.dispose();

    g.translate(x, y);
    if(d < 1) {
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, d));
    }
    g.setColor(Color.BLACK);
    g.drawString(station.getName(), 0, 0);
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
  public Shape nodeClickArea(final SpringNode n, final boolean real) {
    final BusStation station = map.get(n);
    final double r = Math.max(2, matrix.getMaxLines(station) / 2);
    final double x = real ? n.getX() : n.getPredictX();
    final double y = real ? n.getY() : n.getPredictY();
    return new Ellipse2D.Double(x - r, y - r, r * 2, r * 2);
  }

  @Override
  public void drawBackground(final Graphics2D g, final Context ctx) {
    final SpringNode ref = getReferenceNode();
    if(ref == null && !fade) return;
    Point2D center;
    double alpha;
    if(fade) {
      final long time = System.currentTimeMillis();
      final double t = ((double) time - fadingStart) / ((double) fadingEnd - fadingStart);
      final double f = Interpolator.SMOOTH.interpolate(t);
      final SpringNode n = f > 0.5 ? ref : getNode(fadeOut);
      center = n != null ? n.getPos() : null;
      if(t >= 1.0) {
        alpha = 1;
        fadeOut = null;
        fade = false;
      } else {
        alpha = f > 0.5 ? (f - 0.5) * 2 : 1 - f * 2;
      }
    } else {
      center = ref.getPos();
      alpha = 1;
    }
    if(center == null) return;
    boolean b = true;
    g.setColor(Color.WHITE);
    for(int i = MAX_INTERVAL; i > 0; --i) {
      final Shape circ = getCircle(i, center);
      final Graphics2D g2 = (Graphics2D) g.create();
      if(b) {
        final double d = (MAX_INTERVAL - i + 2.0) / (MAX_INTERVAL + 2);
        final double curAlpha = alpha * d;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
            (float) curAlpha));
        g2.setColor(Color.LIGHT_GRAY);
      }
      b = !b;
      g2.fill(circ);
      g2.dispose();
    }
  }

  @Override
  public boolean inAnimation() {
    return fade;
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
    return getNode(from);
  }

  @Override
  public String getTooltipText(final SpringNode node) {
    final BusStation station = map.get(node);
    String dist;
    if(from != null && from != station) {
      final RoutingResult route = getRoute(station);
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
   * @param station The station.
   * @return The corresponding node.
   */
  public SpringNode getNode(final BusStation station) {
    return station == null ? null : rev[station.getId()];
  }

  @Override
  public Rectangle2D getBoundingBox() {
    Rectangle2D bbox = null;
    final BusStation s = predict;
    if(s != null) {
      final Point2D pos = getNode(s).getPos();
      bbox = getCircle(StationDistance.MAX_INTERVAL, pos).getBounds2D();
    } else {
      for(final SpringNode n : nodes()) {
        final Rectangle2D b = nodeClickArea(n, false).getBounds2D();
        if(bbox == null) {
          bbox = b;
        } else {
          bbox.add(b);
        }
      }
    }
    return bbox;
  }

}
