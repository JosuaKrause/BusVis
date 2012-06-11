package infovis.embed;

import java.awt.geom.Point2D;

/**
 * A simple circular embedder.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class CircularEmbedder extends AbstractEmbedder {

  /**
   * The weighter.
   */
  private final Weighter weighter;

  /**
   * Creates a circular embedder.
   * 
   * @param weighter The weighter.
   * @param drawer The drawer.
   */
  public CircularEmbedder(final Weighter weighter, final NodeDrawer drawer) {
    super(drawer);
    this.weighter = weighter;
  }

  @Override
  protected void step() {
    final SpringNode ref = weighter.getReferenceNode();
    Point2D refP;
    if(ref != null) {
      refP = ref.getPos();
    } else {
      refP = null;
    }
    for(final SpringNode n : weighter.nodes()) {
      final Point2D pos = weighter.getDefaultPosition(n);
      if(n == ref) {
        continue;
      }
      if(refP == null) {
        n.setPosition(pos);
        continue;
      }
      if(!weighter.hasWeight(n, ref)) {
        n.setPosition(new Point2D.Double());
        continue;
      }
      final double w = weighter.weight(n, ref);
      n.setPosition(addVec(setLength(subVec(pos, refP), w), refP));
    }
  }

  /**
   * Adds two points.
   * 
   * @param a Point.
   * @param b Point.
   * @return The sum vector.
   */
  public static final Point2D addVec(final Point2D a, final Point2D b) {
    return new Point2D.Double(a.getX() + b.getX(), a.getY() + b.getY());
  }

  /**
   * Subtracts two points.
   * 
   * @param a Point.
   * @param b Point.
   * @return The difference vector.
   */
  public static final Point2D subVec(final Point2D a, final Point2D b) {
    return new Point2D.Double(a.getX() - b.getX(), a.getY() - b.getY());
  }

  /**
   * Multiplies a point with a scalar.
   * 
   * @param v Point.
   * @param s Scalar.
   * @return The scaled vector.
   */
  public static final Point2D mulVec(final Point2D v, final double s) {
    return new Point2D.Double(v.getX() * s, v.getY() * s);
  }

  /**
   * Calculates the squared length of a vector.
   * 
   * @param v The vector.
   * @return The squared length.
   */
  public static final double getLengthSq(final Point2D v) {
    return v.getX() * v.getX() + v.getY() * v.getY();
  }

  /**
   * Calculates the length of a vector.
   * 
   * @param v The vector.
   * @return The length.
   */
  public static final double getLength(final Point2D v) {
    return Math.sqrt(getLengthSq(v));
  }

  /**
   * Sets the length of a vector.
   * 
   * @param v The vector.
   * @param l The new length.
   * @return The vector with the given length.
   */
  public static final Point2D setLength(final Point2D v, final double l) {
    return mulVec(v, l / getLength(v));
  }

}
