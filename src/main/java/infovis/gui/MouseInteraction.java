package infovis.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

/**
 * Mouse interactions for dragging objects or the background.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class MouseInteraction extends MouseAdapter {

  /** Whether we are currently dragging. */
  private boolean drag;

  /** The background start x-coordinate of the mouse. */
  private int startx;

  /** The background start y-coordinate of the mouse. */
  private int starty;

  /** The start x offset of the background. */
  private double origX;

  /** The start y offset of the background. */
  private double origY;

  /** The start position of the object to drag. */
  private Point2D start;

  /**
   * Starts dragging for an object.
   * 
   * @param start The initial position of the object.
   */
  protected final void startDragging(final Point2D start) {
    this.start = start;
    drag = true;
  }

  /**
   * Starts dragging of the background.
   * 
   * @param e The mouse event.
   * @param offX The initial x offset of the background.
   * @param offY The initial y offset of the background.
   */
  protected final void startDragging(final MouseEvent e, final double offX,
      final double offY) {
    startx = e.getX();
    starty = e.getY();
    origX = offX;
    origY = offY;
    start = null;
    drag = true;
  }

  /**
   * Getter.
   * 
   * @return The initial position of the dragged object.
   */
  protected final Point2D getPoint() {
    return start;
  }

  /**
   * Stops dragging of an object.
   * 
   * @return The initial position of the object.
   */
  protected final Point2D stopPointDrag() {
    final Point2D res = start;
    start = null;
    drag = false;
    return res;
  }

  /** Stops dragging. */
  protected final void stopDragging() {
    drag = false;
  }

  /**
   * Getter.
   * 
   * @return Whether we are currently dragging.
   */
  protected final boolean isDragging() {
    return drag;
  }

  /**
   * Getter.
   * 
   * @return Whether we are dragging an object.
   */
  protected final boolean isPointDrag() {
    return start != null;
  }

  /**
   * Calculates the movement of the background.
   * 
   * @param x The current mouse x position.
   * @return The movement in x direction.
   */
  protected final double getMoveX(final double x) {
    return origX + (x - startx);
  }

  /**
   * Calculates the movement of the background.
   * 
   * @param y The current mouse y position.
   * @return The movement in y direction.
   */
  protected final double getMoveY(final double y) {
    return origY + (y - starty);
  }

}
