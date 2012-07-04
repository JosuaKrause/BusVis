package infovis.draw;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Draws and measures a string.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class StringDrawer {

  /** The graphics context. */
  private final Graphics2D g;

  /** The string to draw. */
  private final String str;

  /** The position of the string. */
  private final Point2D pos;

  /** The bounding box of the string. */
  private final Rectangle2D bbox;

  /**
   * Creates a string drawer.
   * 
   * @param g The graphics context.
   * @param str The string.
   */
  public StringDrawer(final Graphics2D g, final String str) {
    this(g, str, new Point2D.Double());
  }

  /**
   * Creates a string drawer with a positioned string.
   * 
   * @param g The graphics context.
   * @param str The string.
   * @param pos The position.
   */
  public StringDrawer(final Graphics2D g, final String str, final Point2D pos) {
    this.g = g;
    this.str = str;
    this.pos = pos;
    final FontMetrics fm = g.getFontMetrics();
    bbox = fm.getStringBounds(str, g);
    // translate the rectangle
    bbox.setRect(pos.getX() + bbox.getMinX(), pos.getY() + bbox.getMinY(),
        bbox.getWidth(), bbox.getHeight());
  }

  /**
   * Getter.
   * 
   * @return The bounds of the string.
   */
  public Rectangle2D getBounds() {
    return bbox;
  }

  /** Draws the string. */
  public void draw() {
    draw(0, 0);
  }

  /**
   * Draws the string at the given position.
   * 
   * @param dx The x position.
   * @param dy The y position.
   */
  public void draw(final double dx, final double dy) {
    final Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(pos.getX() + dx, pos.getY() + dy);
    g2.drawString(str, 0, 0);
    g2.dispose();
  }

}
