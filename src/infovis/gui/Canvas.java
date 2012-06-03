package infovis.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * A simple class adding panning and zooming functionality to a
 * {@link JComponent}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class Canvas extends JComponent implements Refreshable {

  /**
   * SVUID.
   */
  private static final long serialVersionUID = 5148536262867772166L;

  /**
   * The painter.
   */
  private final Painter painter;

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
   * Creates a canvas for the given painter.
   * 
   * @param painter The painter.
   * @param width The initial width of the component.
   * @param height The initial height of the component.
   */
  public Canvas(final Painter painter, final int width, final int height) {
    setPreferredSize(new Dimension(width, height));
    this.painter = painter;
    final MouseAdapter mouse = new MouseAdapter() {

      private boolean drag;

      private int startx;

      private int starty;

      private double origX;

      private double origY;

      @Override
      public void mousePressed(final MouseEvent e) {
        final Point2D p = e.getPoint();
        if(painter.clickHUD(p)) {
          Canvas.this.repaint();
          return;
        }
        final Point2D c = getForScreen(p);
        if(painter.click(c)) {
          Canvas.this.repaint();
          return;
        }
        if(isMoveable() && e.getButton() == MouseEvent.BUTTON1) {
          startx = e.getX();
          starty = e.getY();
          origX = getOffsetX();
          origY = getOffsetY();
          drag = true;
        }
        grabFocus();
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
       * sets the offset according to the mouse position
       * 
       * @param x the mouse x position
       * @param y the mouse y position
       */
      private void move(final int x, final int y) {
        setOffset(origX + (x - startx), origY + (y - starty));
      }

      @Override
      public void mouseWheelMoved(final MouseWheelEvent e) {
        if(isMoveable()) {
          zoomTo(e.getX(), e.getY(), e.getWheelRotation());
        }
      }

    };
    addMouseListener(mouse);
    addMouseMotionListener(mouse);
    addMouseWheelListener(mouse);
    setFocusable(true);
    grabFocus();
    setupKeyActions();
  }

  /**
   * This method is called when it is save to add keyboard actions with
   * {@link #addAction(int, Action)}. You should overwrite this method if you
   * want keyboard interaction for this canvas.
   */
  public void setupKeyActions() {
    // to be overwritten
  }

  /**
   * Adds a keyboard action event.
   * 
   * @param key The key id, given by {@link KeyEvent}. (Constants beginning with
   *          <code>VK</code>)
   * @param a The action that is performed.
   */
  protected void addAction(final int key, final Action a) {
    final Object token = new Object();
    final InputMap input = getInputMap();
    input.put(KeyStroke.getKeyStroke(key, 0), token);
    final ActionMap action = getActionMap();
    action.put(token, a);
  }

  /**
   * The back ground color of the component or <code>null</code> if it is
   * transparent.
   */
  private Color back;

  @Override
  public void setBackground(final Color bg) {
    back = bg;
    super.setBackground(bg);
  }

  @Override
  public void paintComponent(final Graphics g) {
    final Graphics2D g2 = (Graphics2D) g.create();
    g2.clip(getVisibleRect());
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    final Color c = back;
    if(c != null) {
      g2.setColor(c);
      g2.fill(getVisibleRect());
    }
    final Graphics2D gfx = (Graphics2D) g2.create();
    gfx.translate(offX, offY);
    gfx.scale(zoom, zoom);
    painter.draw(gfx);
    gfx.dispose();
    painter.drawHUD(g2);
    g2.dispose();
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
    repaint();
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
    final Rectangle box = getVisibleRect();
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
    final Rectangle2D rect = getVisibleRect();
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
      final Rectangle2D rect = getVisibleRect();
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

  @Override
  public void refresh() {
    repaint();
  }

}
