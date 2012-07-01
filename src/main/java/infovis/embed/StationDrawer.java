package infovis.embed;

import static infovis.util.VecUtil.*;
import infovis.ctrl.Controller;
import infovis.data.BusEdge;
import infovis.data.BusLine;
import infovis.data.BusStation;
import infovis.data.BusTime;
import infovis.data.EdgeMatrix;
import infovis.data.EdgeMatrix.UndirectedEdge;
import infovis.draw.LabelRealizer;
import infovis.draw.LineRealizer;
import infovis.draw.StationRealizer;
import infovis.gui.Context;
import infovis.routing.RoutingResult;
import infovis.util.Interpolator;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

/**
 * Draws the network.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class StationDrawer implements NodeDrawer, Fader {

  /** The corresponding station distance. */
  private final StationDistance dist;

  /** The realizer to actually draw the stations. */
  private final StationRealizer stationRealize;

  /** The realizer to actually draw the lines. */
  private final LineRealizer lineRealize;

  /** The realizer to actually draw the labels. */
  private final LabelRealizer labelRealize;

  /** The predicted next bus station. */
  protected volatile BusStation predict;

  /** The current waiter thread. */
  protected volatile Thread currentCalculator;

  /** The fading bus station. */
  protected BusStation fadeOut;

  /** The fading start time. */
  protected long fadingStart;

  /** The fading end time. */
  protected long fadingEnd;

  /** Whether we do fade currently. */
  protected boolean fade;

  /**
   * Creates a station drawer.
   * 
   * @param dist The corresponding station distance.
   * @param stationRealize The realizer to draw the stations.
   * @param lineRealize The realizer to draw the lines.
   * @param labelRealize The realizer to draw labels.
   */
  public StationDrawer(final StationDistance dist, final StationRealizer stationRealize,
      final LineRealizer lineRealize, final LabelRealizer labelRealize) {
    this.dist = dist;
    this.stationRealize = stationRealize;
    this.lineRealize = lineRealize;
    this.labelRealize = labelRealize;
    dist.setFader(this);
  }

  @Override
  public void setAnimator(final Animator animator) {
    dist.setAnimator(animator);
  }

  @Override
  public void initialize(final BusStation next, final int duration) {
    fadeOut = next;
    fadingStart = System.currentTimeMillis();
    fadingEnd = fadingStart + duration;
    fade = true;
  }

  @Override
  public void setPredict(final BusStation station) {
    predict = station;
  }

  @Override
  public boolean inFade() {
    return fade;
  }

  @Override
  public Iterable<SpringNode> nodes() {
    return dist.nodes();
  }

  @Override
  public void drawEdges(final Graphics2D g, final Context ctx, final SpringNode n) {
    final Rectangle2D visible = ctx.getVisibleCanvas();
    final BusStation station = dist.getStation(n);
    final RoutingResult route = dist.getRoute(station);
    if(route != null && !route.isReachable()) return;

    final double x1 = n.getX();
    final double y1 = n.getY();
    for(final UndirectedEdge e : dist.getMatrix().getEdgesFor(station)) {
      final int degree = e.getLineDegree() - e.walkingHighlighted();
      if(degree <= 0) {
        continue;
      }

      final BusStation neighbor = e.getLower();

      final SpringNode node = dist.getNode(neighbor);
      final RoutingResult otherRoute = dist.getRoute(neighbor);
      if(otherRoute != null && !otherRoute.isReachable()) {
        continue;
      }

      final double x2 = node.getX();
      final double y2 = node.getY();
      final Line2D line = new Line2D.Double(x1, y1, x2, y2);
      final Rectangle2D bbox =
          lineRealize.createLineShape(line, -1, e.getLineDegree()).getBounds2D();
      if(!visible.intersects(bbox)) {
        continue;
      }

      BusLine[] unused;
      BusLine[] used;
      synchronized(e) {
        unused = e.getNonHighlightedLines();
        used = e.getHighlightedLines();
      }
      lineRealize.drawLines(g, line, unused, used);
    }
  }

  @Override
  public void drawNode(final Graphics2D g, final Context ctx, final SpringNode n,
      final boolean secondarySelected) {
    final BusStation station = dist.getStation(n);
    final RoutingResult route = dist.getRoute(station);
    if(route != null && !route.isReachable()) return;

    final Shape shape = nodeClickArea(n, true);
    if(shape == null) return;
    final BasicStroke stroke = new BasicStroke(.5f);
    final Rectangle2D bbox = stroke.createStrokedShape(shape).getBounds2D();
    if(!ctx.getVisibleCanvas().intersects(bbox)) return;

    stationRealize.drawStation(g, shape, stroke,
        station.equals(dist.getFrom()), secondarySelected);
  }

  @Override
  public void drawSecondarySelected(final Graphics2D g, final Context ctx,
      final SpringNode n) {
    final BusStation station = dist.getStation(n);
    final RoutingResult route = dist.getRoute(station);
    if(route != null && !route.isReachable()) return;

    final Collection<BusEdge> edges = route.getEdges();
    if(edges == null) return;

    final int size = edges.size();
    final Shape[] shapes = new Shape[size + 1];
    final Line2D[] lines = new Line2D[size];
    final BusLine[] busLines = new BusLine[size];
    final int[] numbers = new int[size];
    final int[] maxNumbers = new int[size];
    final SpringNode start = dist.getReferenceNode();
    shapes[0] = nodeClickArea(start, true);
    Point2D pos = start.getPos();
    final EdgeMatrix matrix = dist.getMatrix();
    int i = 0;
    for(final BusEdge e : edges) {
      final SpringNode cur = dist.getNode(e.getTo());
      final Point2D curPos = cur.getPos();
      final BusLine line = e.getLine();
      lines[i] = new Line2D.Double(pos, curPos);
      busLines[i] = line;
      final UndirectedEdge ue = matrix.getFor(e.getFrom(), e.getTo());
      numbers[i] = ue.getNumberOfHighlighted(line);
      maxNumbers[i] = ue.getLineDegree();
      // index of shapes must be one greater
      shapes[++i] = nodeClickArea(cur, true);
      pos = curPos;
    }
    stationRealize.drawRoute(g, lineRealize, shapes, lines, busLines, numbers,
        maxNumbers);
  }

  /**
   * If a station has a degree of this or lower the label will always be drawn.
   */
  private static final int LOW_DEGREE = 1;

  /**
   * If a station has a degree of this or higher the label will always be drawn.
   */
  private static final int HIGH_DEGREE = 6;

  @Override
  public void drawLabel(final Graphics2D g, final Context ctx, final SpringNode n,
      final boolean hovered) {
    final BusStation station = dist.getStation(n);

    if(!hovered) {
      final int degree = dist.getMatrix().getDegree(station);
      if(degree > LOW_DEGREE && degree < HIGH_DEGREE) return;
    }

    final Shape s = nodeClickArea(n, true);
    if(s == null) return;
    final Rectangle2D node = s.getBounds2D();
    final Point2D pos = ctx.toComponentCoordinates(
        new Point2D.Double(node.getMaxX(), node.getMinY()));

    final BusStation from = dist.getFrom();
    String distance;
    if(from != null && from != station) {
      final RoutingResult route = dist.getRoute(station);
      if(route.isReachable()) {
        distance = " (" + BusTime.minutesToString(route.minutes()) + ")";
      } else {
        distance = " (not reachable)";
      }
    } else {
      distance = "";
    }

    labelRealize.drawLabel(g, ctx.getVisibleComponent(), ctx.toComponentLength(1),
        pos, station.getName() + distance);
  }

  @Override
  public void dragNode(final SpringNode n, final double startX, final double startY,
      final double dx, final double dy) {
    final BusStation station = dist.getStation(n);
    if(!station.equals(dist.getFrom())) {
      dist.getController().selectStation(station);
    }
    n.setPosition(startX + dx, startY + dy);
  }

  @Override
  public void selectNode(final SpringNode n) {
    final BusStation station = dist.getStation(n);
    if(!station.equals(dist.getFrom())) {
      dist.getController().selectStation(station);
    }
  }

  @Override
  public Shape nodeClickArea(final SpringNode n, final boolean real) {
    final BusStation station = dist.getStation(n);
    final double r = Math.max(2, dist.getMatrix().getMaxLines(station) / 2);
    final double x = real ? n.getX() : n.getPredictX();
    final double y = real ? n.getY() : n.getPredictY();
    return stationRealize.createStationShape(x, y, r);
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
    final Point2D c = center != null ? center : dist.getReferenceNode().getPos();
    final double radius = dist.getFactor() * 5 * i;
    final double r2 = radius * 2;
    return new Ellipse2D.Double(c.getX() - radius, c.getY() - radius, r2, r2);
  }

  @Override
  public void drawBackground(final Graphics2D g, final Context ctx, final boolean dc) {
    if(!dc) return;
    final SpringNode ref = dist.getReferenceNode();
    if(ref == null && !fade) return;
    Point2D center;
    double alpha;
    if(fade) {
      final long time = System.currentTimeMillis();
      final double t = ((double) time - fadingStart) / ((double) fadingEnd - fadingStart);
      final double f = Interpolator.SMOOTH.interpolate(t);
      final SpringNode n = f > 0.5 ? ref : dist.getNode(fadeOut);
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
  public void drawLegend(final Graphics2D g, final Context ctx) {
    // TODO draw legend
  }

  @Override
  public Rectangle2D getBoundingBox() {
    Rectangle2D bbox = null;
    final BusStation s = predict;
    if(s != null) {
      final Point2D pos = dist.getNode(s).getPos();
      bbox = getCircle(MAX_INTERVAL, pos).getBounds2D();
    } else {
      for(final SpringNode n : nodes()) {
        final Shape shape = nodeClickArea(n, false);
        if(shape == null) {
          continue;
        }
        final Rectangle2D b = shape.getBounds2D();
        if(bbox == null) {
          bbox = b;
        } else {
          bbox.add(b);
        }
      }
    }
    return bbox;
  }

  @Override
  public String getTooltipText(final SpringNode node) {
    final BusStation station = dist.getStation(node);
    final BusStation from = dist.getFrom();
    String distance;
    if(from != null && from != station) {
      final RoutingResult route = dist.getRoute(station);
      if(route.isReachable()) {
        distance = " (" + BusTime.minutesToString(route.minutes()) + ")";
      } else {
        distance = " (not reachable)";
      }
    } else {
      distance = "";
    }
    return station.getName() + distance;
  }

  @Override
  public void moveMouse(final Point2D cur) {
    final Controller ctrl = dist.getController();
    if(dist.getFrom() != null) {
      ctrl.setTitle(BusTime.minutesToString((int) Math.ceil(getLength(subVec(cur,
          dist.getReferenceNode().getPos())) / dist.getFactor())));
    } else {
      ctrl.setTitle(null);
    }
  }

}
