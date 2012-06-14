package infovis.overview;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Interactor for the SVG canvas.
 * 
 * @author Marc Spicker
 */
public class OverviewMouse extends MouseAdapter {

  /**
   * The overview visualization.
   */
  private final Overview over;

  /**
   * Constructor.
   * 
   * @param over The overview visualization.
   */
  public OverviewMouse(final Overview over) {
    this.over = over;
  }

  /**
   * The x offset.
   */
  private double offX;

  /**
   * The y offset.
   */
  private double offY;

  /**
   * The zoom level.
   */
  private double zoom = 1;

  /**
   * If dragging is in process.
   */
  private boolean drag;

  /**
   * The starting x coordinate.
   */
  private int startx;

  /**
   * The starting y coordinate.
   */
  private int starty;

  /**
   * The original x coordinate.
   */
  private double origX;

  /**
   * The original y coordinate.
   */
  private double origY;

  @Override
  public void mousePressed(final MouseEvent e) {
    over.grabFocus();
    final Point2D p = e.getPoint();
    final Point2D c = getForScreen(p);
    if(click(c)) return;
    final boolean leftButton = e.getButton() == MouseEvent.BUTTON1;

    if(leftButton) {
      startx = e.getX();
      starty = e.getY();
      origX = getOffsetX();
      origY = getOffsetY();
      drag = true;
    }
  }

  /**
   * Returns weather or not the user has clicked on a bus station.
   * 
   * @param c Click Point.
   * @return Weather a station has been clicked on.
   */
  private boolean click(final Point2D c) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    if(drag) {
      move(e.getX(), e.getY());
    }
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    if(drag) {

      move(e.getX(), e.getY());

      drag = false;
    }
  }

  /**
   * sets the offset according to the mouse position.
   * 
   * @param x the mouse x position
   * @param y the mouse y position
   */
  private void move(final int x, final int y) {
    setOffset(origX + (x - startx), origY + (y - starty));
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) {
    zoomTo(e.getX(), e.getY(), e.getWheelRotation());
  }

  /**
   * Setter.
   * 
   * @param x the x offset.
   * @param y the y offset.
   */
  public void setOffset(final double x, final double y) {
    offX = x;
    offY = y;
    updateTransformation();
  }

  /**
   * Updates the SVG rendering transformation.
   */
  private void updateTransformation() {
    final AffineTransform at = new AffineTransform();
    at.translate(offX, offY);
    at.scale(zoom, zoom);
    over.setRenderingTransform(at, true);
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
    // P = (off - mouse) / zoom
    // P = (newOff - mouse) / newZoom
    // newOff = (off - mouse) / zoom * newZoom + mouse
    // newOff = (off - mouse) * factor + mouse
    zoom *= factor;
    // does repaint
    setOffset((offX - x) * factor + x, (offY - y) * factor + y);
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
   * The margin for the viewport reset. The default is <code>10.0</code>.
   */
  private double margin = 10.0;

  /**
   * Getter.
   * 
   * @return The margin for viewport resets.
   */
  public double getMargin() {
    return margin;
  }

  /**
   * Setter.
   * 
   * @param margin Sets the margin for viewport resets.
   */
  public void setMargin(final double margin) {
    this.margin = margin;
  }

  /**
   * Resets the viewport to show exactly the given rectangle expanded by the
   * margin given by {@link #getMargin()}.
   * 
   * @param bbox The rectangle that is visible.
   */
  public void reset(final Rectangle2D bbox) {
    if(bbox == null) {
      reset();
    } else {
      final double margin = getMargin();
      final Rectangle2D rect = over.getVisibleRect();
      final int nw = (int) (rect.getWidth() - 2 * margin);
      final int nh = (int) (rect.getHeight() - 2 * margin);
      zoom = 1.0;
      // does repaint
      setOffset(margin + (nw - bbox.getWidth()) / 2 - bbox.getMinX(), margin
          + (nh - bbox.getHeight()) / 2 - bbox.getMinY());
      final double rw = nw / bbox.getWidth();
      final double rh = nh / bbox.getHeight();
      final double factor = rw < rh ? rw : rh;
      zoom(factor);
    }
  }

  /**
   * Whether the canvas is moveable, ie it can be panned and zoomed.
   */
  private boolean isMoveable = true;

  /**
   * Sets whether the canvas is moveable, ie whether it can be panned or zoomed.
   * 
   * @param isMoveable If it is moveable.
   */
  public void setMoveable(final boolean isMoveable) {
    this.isMoveable = isMoveable;
  }

  /**
   * Getter.
   * 
   * @return Is <code>true</code>, when the canvas can be panned and zoomed.
   */
  public boolean isMoveable() {
    return isMoveable;
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
   * Converts a point in component coordinates in canvas coordinates.
   * 
   * @param p The point.
   * @return The point in the canvas coordinates.
   */
  protected Point2D getForScreen(final Point2D p) {
    return new Point2D.Double(getXForScreen(p.getX()), getYForScreen(p.getY()));
  }
}
