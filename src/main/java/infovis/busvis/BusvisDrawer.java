package infovis.busvis;

import static infovis.util.VecUtil.*;
import infovis.ctrl.Controller;
import infovis.data.BusEdge;
import infovis.data.BusLine;
import infovis.data.BusStation;
import infovis.data.BusTime;
import infovis.data.EdgeMatrix;
import infovis.data.EdgeMatrix.UndirectedEdge;
import infovis.draw.BackgroundRealizer;
import infovis.draw.LabelRealizer;
import infovis.draw.LegendRealizer;
import infovis.draw.LineRealizer;
import infovis.draw.StationRealizer;
import infovis.gui.Context;
import infovis.routing.RoutingResult;
import infovis.util.Interpolator;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Set;

/**
 * Draws the network.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class BusvisDrawer implements NodeDrawer, Fader {

  /** The corresponding station distance. */
  private final BusvisWeighter dist;

  /** The realizer to actually draw the stations. */
  private final StationRealizer stationRealize;

  /** The realizer to actually draw the lines. */
  private final LineRealizer lineRealize;

  /** The realizer to actually draw the labels. */
  private final LabelRealizer labelRealize;

  /** The realizer to actually draw the legend. */
  private final LegendRealizer legendRealize;

  /** The predicted next bus station. */
  private volatile BusStation predict;

  /** The fading bus station. */
  private BusStation fadeOut;

  /** The fading start time. */
  private long fadingStart;

  /** The fading end time. */
  private long fadingEnd;

  /** Whether we do fade currently. */
  private boolean fade;

  /**
   * Creates a station drawer.
   * 
   * @param dist The corresponding station distance.
   * @param stationRealize The realizer to draw the stations.
   * @param lineRealize The realizer to draw the lines.
   * @param labelRealize The realizer to draw labels.
   * @param legendRealize The realizer to draw legends.
   */
  public BusvisDrawer(final BusvisWeighter dist, final StationRealizer stationRealize,
      final LineRealizer lineRealize, final LabelRealizer labelRealize,
      final LegendRealizer legendRealize) {
    this.dist = dist;
    this.stationRealize = stationRealize;
    this.lineRealize = lineRealize;
    this.labelRealize = labelRealize;
    this.legendRealize = legendRealize;
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
  public Iterable<LayoutNode> nodes() {
    return dist.nodes();
  }

  @Override
  public void drawEdges(final Graphics2D g, final Context ctx,
      final LayoutNode n, final Set<BusLine> visibleLines, final boolean secSel) {
    final Rectangle2D visible = ctx.getVisibleCanvas();

    final BusStation station = dist.getStation(n);
    final RoutingResult route = dist.getRoute(station);
    if(route != null && !route.isReachable()) return;

    final double x1 = n.getX();
    final double y1 = n.getY();

    if(Double.isNaN(x1) || Double.isNaN(y1)) return;

    for(final UndirectedEdge e : dist.getMatrix().getEdgesFor(station)) {
      final int degree = e.getLineDegree() - e.walkingHighlighted();
      if(degree <= 0) {
        continue;
      }

      final BusStation neighbor = e.getLower();

      final LayoutNode node = dist.getNode(neighbor);
      final RoutingResult otherRoute = dist.getRoute(neighbor);
      if(otherRoute != null && !otherRoute.isReachable()) {
        continue;
      }

      final double x2 = node.getX();
      final double y2 = node.getY();

      if(Double.isNaN(x2) || Double.isNaN(y2)) {
        continue;
      }

      final Line2D line = new Line2D.Double(x1, y1, x2, y2);
      final Shape lineShape = lineRealize.createLineShape(line, -1, e.getLineDegree());

      if(!lineShape.intersects(visible)) {
        continue;
      }

      BusLine[] unused;
      BusLine[] used;
      if(dist.getFrom() != null) {
        if(!secSel) {
          synchronized(e) {
            unused = e.getNonHighlightedLines();
            used = e.getHighlightedLines();
          }
        } else {
          unused = e.getLines();
          used = new BusLine[0];
        }
      } else {
        unused = e.getLines();
        used = null;
      }

      // sorting
      Arrays.sort(unused);
      if(used != null) {
        Arrays.sort(used);
      }

      // adding to visible lines
      if(used != null) {
        visibleLines.addAll(Arrays.asList(used));
      }
      visibleLines.addAll(Arrays.asList(unused));

      lineRealize.drawLines(g, line, unused, used);
    }
  }

  @Override
  public void drawNode(final Graphics2D g, final Context ctx, final LayoutNode n,
      final boolean secondarySelected) {
    final BusStation station = dist.getStation(n);
    final RoutingResult route = dist.getRoute(station);
    if(route != null && !route.isReachable()) return;

    final Shape shape = nodeClickArea(n, true);
    final BasicStroke stroke = new BasicStroke(.5f);
    final Rectangle2D bbox = stroke.createStrokedShape(shape).getBounds2D();
    if(!ctx.getVisibleCanvas().intersects(bbox)) return;

    stationRealize.drawStation(g, shape, stroke,
        station.equals(dist.getFrom()), secondarySelected);
  }

  @Override
  public void drawSecondarySelected(final Graphics2D g, final Context ctx,
      final LayoutNode n) {
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
    final LayoutNode start = dist.getReferenceNode();
    shapes[0] = nodeClickArea(start, true);
    Point2D pos = start.getPos();
    final EdgeMatrix matrix = dist.getMatrix();
    int i = 0;
    for(final BusEdge e : edges) {
      final LayoutNode cur = dist.getNode(e.getTo());
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
    stationRealize.drawRoute(g, lineRealize, shapes, lines, busLines, numbers, maxNumbers);
  }

  /** If a station has a degree of this or lower the label will always show. */
  private static final int LOW_DEGREE = 1;

  /** If a station has a radius of this or higher the label will always show. */
  private static final double HIGH_RADIUS = 6;

  @Override
  public void drawLabel(final Graphics2D g, final Context ctx, final LayoutNode n,
      final boolean hovered, final String addText) {
    final double zoom = ctx.toComponentLength(1);
    final BusStation station = dist.getStation(n);
    boolean isImportantNode = true;

    if(!hovered && addText == null) {
      final int degree = dist.getMatrix().getDegree(station);
      final double radius = nodeRadius(n);
      if(degree > LOW_DEGREE && radius < HIGH_RADIUS) {
        isImportantNode = false;
      }
    }

    final Shape s = nodeClickArea(n, true);
    final Rectangle2D node = s.getBounds2D();
    final Point2D pos = ctx.toComponentCoordinates(
        new Point2D.Double(node.getMaxX(), node.getMinY()));

    final BusStation from = dist.getFrom();
    String distance;
    if(addText != null) {
      distance = " [" + addText + "]";
    } else if(from != null && from != station) {
      final RoutingResult route = dist.getRoute(station);
      if(route.isReachable()) {
        distance = " (" + BusTime.minutesToString(route.minutes()) + ")";
      } else
        return;
    } else {
      distance = "";
    }

    labelRealize.drawLabel(g, ctx.getVisibleComponent(), zoom,
        pos, station.getName() + distance, isImportantNode);
  }

  @Override
  public void drawLegend(final Graphics2D g2, final Context ctx, final Set<BusLine> lines) {
    final BusLine[] ls = lines.toArray(new BusLine[lines.size()]);
    Arrays.sort(ls);
    legendRealize.drawLegend(g2, ctx.getVisibleComponent(), ls);
  }

  @Override
  public void drawRouteLabels(final Graphics2D g, final Context ctx, final LayoutNode n,
      final BitSet visited) {
    final BusStation s = dist.getStation(n);
    final RoutingResult route = dist.getRoute(s);
    if(route == null || !route.isReachable()) return;

    final Collection<BusEdge> edges = route.getEdges();
    if(edges == null) return;

    final LayoutNode startNode = dist.getReferenceNode();
    final Graphics2D gfx = (Graphics2D) g.create();
    drawLabel(gfx, ctx, startNode, false, route.getStartTime().pretty());
    gfx.dispose();
    visited.set(startNode.getId());

    final BusTime start = route.getStartTime();
    for(final BusEdge e : edges) {
      final LayoutNode to = dist.getNode(e.getTo());
      final Graphics2D g2 = (Graphics2D) g.create();
      final BusLine line = e.getLine();
      drawLabel(g2, ctx, to, false, e.getEnd().pretty() + " (" +
          BusTime.minutesToString(start.minutesTo(e.getEnd())) +
          ") - " + line.getName());
      g2.dispose();
      visited.set(to.getId());
    }
  }

  @Override
  public LayoutNode getNode(final int i) {
    return dist.getNode(i);
  }

  @Override
  public void dragNode(final LayoutNode n, final double startX, final double startY,
      final double dx, final double dy) {
    final BusStation station = dist.getStation(n);
    if(!station.equals(dist.getFrom())) {
      dist.getController().selectStation(station);
    }
    n.setPosition(startX + dx, startY + dy);
  }

  @Override
  public void selectNode(final LayoutNode n) {
    final BusStation station = dist.getStation(n);
    if(!station.equals(dist.getFrom())) {
      dist.getController().selectStation(station);
    }
  }

  @Override
  public Shape nodeClickArea(final LayoutNode n, final boolean real) {
    final double x = real ? n.getX() : n.getPredictX();
    final double y = real ? n.getY() : n.getPredictY();
    return nodeClickArea(n, new Point2D.Double(x, y));
  }

  /**
   * A shape defining the area, where a click is associated with the given node.
   * The area should be located at the given position.
   * 
   * @param n The node.
   * @param pos The position.
   * @return The clickable shape of the node.
   */
  public Shape nodeClickArea(final LayoutNode n, final Point2D pos) {
    return stationRealize.createStationShape(pos.getX(), pos.getY(), nodeRadius(n));
  }

  @Override
  public double nodeRadius(final LayoutNode n) {
    final BusStation station = dist.getStation(n);
    return Math.max(2, dist.getMatrix().getMaxLines(station) / 2);
  }

  /**
   * Getter.
   * 
   * @param i The interval.
   * @param factor The distance factor.
   * @param center The center of the circle.
   * @return The circle.
   */
  public static Ellipse2D getCircle(final int i, final double factor, final Point2D center) {
    final Point2D c = center;
    final double radius = factor * 5 * i;
    final double r2 = radius * 2;
    return new Ellipse2D.Double(c.getX() - radius, c.getY() - radius, r2, r2);
  }

  @Override
  public void drawBackground(final Graphics2D g, final Context ctx,
      final BackgroundRealizer background) {
    final LayoutNode ref = dist.getReferenceNode();
    if(ref == null && !fade) return;
    Point2D center;
    double alpha;
    if(fade) {
      final long time = System.currentTimeMillis();
      final double t = ((double) time - fadingStart) / ((double) fadingEnd - fadingStart);
      final double f = Interpolator.SMOOTH.interpolate(t);
      final LayoutNode n = f > 0.5 ? ref : dist.getNode(fadeOut);
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
    final double factor = dist.getFactor();
    if(!ctx.getVisibleCanvas().intersects(background.boundingBox(center, factor))) return;
    background.drawBackground(g, center, factor, alpha);
  }

  @Override
  public Rectangle2D getBoundingBox(final BackgroundRealizer background) {
    Rectangle2D bbox = null;
    final BusStation s = predict;
    if(s != null) {
      final Point2D pos = dist.getNode(s).getPos();
      bbox = background.boundingBox(pos, dist.getFactor());
    } else {
      for(final LayoutNode n : nodes()) {
        final Shape shape = nodeClickArea(n, false);
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
  public void moveMouse(final Point2D cur) {
    final Controller ctrl = dist.getController();
    if(dist.getFrom() != null) {
      ctrl.setTitle(BusTime.minutesToString((int) Math.ceil(getLength(subVec(cur,
          dist.getReferenceNode().getPos())) / dist.getFactor())));
    } else {
      ctrl.setTitle(null);
    }
  }

  @Override
  public boolean hasSecondarySelection() {
    final Controller ctrl = dist.getController();
    return ctrl.hasSecondarySelection();
  }

  @Override
  public boolean isSecondarySelected(final LayoutNode node) {
    final Controller ctrl = dist.getController();
    return ctrl.isSecondarySelected(dist.getStation(node));
  }

  /** The cached secondary selected nodes. */
  private Collection<LayoutNode> secSelCache;

  /** The old secondary selected nodes id list. */
  private int[] lastSecSelIds;

  @Override
  public Collection<LayoutNode> secondarySelected() {
    final Controller ctrl = dist.getController();
    final int[] ids = ctrl.secondarySelectedIds();
    if(ids != lastSecSelIds || secSelCache == null) {
      final LayoutNode[] res = new LayoutNode[ids.length];
      for(int i = 0; i < res.length; ++i) {
        res[i] = dist.getNode(dist.getStation(ids[i]));
      }
      secSelCache = Arrays.asList(res);
      lastSecSelIds = ids;
    }
    return secSelCache;
  }

  @Override
  public void secondarySelection(final Collection<LayoutNode> nodes) {
    final Controller ctrl = dist.getController();
    if(nodes.isEmpty()) {
      ctrl.clearSecondarySelection();
      return;
    }
    for(final LayoutNode node : nodes) {
      ctrl.toggleSecondarySelected(dist.getStation(node));
    }
  }

  @Override
  public boolean showLegend() {
    return dist.getController().showLegend();
  }

}
