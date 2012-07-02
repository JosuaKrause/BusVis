package infovis.draw;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Realizes the actual painting of labels.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface LabelRealizer {

  /**
   * Draws a label.
   * 
   * @param g The graphics context.
   * @param view The current view-port.
   * @param scale The current scale.
   * @param pos The position where the label should be drawn.
   * @param text The text of the label.
   */
  void drawLabel(Graphics2D g, Rectangle2D view, double scale, Point2D pos, String text);

  /** The standard way to draw labels. */
  LabelRealizer STANDARD = new LabelRealizer() {

    @Override
    public void drawLabel(final Graphics2D g, final Rectangle2D view, final double scale,
        final Point2D pos, final String label) {
      final double x = pos.getX();
      final double y = pos.getY();

      final FontMetrics fm = g.getFontMetrics();
      final Rectangle2D bbox = fm.getStringBounds(label, g);
      // translate the rectangle
      bbox.setRect(x + bbox.getMinX(), y + bbox.getMinY(), bbox.getWidth(),
          bbox.getHeight());

      if(!view.intersects(bbox)) return;

      final float d = (float) (scale * scale);

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
      g.drawString(label, 0, 0);
    }

  };

}
