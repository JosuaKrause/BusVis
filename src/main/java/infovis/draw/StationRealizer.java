package infovis.draw;

import infovis.data.BusLine;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import setvis.bubbleset.BubbleSet;
import setvis.shape.AbstractShapeGenerator;
import setvis.shape.BSplineShapeGenerator;
import setvis.shape.PolygonShapeGenerator;
import setvis.shape.ShapeSimplifier;

/**
 * Realizes the actual painting of bus stations.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface StationRealizer {

  /**
   * The shape of the depiction of the bus station. This is also the are that
   * can be clicked on.
   * 
   * @param x The x translation.
   * @param y The y translation.
   * @param r The radius of the station.
   * @return The shape of the bus station.
   */
  Shape createStationShape(double x, double y, double r);

  /**
   * Draws the bus station.
   * 
   * @param g The graphics context.
   * @param station The station that is drawn.
   * @param stroke The stroke that can be used.
   * @param referenceNode Whether this node is the reference node.
   * @param secondarySelected Whether this node is secondary selected.
   */
  void drawStation(Graphics2D g, Shape station, Stroke stroke, boolean referenceNode,
      boolean secondarySelected);

  /**
   * Highlights a selected route.
   * 
   * @param g The graphics context.
   * @param liner The method to draw lines.
   * @param stations The shapes of the used stations.
   * @param lines The used lines.
   * @param busLines The used bus lines.
   * @param numbers The numbers of the highlighted lines.
   * @param maxNumbers The max numbers of the lines.
   */
  void drawRoute(Graphics2D g, LineRealizer liner, Shape[] stations, Line2D[] lines,
      BusLine[] busLines, int[] numbers, int[] maxNumbers);

  /**
   * The standard way to show bus stations.
   * 
   * @author Joschi <josua.krause@googlemail.com>
   */
  StationRealizer STANDARD = new StationRealizer() {

    /**
     * The bubble set generator.
     */
    private final AbstractShapeGenerator bubbles = new ShapeSimplifier(
        new BSplineShapeGenerator(new ShapeSimplifier(new PolygonShapeGenerator(
            new BubbleSet()))), 1.0);

    {
      bubbles.setRadius(0);
    }

    @Override
    public Shape createStationShape(final double x, final double y, final double r) {
      // JVM-bug #7180110
      if(Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(r)) return null;
      return new Ellipse2D.Double(x - r, y - r, r * 2, r * 2);
    }

    @Override
    public void drawStation(final Graphics2D g, final Shape node, final Stroke stroke,
        final boolean referenceNode, final boolean secondarySelected) {
      g.setColor(secondarySelected ? Color.BLUE :
          (!referenceNode ? Color.WHITE : Color.RED));
      g.fill(node);
      g.setStroke(stroke);
      g.setColor(Color.BLACK);
      g.draw(node);
    }

    @Override
    public void drawRoute(final Graphics2D g, final LineRealizer liner,
        final Shape[] stations, final Line2D[] lines, final BusLine[] busLines,
        final int[] numbers, final int[] maxNumbers) {
      final List<Rectangle2D> rects = new ArrayList<Rectangle2D>();
      for(final Shape s : stations) {
        if(s == null) {
          continue;
        }
        rects.add(s.getBounds2D());
      }
      final Shape bubble = bubbles.createShapeFor(
          rects.toArray(new Rectangle2D[rects.size()]), new Rectangle2D[0], lines);
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
      g.setColor(Color.BLUE);
      g.fill(bubble);
      g.setColor(Color.BLACK);
      g.draw(bubble);
    }

  };

}
