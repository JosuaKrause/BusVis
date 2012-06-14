package infovis;

import java.awt.geom.Point2D;

/**
 * A small vector utility class.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class VecUtil {

  /**
   * No constructor.
   */
  private VecUtil() {
    // nothing to construct
  }

  /**
   * Adds two points.
   * 
   * @param a Point.
   * @param b Point.
   * @return The sum vector.
   */
  public static Point2D addVec(final Point2D a, final Point2D b) {
    return new Point2D.Double(a.getX() + b.getX(), a.getY() + b.getY());
  }

  /**
   * Subtracts two points.
   * 
   * @param a Point.
   * @param b Point.
   * @return The difference vector.
   */
  public static Point2D subVec(final Point2D a, final Point2D b) {
    return new Point2D.Double(a.getX() - b.getX(), a.getY() - b.getY());
  }

  /**
   * Multiplies a point with a scalar.
   * 
   * @param v Point.
   * @param s Scalar.
   * @return The scaled vector.
   */
  public static Point2D mulVec(final Point2D v, final double s) {
    return new Point2D.Double(v.getX() * s, v.getY() * s);
  }

  /**
   * Calculates the squared length of a vector.
   * 
   * @param v The vector.
   * @return The squared length.
   */
  public static double getLengthSq(final Point2D v) {
    return v.getX() * v.getX() + v.getY() * v.getY();
  }

  /**
   * Calculates the length of a vector.
   * 
   * @param v The vector.
   * @return The length.
   */
  public static double getLength(final Point2D v) {
    return Math.sqrt(getLengthSq(v));
  }

  /**
   * Sets the length of a vector.
   * 
   * @param v The vector.
   * @param l The new length.
   * @return The vector with the given length.
   */
  public static Point2D setLength(final Point2D v, final double l) {
    return mulVec(v, l / getLength(v));
  }

}
