package infovis.overview;

import infovis.ctrl.Controller;
import infovis.data.BusStation;
import infovis.gui.MouseInteraction;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * Interactor for the SVG canvas.
 * 
 * @author Marc Spicker
 */
public final class OverviewMouse extends MouseInteraction {

  /** The overview visualization. */
  private final Overview over;

  /** Controller. */
  private final Controller ctrl;

  /**
   * The minimal zoom factor. Gets determined dynamically when the abstract view
   * completely shown.
   */
  private double minZoom = -1;

  /** The maximal zoom factor. */
  private final double maxZoom = 2.5;

  /**
   * Constructor.
   * 
   * @param over The overview visualization.
   * @param ctrl Controller.
   */
  public OverviewMouse(final Overview over, final Controller ctrl) {
    this.over = over;
    this.ctrl = ctrl;
    focus = over;
  }

  /** The x offset. */
  private double offX;

  /** The y offset. */
  private double offY;

  /** The zoom level. */
  private double zoom = 1;

  /** The focused component. */
  private JComponent focus;

  @Override
  public void mousePressed(final MouseEvent e) {
    focus.grabFocus();
    final Point2D p = e.getPoint();
    final Point2D c = getForScreen(p);
    final boolean leftButton = SwingUtilities.isLeftMouseButton(e);
    if(click(c, leftButton)) return;

    if(leftButton) {
      startDragging(e, getOffsetX(), getOffsetY());
    }
  }

  /**
   * Setter.
   * 
   * @param focus The component to focus when clicked.
   */
  public void setFocusComponent(final JComponent focus) {
    if(focus == null) {
      new NullPointerException("focus");
    }
    this.focus = focus;
  }

  /**
   * Getter.
   * 
   * @return The component to focus when clicked.
   */
  public JComponent getFocusComponent() {
    return focus;
  }

  /** The radius in which, if clicked, the station is selected. */
  public static final int STATION_RADIUS = 10;

  /**
   * Returns whether or not the user has clicked on a bus station.
   * 
   * @param c Click Point.
   * @param leftButton Whether the click is a left button click.
   * @return Whether a station has been clicked on.
   */
  private boolean click(final Point2D c, final boolean leftButton) {
    double minDist = Double.POSITIVE_INFINITY;
    BusStation closestStation = null;
    for(final BusStation station : ctrl.getStations()) {
      final double curDist = distanceToStationSq(c, station);
      if(curDist < minDist) {
        minDist = curDist;
        closestStation = station;
      }
    }
    if(minDist >= STATION_RADIUS * STATION_RADIUS) {
      if(!leftButton) {
        ctrl.clearSecondarySelection();
        return true;
      }
      return false;
    }
    if(leftButton) {
      ctrl.selectStation(closestStation);
    } else {
      ctrl.toggleSecondarySelected(closestStation);
    }
    return true;
  }

  /**
   * Returns the distance of a point to an abstract station position.
   * 
   * @param p The point.
   * @param station The station.
   * @return Distance between the point and the station.
   */
  private static double distanceToStationSq(final Point2D p, final BusStation station) {
    final double dx = p.getX() - station.getAbstractX();
    final double dy = p.getY() - station.getAbstractY();
    return dx * dx + dy * dy;
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    if(isDragging()) {
      move(e.getX(), e.getY());
    }
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    mouseDragged(e);
    stopDragging();
  }

  /**
   * Sets the offset according to the mouse position.
   * 
   * @param x the mouse x position
   * @param y the mouse y position
   */
  private void move(final int x, final int y) {
    setOffset(getMoveX(x), getMoveY(y));
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) {
    if(!isDragging()) {
      zoomTo(e.getX(), e.getY(), e.getWheelRotation());
    }
  }

  /**
   * Setter.
   * 
   * @param x the x offset.
   * @param y the y offset.
   */
  public void setOffset(final double x, final double y) {
    final Rectangle2D svgBB = over.getSVGBoundingRect();
    if(svgBB == null) return;

    offX = x;
    offY = y;
    final Rectangle2D visBB = getVisibleCanvas();

    // snap back
    if(!svgBB.contains(visBB)) {
      double transX = 0;
      double transY = 0;

      if(visBB.getMaxX() > svgBB.getMaxX()) {
        // too far right
        transX -= visBB.getMaxX() - svgBB.getMaxX();
      } else if(visBB.getMinX() < svgBB.getMinX()) {
        // too far left
        transX += svgBB.getMinX() - visBB.getMinX();
      }
      if(visBB.getMaxY() > svgBB.getMaxY()) {
        // too far down
        transY -= visBB.getMaxY() - svgBB.getMaxY();
      } else if(visBB.getMinY() < svgBB.getMinY()) {
        // too far up
        transY += svgBB.getMinY() - visBB.getMinY();
      }

      offX -= fromReal(transX);
      offY -= fromReal(transY);
    }
    updateTransformation();
  }

  /**
   * Returns the visible rectangle in canvas coordinates.
   * 
   * @return The visible rectangle in canvas coordinates.
   */
  public Rectangle2D getVisibleCanvas() {
    final Rectangle2D rect = over.getVisibleRect();
    final Point2D topLeft = getForScreen(new Point2D.Double(rect.getMinX(),
        rect.getMinY()));
    return new Rectangle2D.Double(topLeft.getX(), topLeft.getY(),
        inReal(rect.getWidth()), inReal(rect.getHeight()));
  }

  /** Updates the SVG rendering transformation. */
  private void updateTransformation() {
    final AffineTransform at = new AffineTransform();
    at.translate(offX, offY);
    at.scale(zoom, zoom);
    over.setRenderingTransform(at, true);
  }

  /**
   * Transforms a input graphics context with the current translation and
   * scaling.
   * 
   * @param g The input graphics context.
   */
  public void transformGraphics(final Graphics2D g) {
    g.translate(offX, offY);
    g.scale(zoom, zoom);
  }

  /**
   * Zooms to the on screen (in components coordinates) position.
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
   * Zooms to the on screen (in components coordinates) position.
   * 
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param factor The factor to alter the zoom level.
   */
  public void zoomTo(final double x, final double y, final double factor) {
    double f = factor;
    double newZoom = zoom * factor;
    if(newZoom < minZoom) {
      newZoom = minZoom;
      f = newZoom / zoom;
    } else if(newZoom > maxZoom) {
      newZoom = maxZoom;
      f = newZoom / zoom;
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
   * Zooms towards the center of the display area.
   * 
   * @param factor The zoom factor.
   */
  public void zoom(final double factor) {
    final Rectangle box = over.getVisibleRect();
    zoomTo(box.width / 2.0, box.height / 2.0, factor);
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
   * Resets the viewport to a scaling of <code>1.0</code> and
   * <code>(0, 0)</code> being in the center of the component.
   */
  public void reset() {
    final Rectangle2D rect = over.getVisibleRect();
    zoom = 1;
    setOffset(rect.getCenterX(), rect.getCenterY());
  }

  /**
   * Resets the viewport to show exactly the given rectangle.
   * 
   * @param bbox The rectangle that is visible.
   */
  public void reset(final Rectangle2D bbox) {
    if(bbox == null) {
      reset();
    } else {
      final Rectangle2D rect = over.getVisibleRect();
      final int nw = (int) (rect.getWidth());
      final int nh = (int) (rect.getHeight());
      zoom = 1.0;
      // does repaint
      setOffset((nw - bbox.getWidth()) / 2 - bbox.getMinX(),
          (nh - bbox.getHeight()) / 2 - bbox.getMinY());
      final double rw = nw / bbox.getWidth();
      final double rh = nh / bbox.getHeight();
      final double factor = rw > rh ? rw : rh;
      if(minZoom == -1) {
        minZoom = factor;
      }
      zoom(factor);
    }
  }

  /**
   * Resets the visible rect and re-determindes the minimal zoom value.
   * 
   * @param newBB The new visible rect.
   */
  public void visibleRectChanged(final Rectangle2D newBB) {
    minZoom = -1;
    reset(newBB);
  }

  /**
   * Calculates the real coordinate of the given input in screen coordinates.
   * 
   * @param s The coordinate in screen coordinates. Due to uniform zooming both
   *          horizontal and vertical coordinates can be converted.
   * @return In real coordinates.
   */
  protected double inReal(final double s) {
    return s / zoom;
  }

  /**
   * Calculates the screen coordinate of the given input in real coordinates.
   * 
   * @param s The coordinate in real coordinates. Due to uniform zooming both
   *          horizontal and vertical coordinates can be converted.
   * @return In screen coordinates.
   */
  protected double fromReal(final double s) {
    return s * zoom;
  }

  /**
   * Calculates the real coordinate from the components coordinate.
   * 
   * @param x The components x coordinate.
   * @return The real coordinate.
   */
  protected double getXForScreen(final double x) {
    return inReal(x - offX);
  }

  /**
   * Calculates the real coordinate from the components coordinate.
   * 
   * @param y The components y coordinate.
   * @return The real coordinate.
   */
  protected double getYForScreen(final double y) {
    return inReal(y - offY);
  }

  /**
   * Calculates the component coordinate from the real coordinate.
   * 
   * @param x The real x coordinate.
   * @return The component coordinate.
   */
  protected double getXFromCanvas(final double x) {
    return fromReal(x) + offX;
  }

  /**
   * Calculates the component coordinate from the real coordinate.
   * 
   * @param y The real y coordinate.
   * @return The component coordinate.
   */
  protected double getYFromCanvas(final double y) {
    return fromReal(y) + offY;
  }

  /**
   * Converts a point in component coordinates in canvas coordinates.
   * 
   * @param p The point.
   * @return The point in the canvas coordinates.
   */
  protected Point2D getForScreen(final Point2D p) {
    return new Point2D.Double(getXForScreen(p.getX()), getYForScreen(p.getY()));
  }

}
