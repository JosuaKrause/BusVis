package infovis.draw;

import infovis.data.BusLine;
import infovis.util.VecUtil;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
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
   * @param line The actual line.
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
   * @param line The line coordinates.
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

    /** The normal stroke. */
    private final Stroke normalStroke = new BasicStroke(1);

    @Override
    public Shape createLineShape(final Line2D line, final int number, final int maxNumber) {
      if(Double.isNaN(line.getX1()) || Double.isNaN(line.getX2())
          || Double.isNaN(line.getY1()) || Double.isNaN(line.getY2())) return new Line2D.Double();

      if(number < 0) return new BasicStroke(maxNumber).createStrokedShape(line);

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
      // unit length
      final double length = VecUtil.getLength(normal);
      normal.setLocation((normal.getX() / length) * 0.95, (normal.getY() / length) * 0.95);

      final double transX = (-maxNumber / 2.0 + number + 1) * normal.getX();
      final double transY = (-maxNumber / 2.0 + number + 1) * normal.getY();

      // create new line
      final Line2D newLine = new Line2D.Double(line.getP1().getX() - transX,
          line.getP1().getY() - transY, line.getP2().getX() - transX, line.getP2().getY()
          - transY);

      return normalStroke.createStrokedShape(newLine);
    }

  };

}
