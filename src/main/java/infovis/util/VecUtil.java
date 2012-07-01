package infovis.util;

import java.awt.geom.AffineTransform;
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

  /**
   * Rotates a point around the center such that the result has the given
   * distance to the original point.
   * 
   * @param pos The point to rotate.
   * @param center The center.
   * @param dist The distance.
   * @return The point that has the given distance to the original point.
   * @throws IllegalArgumentException When the distance is longer than the
   *           diameter.
   */
  public static Point2D rotate(final Point2D pos, final Point2D center, final double dist) {
    final double f = dist > 0 ? 1 : -1;
    final double dSq = dist * dist;
    final Point2D rad = subVec(pos, center);
    final double radSq = getLengthSq(rad);
    if(dSq > 4 * radSq) throw new IllegalArgumentException("distance too long");
    return rotateByAngle(pos, center, f * Math.acos(1 - dSq * 0.5 / radSq));
  }

  /**
   * Rotates a point a given angle around the center.
   * 
   * @param pos The point to rotate.
   * @param center The center.
   * @param angle The angle.
   * @return The rotated point.
   */
  public static Point2D rotateByAngle(final Point2D pos, final Point2D center,
      final double angle) {
    final AffineTransform at = AffineTransform.getRotateInstance(angle, center.getX(),
        center.getY());
    return at.transform(pos, null);
  }

}
