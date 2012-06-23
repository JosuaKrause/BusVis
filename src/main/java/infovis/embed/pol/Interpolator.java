package infovis.embed.pol;

import static java.lang.Math.*;

/**
 * An interpolator maps values from the range <code>[0,1]</code> to
 * <code>[0,1]</code>.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface Interpolator {

  /**
   * Maps the input to a value from 0 to 1.
   * 
   * @param t The input value from 0 to 1.
   * @return The mapped value from 0 to 1.
   */
  double interpolate(double t);

  /** Smooth interpolation. */
  Interpolator SMOOTH = new Interpolator() {
    @Override
    public double interpolate(final double t) {
      return sin((t - 0.5) * PI) * 0.5 + 0.5;
    }
  };

  /** Linear interpolation. */
  Interpolator LINEAR = new Interpolator() {
    @Override
    public double interpolate(final double t) {
      return t;
    }
  };

  /**
   * The standard animation duration.
   */
  int NORMAL = 1000;

  /**
   * Fast animation duration.
   */
  int FAST = 100;

}
