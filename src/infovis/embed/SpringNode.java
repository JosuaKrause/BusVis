package infovis.embed;

import java.util.Random;


public class SpringNode {

  private double x;

  private double y;

  private double dx;

  private double dy;

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
      final double diffX = ox - x;
      final double diffY = oy - y;
      final double dist = Math.sqrt(diffX * diffX + diffY * diffY);
      dx += (diffX - diffX / dist * d) * 0.5;
      dy += (diffY - diffY / dist * d) * 0.5;
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

  public void step() {
    x += dx;
    y += dy;
    dx = 0;
    dy = 0;
  }

  public double getDx() {
    return dx;
  }

  public double getDy() {
    return dy;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public void addMove(final double dx, final double dy) {
    this.dx += dx;
    this.dy += dy;
  }

}
