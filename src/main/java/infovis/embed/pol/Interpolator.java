package infovis.embed.pol;

/**
 * An interpolator maps values from 0 to 1 to 0 to 1.
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

}
