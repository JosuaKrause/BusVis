package infovis.gui;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

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
   * @param ctx The canvas context.
   */
  void draw(Graphics2D gfx, Context ctx);

  /**
   * Draws a HUD (Head-Up-Display) on the canvas. This method draws over the
   * canvas and uses the components coordinate space.
   * 
   * @param gfx The graphics context.
   * @param ctx The canvas context.
   */
  void drawHUD(Graphics2D gfx, Context ctx);

  /**
   * Is called when the user clicks at the component and the HUD action does not
   * consume the event. The coordinates are in the {@link Painter Painters}
   * coordinate space and therefore suitable for clicks on objects on the
   * canvas. This method is the second in the order of click processing and no
   * dragging is performed, when this method returns <code>true</code>.
   * 
   * @param p The click position in canvas coordinates.
   * @param e The original event.
   * @return Whether the click was consumed.
   */
  boolean click(Point2D p, MouseEvent e);

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

  /**
   * Is called when the user moves the mouse over the component and
   * {@link #getTooltipHUD(Point2D)} has returned <code>null</code>. This method
   * returns the tool-tip that should be displayed at the given canvas position.
   * 
   * @param p The mouse position in canvas coordinates.
   * @return The tool-tip text or <code>null</code> if no tool-tip should be
   *         displayed.
   */
  String getTooltip(Point2D p);

  /**
   * Is called when the user moves the mouse over the component. This method
   * returns the tool-tip that should be displayed at the given position. This
   * method is called before {@link #getTooltip(Point2D)}.
   * 
   * @param p The mouse position in component coordinates.
   * @return The tool-tip text or <code>null</code> if no tool-tip should be
   *         displayed.
   */
  String getTooltipHUD(Point2D p);

  /**
   * Is called when the user starts a dragging operation on the canvas. The
   * coordinates are in the {@link Painter Painters} coordinate space and
   * therefore suitable for dragging of objects on the canvas.
   * 
   * @param p The position where the drag starts in canvas coordinates.
   * @return Whether the drag is accepted and the dragging is started.
   */
  boolean acceptDrag(Point2D p);

  /**
   * Is called subsequently after {@link #acceptDrag(Point2D)} returned
   * <code>true</code> on every mouse movement until the user releases the mouse
   * button.
   * 
   * @param start The position where the drag started in canvas coordinates.
   * @param cur The current drag position in canvas coordinates.
   * @param dx The x distance of the drag in canvas coordinates.
   * @param dy The y distance of the drag in canvas coordinates.
   */
  void drag(Point2D start, Point2D cur, double dx, double dy);

  /**
   * Is called when the user releases the mouse in drag operation.
   * 
   * @param start The position where the drag started in canvas coordinates.
   * @param end The end position of the drag in canvas coordinates.
   * @param dx The x distance of the drag in canvas coordinates.
   * @param dy The y distance of the drag in canvas coordinates.
   */
  void endDrag(Point2D start, Point2D end, double dx, double dy);

  /**
   * Is called when the mouse was moved.
   * 
   * @param cur The current position in canvas coordinates.
   */
  void moveMouse(Point2D cur);

  /**
   * Calculates the bounding box of the canvas. This method may return
   * <code>null</code> to indicate that the bounding box is irrelevant.
   * 
   * @return The bounding box.
   */
  Rectangle2D getBoundingBox();

}
