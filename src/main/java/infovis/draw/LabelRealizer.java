package infovis.draw;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

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
   * @param isImportantNode Whether this node is important.
   */
  void drawLabel(Graphics2D g, Rectangle2D view, double scale,
      Point2D pos, String text, boolean isImportantNode);

  /**
   * The standard way to draw labels.
   * 
   * @author Joschi <josua.krause@googlemail.com>
   */
  LabelRealizer STANDARD = new LabelRealizer() {

    private final float minZoomLevel = 10;

    private final float maxZoomLevel = 120;

    @Override
    public void drawLabel(final Graphics2D g, final Rectangle2D view, final double scale,
        final Point2D pos, final String label, final boolean isImportantNode) {
      final StringDrawer sd = new StringDrawer(g, label, pos);
      final Rectangle2D bbox = sd.getBounds();

      if(!view.intersects(bbox)) return;

      final float d = (float) (scale * scale);
      final float alpha = isImportantNode ? d :
          (d - minZoomLevel) / (maxZoomLevel - minZoomLevel);

      if(alpha <= 0f) return;

      final Graphics2D g2 = (Graphics2D) g.create();
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
          0.3f * Math.min(1f, alpha)));
      g2.setColor(Color.WHITE);
      final double arc = bbox.getHeight() / 3;
      final double space = bbox.getHeight() / 6;
      g2.fill(new RoundRectangle2D.Double(bbox.getMinX() - space, bbox.getMinY() - space,
          bbox.getWidth() + 2 * space, bbox.getHeight() + 2 * space, arc, arc));
      g2.dispose();

      if(alpha < 1f) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
      }

      g.setColor(Color.BLACK);
      sd.draw();
    }

  };

}
