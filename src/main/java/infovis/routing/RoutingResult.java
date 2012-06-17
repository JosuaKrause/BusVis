package infovis.routing;

import infovis.data.BusStation;

/**
 * Encapsulates the result of a routing.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class RoutingResult {

  /**
   * The start bus station.
   */
  private final BusStation from;

  /**
   * The end bus station.
   */
  private final BusStation to;

  /**
   * The travel time.
   */
  private final int minutes;

  /**
   * Whether the destination is not reachable.
   */
  private final boolean inr;

  /**
   * Creates a routing result.
   * 
   * @param from The start station.
   * @param to The end station.
   * @param minutes The travel time.
   * @param inr Whether the destination is not reachable.
   */
  public RoutingResult(final BusStation from, final BusStation to, final int minutes,
      final boolean inr) {
    this.from = from;
    this.to = to;
    this.minutes = minutes;
    this.inr = inr;
  }

  /**
   * Getter.
   * 
   * @return The start bus station.
   */
  public BusStation getStart() {
    return from;
  }

  /**
   * Getter.
   * 
   * @return The end bus station.
   */
  public BusStation getEnd() {
    return to;
  }

  /**
   * Getter.
   * 
   * @return The travel time in minutes.
   */
  public int minutes() {
    if(minutes < 0) throw new IllegalStateException("has no time");
    return minutes;
  }

  /**
   * Getter.
   * 
   * @return Whether the destination is not reachable.
   */
  public boolean isNotReachable() {
    return inr;
  }

}
