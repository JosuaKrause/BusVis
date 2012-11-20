package infovis.busvis;

import infovis.util.Interpolator;

import java.awt.geom.Point2D;

/**
 * A node in a layout.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class LayoutNode {

  /** The dense id of the node. */
  private final int id;

  /** The current x position. */
  private double x;

  /** The current y position. */
  private double y;

  /** The animation start point. */
  private Point2D start;

  /** The animation end point. */
  private Point2D end;

  /** The interpolation method or <code>null</code> if no animation is active. */
  private Interpolator pol;

  /** The start time. */
  private long startTime;

  /** The end time. */
  private long endTime;

  /**
   * Creates a spring node.
   * 
   * @param id The dense id.
   */
  public LayoutNode(final int id) {
    this.id = id;
  }

  /**
   * Getter.
   * 
   * @return The id.
   */
  public int getId() {
    return id;
  }

  /**
   * Getter.
   * 
   * @return The current x position.
   */
  public double getX() {
    return x;
  }

  /**
   * Getter.
   * 
   * @return The current y position.
   */
  public double getY() {
    return y;
  }

  /**
   * Getter.
   * 
   * @return The x final position after the animation has finished.
   */
  public double getPredictX() {
    return end != null ? end.getX() : getX();
  }

  /**
   * Getter.
   * 
   * @return The final y position after the animation has finished.
   */
  public double getPredictY() {
    return end != null ? end.getY() : getY();
  }

  /**
   * Getter.
   * 
   * @return The current position.
   */
  public Point2D getPos() {
    return new Point2D.Double(getX(), getY());
  }

  /**
   * Sets a new position for the node and resets velocity.
   * 
   * @param x The new x position.
   * @param y The new y position.
   */
  public void setPosition(final double x, final double y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Sets a new position for the node and resets velocity.
   * 
   * @param pos The new position.
   */
  public void setPosition(final Point2D pos) {
    setPosition(pos.getX(), pos.getY());
  }

  /** The long animation duration. */
  public static final int LONG = 2000;

  /** The standard animation duration. */
  public static final int NORMAL = 1000;

  /** Fast animation duration. */
  public static final int FAST = 100;

  /**
   * Starts an animation to the given point.
   * 
   * @param pos The end point.
   * @param pol The interpolation method.
   * @param duration The duration in milliseconds.
   */
  public void startAnimationTo(final Point2D pos,
      final Interpolator pol, final int duration) {
    clearAnimation();
    if(duration <= 0) {
      setPosition(pos);
      return;
    }
    final long millis = System.currentTimeMillis();
    startTime = millis;
    endTime = millis + duration;
    this.pol = pol;
    start = getPos();
    end = pos;
  }

  /**
   * Sets the current animation to a new destination. If no current animation is
   * active a new one is created with the given default values.
   * 
   * @param pos The new destination position.
   * @param defaultPol The default interpolation that is used when no animation
   *          is active.
   * @param defaultDuration The default duration that is used when no animation
   *          is active in milliseconds.
   */
  public void changeAnimationTo(final Point2D pos,
      final Interpolator defaultPol, final int defaultDuration) {
    final Interpolator p = pol;
    final long et = endTime;
    animate();
    if(!inAnimation()) {
      startAnimationTo(pos, defaultPol, defaultDuration);
      return;
    }
    start = getPos();
    end = pos;
    pol = p;
    startTime = System.currentTimeMillis();
    endTime = et;
  }

  /** Animates the position. */
  public void animate() {
    if(!inAnimation()) return;
    final long millis = System.currentTimeMillis();
    if(millis >= endTime) {
      setPosition(end);
      start = null;
      end = null;
      pol = null;
      return;
    }
    final double t = ((double) millis - startTime) / ((double) endTime - startTime);
    final double f = pol.interpolate(t);
    setPosition(start.getX() * (1 - f) + end.getX() * f,
        start.getY() * (1 - f) + end.getY() * f);
  }

  /**
   * Getter.
   * 
   * @return Whether this node is in animation.
   */
  public boolean inAnimation() {
    return pol != null;
  }

  /** Lazy in animation memory. */
  private boolean lazyIa;

  /**
   * Getter.
   * 
   * @return Whether this node is in animation. This method returns
   *         <code>true</code> one time after {@link #inAnimation()} returns
   *         <code>false</code>.
   */
  public boolean lazyInAnimation() {
    final boolean ia = inAnimation();
    lazyIa |= ia;
    final boolean res = lazyIa;
    lazyIa &= ia;
    return res;
  }

  /** Aborts the current animation and keeps the current position. */
  public void clearAnimation() {
    animate();
    pol = null;
    start = null;
    end = null;
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof LayoutNode && ((LayoutNode) obj).id == id;
  }

  @Override
  public int hashCode() {
    return id;
  }

}
