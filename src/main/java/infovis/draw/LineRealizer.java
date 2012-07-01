package infovis.draw;

import infovis.data.BusLine;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;

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
      final int degree = used.length + unused.length;
      final Graphics2D g2 = (Graphics2D) g.create();
      if(used.length > 0) {
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
      for(final BusLine l : used) {
        g.setColor(l.getColor());
        g.fill(createLineShape(line, counter, degree));
        ++counter;
      }
    }

  };

}
