package infovis.gui;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

/**
 * Paints and interacts with a {@link Canvas}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface Painter {

  /**
   * Draw on a canvas. The panning and zooming of the canvas is transparent to
   * this method and needs no further investigation.
   * 
   * @param gfx The graphics context.
   */
  void draw(Graphics2D gfx);

  /**
   * Draws a HUD (Head-Up-Display) on the canvas. This method draws over the
   * canvas and uses the components coordinate space.
   * 
   * @param gfx The graphics context.
   */
  void drawHUD(Graphics2D gfx);

  /**
   * Is called when the user clicks at the component and the HUD action does not
   * consume the event. The coordinates are in the {@link Painter Painters}
   * coordinate space and therefore suitable for clicks on objects on the
   * canvas. This method is the second in the order of click processing and no
   * dragging is performed, when this method returns <code>true</code>.
   * 
   * @param p The click position in canvas coordinates.
   * @return Whether the click was consumed.
   */
  boolean click(Point2D p);

  /**
   * Is called when the user clicks at the component. The coordinates are in the
   * components coordinate space and therefore suitable for clicks on HUDs. This
   * method is the first in the order of click processing and other actions
   * (object clicking and dragging) won't happen if this method returns
   * <code>true</code>.
   * 
   * @param p The click position in component coordinates.
   * @return Whether the click was consumed.
   */
  boolean clickHUD(Point2D p);

}
