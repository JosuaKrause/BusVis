package infovis.gui;

import infovis.util.Objects;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * A zoomable user interface can be translated and zooming can be performed.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class ZoomableUI {

  /** The {@link Refreshable} to be notified when the transformation changes. */
  private final Refreshable refreshee;

  /** An optional restriction of the canvas. */
  private final RestrictedCanvas restriction;

  /** The x offset. */
  private double offX;

  /** The y offset. */
  private double offY;

  /** The zoom level. */
  private double zoom = 1;

  /** The minimal zoom value. */
  private double minZoom = -1;

  /** The maximal zoom value. */
  private double maxZoom = -1;

  /**
   * Creates a zoomable user interface.
   * 
   * @param refreshee Will be notified when the transformation changes.
   * @param restriction An optional restriction for the canvas.
   */
  public ZoomableUI(final Refreshable refreshee, final RestrictedCanvas restriction) {
    this.refreshee = Objects.requireNonNull(refreshee);
    this.restriction = restriction;
  }

  /**
   * Setter.
   * 
   * @param x The x offset.
   * @param y The y offset.
   */
  public void setOffset(final double x, final double y) {
    if(restriction != null) {
      final Rectangle2D bbox = restriction.getBoundingRect();
      if(bbox == null) return;
      offX = x;
      offY = y;
      final Rectangle2D visBB = restriction.getCurrentView();
      // snap back
      if(!bbox.contains(visBB)) {
        double transX = 0;
        double transY = 0;

        if(visBB.getMaxX() > bbox.getMaxX()) {
          // too far right
          transX -= visBB.getMaxX() - bbox.getMaxX();
        } else if(visBB.getMinX() < bbox.getMinX()) {
          // too far left
          transX += bbox.getMinX() - visBB.getMinX();
        }
        if(visBB.getMaxY() > bbox.getMaxY()) {
          // too far down
          transY -= visBB.getMaxY() - bbox.getMaxY();
        } else if(visBB.getMinY() < bbox.getMinY()) {
          // too far up
          transY += bbox.getMinY() - visBB.getMinY();
        }

        offX -= fromReal(transX);
        offY -= fromReal(transY);
      }
    } else {
      offX = x;
      offY = y;
    }
    refreshee.refresh();
  }

  /**
   * Zooms to the on screen (in component coordinates) position.
   * 
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param zooming The amount of zooming.
   */
  public void zoomTo(final double x, final double y, final int zooming) {
    final double factor = Math.pow(1.1, -zooming);
    zoomTo(x, y, factor);
  }

  /**
   * Zooms to the on screen (in component coordinates) position.
   * 
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param factor The factor to alter the zoom level.
   */
  public void zoomTo(final double x, final double y, final double factor) {
    double f = factor;
    double newZoom = zoom * factor;
    if(hasMinZoom()) {
      if(newZoom < minZoom) {
        newZoom = minZoom;
        f = newZoom / zoom;
      } else if(newZoom > maxZoom) {
        newZoom = maxZoom;
        f = newZoom / zoom;
      }
    }

    // P = (off - mouse) / zoom
    // P = (newOff - mouse) / newZoom
    // newOff = (off - mouse) / zoom * newZoom + mouse
    // newOff = (off - mouse) * factor + mouse
    zoom = newZoom;
    // does repaint
    setOffset((offX - x) * f + x, (offY - y) * f + y);
  }

  /**
   * Zooms towards the center of the given rectangle.
   * 
   * @param factor The zoom factor.
   * @param box The rectangle to zoom to in component coordinates.
   */
  public void zoom(final double factor, final Rectangle2D box) {
    zoomTo(box.getCenterX(), box.getCenterY(), factor);
  }

  /**
   * Resets the view such that the origin of canvas coordinates lies in the
   * center of the given rectangle in component coordinates.
   * 
   * @param screen The screen rectangle in component coordinates.
   */
  public void resetView(final Rectangle2D screen) {
    zoom = 1;
    // does repaint
    setOffset(screen.getCenterX(), screen.getCenterY());
  }

  /**
   * Transforms the view such that the rectangle <code>view</code> in canvas
   * coordinates matches the rectangle <code>screen</code> in component
   * coordinates. An additional margin can be added.
   * 
   * @param view The rectangle in canvas coordinates.
   * @param screen The rectangle in component coordinates.
   * @param margin The margin.
   * @param setAsMinZoom Sets the resulting zoom level as min zoom when no min
   *          zoom is assigned yet. Also no more than the rectangle to show will
   *          be visible.
   */
  public void showRectangle(final Rectangle2D view, final Rectangle2D screen,
      final double margin, final boolean setAsMinZoom) {
    final int nw = (int) (screen.getWidth() - 2 * margin);
    final int nh = (int) (screen.getHeight() - 2 * margin);
    final double rw = nw / view.getWidth();
    final double rh = nh / view.getHeight();
    final double factor = setAsMinZoom ? (rw > rh ? rw : rh) : (rw < rh ? rw : rh);
    zoom = 1;
    setOffset(margin + (nw - view.getWidth()) / 2 - view.getMinX(), margin
        + (nh - view.getHeight()) / 2 - view.getMinY());
    if(setAsMinZoom && !hasMinZoom()) {
      setMinZoom(factor);
    }
    zoom(factor, screen);
  }

  /**
   * Getter.
   * 
   * @return the x offset
   */
  public double getOffsetX() {
    return offX;
  }

  /**
   * Getter.
   * 
   * @return the y offset
   */
  public double getOffsetY() {
    return offY;
  }

  /**
   * Transforms the given graphics object.
   * 
   * @param gfx The graphics object.
   */
  public void transform(final Graphics2D gfx) {
    gfx.translate(offX, offY);
    gfx.scale(zoom, zoom);
  }

  /**
   * Transforms the given affine transformation.
   * 
   * @param at The affine transformation.
   */
  public void transform(final AffineTransform at) {
    at.translate(offX, offY);
    at.scale(zoom, zoom);
  }

  /**
   * Calculates the real coordinate of the given input in component coordinates.
   * 
   * @param s The coordinate in component coordinates. Due to uniform zooming
   *          both horizontal and vertical coordinates can be converted.
   * @return In real coordinates.
   */
  public double inReal(final double s) {
    return s / zoom;
  }

  /**
   * Calculates the component coordinate of the given input in real coordinates.
   * 
   * @param s The coordinate in real coordinates. Due to uniform zooming both
   *          horizontal and vertical coordinates can be converted.
   * @return In screen coordinates.
   */
  public double fromReal(final double s) {
    return s * zoom;
  }

  /**
   * Calculates the real coordinate from the components coordinate.
   * 
   * @param x The components x coordinate.
   * @return The real coordinate.
   */
  public double getXForScreen(final double x) {
    return inReal(x - offX);
  }

  /**
   * Calculates the real coordinate from the components coordinate.
   * 
   * @param y The components y coordinate.
   * @return The real coordinate.
   */
  public double getYForScreen(final double y) {
    return inReal(y - offY);
  }

  /**
   * Calculates the component coordinate from the real coordinate.
   * 
   * @param x The real x coordinate.
   * @return The component coordinate.
   */
  public double getXFromCanvas(final double x) {
    return fromReal(x) + offX;
  }

  /**
   * Calculates the component coordinate from the real coordinate.
   * 
   * @param y The real y coordinate.
   * @return The component coordinate.
   */
  public double getYFromCanvas(final double y) {
    return fromReal(y) + offY;
  }

  /**
   * Converts a point in component coordinates to canvas coordinates.
   * 
   * @param p The point.
   * @return The point in the canvas coordinates.
   */
  public Point2D getForScreen(final Point2D p) {
    return new Point2D.Double(getXForScreen(p.getX()), getYForScreen(p.getY()));
  }

  /**
   * Converts a rectangle in component coordinates to canvas coordinates.
   * 
   * @param rect The rectangle.
   * @return The rectangle in canvas coordinates.
   */
  public Rectangle2D toCanvas(final Rectangle2D rect) {
    final Point2D topLeft =
        getForScreen(new Point2D.Double(rect.getMinX(), rect.getMinY()));
    return new Rectangle2D.Double(topLeft.getX(), topLeft.getY(),
        inReal(rect.getWidth()), inReal(rect.getHeight()));
  }

  /**
   * Returns the minimal zoom value.
   * 
   * @return The minimal zoom value. If the value is non-positive then no
   *         restrictions are made.
   */
  public double getMinZoom() {
    return minZoom;
  }

  /**
   * Getter.
   * 
   * @return Whether zoom has a minimum.
   */
  public boolean hasMinZoom() {
    return minZoom > 0;
  }

  /**
   * Sets the current minimal zoom value.
   * 
   * @param zoom The new minimal zoom value. Non-positive values indicate no
   *          restriction.
   */
  public void setMinZoom(final double zoom) {
    minZoom = zoom;
  }

  /**
   * Returns the maximal zoom value.
   * 
   * @return The maximal zoom value. If the value is non-positive then no
   *         restrictions are made.
   */
  public double getMaxZoom() {
    return maxZoom;
  }

  /**
   * Getter.
   * 
   * @return Whether zoom has a maximum.
   */
  public boolean hasMaxZoom() {
    return maxZoom > 0;
  }

  /**
   * Sets the current maximal zoom value.
   * 
   * @param zoom The new maximal zoom value. Non-positive values indicate no
   *          restriction.
   */
  public void setMaxZoom(final double zoom) {
    maxZoom = zoom;
  }

}
