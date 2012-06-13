package infovis.embed.pol;

import static java.lang.Math.*;

/**
 * A sinusodial interpolator.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class SinInterpolator implements Interpolator {

  /**
   * Half pi.
   */
  private static final double PI2 = PI * 0.5;

  @Override
  public double interpolate(final double t) {
    return sin(t * PI - PI2) * 0.5 + 0.5;
  }

}
