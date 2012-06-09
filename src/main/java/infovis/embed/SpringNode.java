package infovis.embed;

import java.util.Random;

/**
 * A node in a spring embedder system.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class SpringNode {

  /**
   * The current x position.
   */
  private double x;

  /**
   * The current y position.
   */
  private double y;

  /**
   * The current x movement.
   */
  private double dx;

  /**
   * The current y movement.
   */
  private double dy;

  /**
   * Calculates the movement of the node.
   * 
   * @param w The weighter, determining the weights of the edges between the
   *          nodes.
   */
  public void move(final Weighter w) {
    boolean rnd = false;
    double dx = 0;
    double dy = 0;
    for(final SpringNode n : w.nodes()) {
      if(n == this) {
        continue;
      }
      if(!w.hasWeight(this, n)) {
        continue;
      }
      final double d = w.weight(this, n);
      final double ox = n.getX();
      final double oy = n.getY();
      if(x == ox && y == oy) {
        rnd = true;
        continue;
      }
      rnd = false;
      final double diffX = ox - x;
      final double diffY = oy - y;
      final double dist = Math.sqrt(diffX * diffX + diffY * diffY);
      if(d > 0) {
        dx += (diffX - diffX / dist * d) * 0.5;
        dy += (diffY - diffY / dist * d) * 0.5;
      } else {
        final double f = dist <= -d ? 1 : Math.exp(-(dist + d) * (dist + d));
        dx += (diffX + diffX / dist * d) * 0.5 * f;
        dy += (diffY + diffY / dist * d) * 0.5 * f;
      }
    }
    if(rnd && dx == 0 && dy == 0) {
      final Random r = new Random(hashCode());
      dx = r.nextGaussian();
      dy = r.nextGaussian();
    }
    final double c = w.springConstant();
    this.dx += dx * c;
    this.dy += dy * c;
  }

  /**
   * Actually moves the node and resets the movement values.
   */
  public void step() {
    x += dx;
    y += dy;
    dx = 0;
    dy = 0;
  }

  /**
   * Getter.
   * 
   * @return The current x movement.
   */
  public double getDx() {
    return dx;
  }

  /**
   * Getter.
   * 
   * @return The current y movement.
   */
  public double getDy() {
    return dy;
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
   * Manually adds a movement to the current motion.
   * 
   * @param dx The x movement.
   * @param dy The y movement.
   */
  public void addMove(final double dx, final double dy) {
    this.dx += dx;
    this.dy += dy;
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
    dx = 0;
    dy = 0;
  }

}