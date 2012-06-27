package infovis.embed;

import static infovis.util.VecUtil.*;
import infovis.ctrl.Controller;
import infovis.data.BusLine;
import infovis.data.BusStation;
import infovis.data.BusTime;
import infovis.data.EdgeMatrix.UndirectedEdge;
import infovis.gui.Context;
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

public class StationDrawer implements NodeDrawer, Fader {

  private final StationDistance dist;

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

  public StationDrawer(final StationDistance dist) {
    this.dist = dist;
    dist.setFader(this);
  }

  @Override
  public void setAnimator(final Animator animator) {
    dist.setAnimator(animator);
  }

  @Override
  public void initialize(final BusStation from, final long currentTimeMillis,
      final int normal) {
    fadeOut = from;
    fadingStart = System.currentTimeMillis();
    fadingEnd = fadingStart + Interpolator.NORMAL;
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
    if(route != null && route.isNotReachable()) return;

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
      if(dist.getFrom() != null) {
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

  @Override
  public void drawNode(final Graphics2D g, final Context ctx, final SpringNode n) {
    final BusStation station = dist.getStation(n);
    final RoutingResult route = dist.getRoute(station);
    if(route != null && route.isNotReachable()) return;

    final Shape shape = nodeClickArea(n, true);
    final BasicStroke stroke = new BasicStroke(.5f);
    final Rectangle2D bbox = stroke.createStrokedShape(shape).getBounds2D();
    if(!ctx.getVisibleCanvas().intersects(bbox)) return;

    final Graphics2D g2 = (Graphics2D) g.create();
    g2.setColor(!station.equals(dist.getFrom()) ? Color.WHITE : Color.RED);
    g2.fill(shape);
    g2.setStroke(stroke);
    g2.setColor(Color.BLACK);
    g2.draw(shape);
    g2.dispose();
  }

  @Override
  public void drawLabel(final Graphics2D g, final Context ctx, final SpringNode n) {
    final BusStation station = dist.getStation(n);
    if(dist.getMatrix().getDegree(station) == 2) return;

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
    return new Ellipse2D.Double(x - r, y - r, r * 2, r * 2);
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
  public Rectangle2D getBoundingBox() {
    Rectangle2D bbox = null;
    final BusStation s = predict;
    if(s != null) {
      final Point2D pos = dist.getNode(s).getPos();
      bbox = getCircle(MAX_INTERVAL, pos).getBounds2D();
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

  @Override
  public String getTooltipText(final SpringNode node) {
    final BusStation station = dist.getStation(node);
    final BusStation from = dist.getFrom();
    String distance;
    if(from != null && from != station) {
      final RoutingResult route = dist.getRoute(station);
      if(!route.isNotReachable()) {
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