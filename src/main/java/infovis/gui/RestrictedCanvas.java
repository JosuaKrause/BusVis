package infovis.gui;

import java.awt.geom.Rectangle2D;

/**
 * Restricts the area of a {@link ZoomableUI}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface RestrictedCanvas {

  /**
   * Returns the bounding rectangle in canvas coordinates.
   * 
   * @return The bounding rectangle or <code>null</code> if the canvas should
   *         not be moveable at all.
   */
  Rectangle2D getBoundingRect();

  /**
   * Returns the visible rectangle in canvas coordinates.
   * 
   * @return The visible rectangle in canvas coordinates.
   */
  Rectangle2D getCurrentView();

}
