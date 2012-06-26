package infovis.gui;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * The paint context provides information of the state of drawing. Methods for
 * converting lengths and points from canvas coordinates to component
 * coordinates are provided.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface Context {

  /**
   * Converts a point from canvas to component coordinates.
   * 
   * @param p The point in canvas coordinates.
   * @return The point in component coordinates.
   */
  Point2D toComponentCoordinates(Point2D p);

  /**
   * Converts a length from canvas to component coordinates.
   * 
   * @param length The length in canvas coordinates.
   * @return The length in component coordinates.
   */
  double toComponentLength(double length);

  /**
   * Converts a point from component to canvas coordinates.
   * 
   * @param p The point in component coordinates.
   * @return The point in canvas coordinates.
   */
  Point2D toCanvasCoordinates(Point2D p);

  /**
   * Converts a length from component to canvas coordinates.
   * 
   * @param length The length in component coordinates.
   * @return The length in canvas coordinates.
   */
  double toCanvasLength(double length);

  /**
   * Whether this context is currently in canvas coordinates. Meaning if a point
   * is in canvas coordinates it does not need to be converted and if it is in
   * component coordinates the method {@link #toCanvasCoordinates(Point2D)} must
   * be used. When this method return <code>false</code> the opposite is true.
   * This applies to length as well.
   * 
   * @return Whether this context is currently in canvas coordinates.
   */
  boolean inCanvasCoordinates();

  /**
   * Getter.
   * 
   * @return The visible rectangle in component coordinates.
   */
  Rectangle2D getVisibleComponent();

  /**
   * Getter.
   * 
   * @return The visible rectangle in canvas coordinates.
   */
  Rectangle2D getVisibleCanvas();

}
