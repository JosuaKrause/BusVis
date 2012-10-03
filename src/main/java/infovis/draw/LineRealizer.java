package infovis.draw;

import infovis.data.BusLine;
import infovis.util.VecUtil;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * Realizes the actual painting of bus lines.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface LineRealizer {

  /**
   * The shape of the line between bus stations.
   * 
   * @param line The actual line (assumed to contain no NaN values).
   * @param number The currently drawn bus line. If the number is negative the
   *          overall shape (click area and bounding box computation) should be
   *          returned.
   * @param maxNumber The number of bus lines that will be drawn.
   * @return The shape of the bus line.
   */
  Shape createLineShape(Line2D line, int number, int maxNumber);

  /**
   * Draws lines.
   * 
   * @param g The graphics context.
   * @param line The line coordinates (assumed to contain no NaN values).
   * @param unused The unused bus lines.
   * @param used The used bus lines.
   */
  void drawLines(Graphics2D g, Line2D line, BusLine[] unused, BusLine[] used);

  /**
   * The standard way to show bus lines.
   * 
   * @author Joschi <josua.krause@googlemail.com>
   */
  LineRealizer STANDARD = new LineRealizer() {

    @Override
    public Shape createLineShape(final Line2D line, final int number, final int maxNumber) {
      return new BasicStroke(number < 0 ? maxNumber : maxNumber - number,
          BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL).createStrokedShape(line);
    }

    @Override
    public void drawLines(final Graphics2D g, final Line2D line, final BusLine[] unused,
        final BusLine[] used) {
      final int degree = (used != null ? used.length : 0) + unused.length;
      final Graphics2D g2 = (Graphics2D) g.create();
      if(used != null) {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
      }
      int counter = 0;
      for(final BusLine l : unused) {
        if(BusLine.WALK.equals(l)) {
          continue;
        }
        g2.setColor(l.getColor());
        g2.fill(createLineShape(line, counter, degree));
        ++counter;
      }
      g2.dispose();
      if(used == null) return;
      for(final BusLine l : used) {
        g.setColor(l.getColor());
        g.fill(createLineShape(line, counter, degree));
        ++counter;
      }
    }

  };

  /**
   * An slightly advanced line drawing technique that displays the lines only
   * once.
   * 
   * @author Marc Spicker
   */
  LineRealizer ADVANCED = new LineRealizer() {

    @Override
    public void drawLines(final Graphics2D g, final Line2D line,
        final BusLine[] unused, final BusLine[] used) {
      final int degree = (used != null ? used.length : 0) + unused.length;
      int counter = 0;

      if(used != null) {
        for(final BusLine l : used) {
          g.setColor(l.getColor());
          g.fill(createLineShape(line, counter, degree));
          ++counter;
        }
      }

      final Graphics2D g2 = (Graphics2D) g.create();
      if(used != null) {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
      }
      for(final BusLine l : unused) {
        if(BusLine.WALK.equals(l)) {
          continue;
        }
        g2.setColor(l.getColor());
        g2.fill(createLineShape(line, counter, degree));
        ++counter;
      }
      g2.dispose();
    }

    /** The line width. */
    private static final double LINE_WIDTH = 1.0;

    @Override
    public Shape createLineShape(final Line2D line, final int number, final int maxNumber) {
      if(number < 0) return createLine(line.getX1(), line.getY1(),
          line.getX2(), line.getY2(), maxNumber * LINE_WIDTH);

      // calculate normal vector
      final Point2D p1 = line.getP1();
      final Point2D p2 = line.getP2();

      final double dx, dy;
      if(p1.getX() < p2.getX() || (p1.getX() == p2.getX() && p1.getY() < p2.getY())) {
        dx = p2.getX() - p1.getX();
        dy = p2.getY() - p1.getY();
      } else {
        dx = p1.getX() - p2.getX();
        dy = p1.getY() - p2.getY();
      }

      final Point2D normal = new Point2D.Double(-dy, dx);
      final Point2D n2 = VecUtil.setLength(normal, 0.95);

      final int factor;
      if((line.getX1() > line.getX2() && line.getY1() > line.getY2())
          || (line.getX1() < line.getX2() && line.getY1() < line.getY2())) {
        factor = calcFactor(number);
      } else {
        factor = -calcFactor(number);
      }
      final double transX = factor * n2.getX();
      final double transY = factor * n2.getY();

      final double x1 = line.getP1().getX() - transX;
      final double y1 = line.getP1().getY() - transY;
      final double x2 = line.getP2().getX() - transX;
      final double y2 = line.getP2().getY() - transY;

      return createLine(x1, y1, x2, y2, LINE_WIDTH);
    }

    /**
     * Creates a line with a given width without using a stroke.
     * 
     * @param x1 The first x coordinate.
     * @param y1 The first y coordinate.
     * @param x2 The second x coordinate.
     * @param y2 The second y coordinate.
     * @param width The width of the line.
     * @return The shape of the line.
     */
    private Shape createLine(final double x1, final double y1, final double x2,
        final double y2, final double width) {
      final Point2D ortho = VecUtil.setLength(
          new Point2D.Double(y1 - y2, x2 - x1), width * 0.5);
      final GeneralPath gp = new GeneralPath();
      gp.moveTo(x1 + ortho.getX(), y1 + ortho.getY());
      gp.lineTo(x2 + ortho.getX(), y2 + ortho.getY());
      gp.lineTo(x2 - ortho.getX(), y2 - ortho.getY());
      gp.lineTo(x1 - ortho.getX(), y1 - ortho.getY());
      gp.closePath();
      return gp;
    }

    /**
     * Calculates the factor of a number.
     * 
     * @param number The number.
     * @return The factor.
     */
    private int calcFactor(final int number) {
      if(number == 0) return 0;
      final int res = (number + 1) / 2;
      return (number & 1) == 0 ? res : -res;
    }

  };

}
