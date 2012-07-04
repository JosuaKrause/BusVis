package infovis.draw;

import infovis.data.BusLine;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Realizes the legend.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface LegendRealizer {

  /**
   * Draws the legend.
   * 
   * @param g The graphics context.
   * @param view The visible rectangle.
   * @param lines The bus lines that are visible.
   */
  void drawLegend(Graphics2D g, Rectangle2D view, BusLine[] lines);

  /**
   * The standard legend realizer.
   * 
   * @author Joschi <josua.krause@googlemail.com>
   */
  LegendRealizer STANDARD = new LegendRealizer() {

    /** The font / block height. */
    private final double height = 18;

    /** The space between two lines. */
    private final double space = height / 6;

    /** The arc size. */
    private final double arc = height / 3;

    @Override
    public void drawLegend(final Graphics2D g, final Rectangle2D view,
        final BusLine[] lines) {
      g.setFont(g.getFont().deriveFont((float) height));
      g.translate(view.getMaxX() - space, view.getMaxY() - 2 * space);
      final Rectangle2D block = new Rectangle2D.Double(-height, -height, height, height);
      for(final BusLine l : lines) {
        final StringDrawer sd = new StringDrawer(g, l.getFullName());
        final Rectangle2D bbox = sd.getBounds();
        final double left = bbox.getWidth() + height + space;
        final double bottom = (height - bbox.getHeight()) * 0.5;

        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Double(-left - space, -height - space,
            left + height + space * 2, height + space * 2, arc, arc));
        g2.dispose();

        g.setColor(l.getColor());
        g.fill(block);
        g.setColor(Color.BLACK);
        g.draw(block);
        sd.draw(-left, bottom);
        g.translate(0, -height - 3 * space);
      }
    }

  };

}
