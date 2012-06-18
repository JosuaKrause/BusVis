package infovis.routing;

import infovis.data.BusEdge;
import infovis.data.BusStation;

import java.util.Collection;

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
   * The edges used by this route.
   */
  private final Collection<BusEdge> edges;

  /**
   * Creates a routing result for a reachable station.
   * 
   * @param from The start station.
   * @param to The end station.
   * @param minutes The travel time.
   * @param edges The edges used by this route.
   */
  public RoutingResult(final BusStation from, final BusStation to, final int minutes,
      final Collection<BusEdge> edges) {
    this.from = from;
    this.to = to;
    this.minutes = minutes;
    this.edges = edges;
    inr = false;
  }

  /**
   * Creates a routing result for a not reachable station.
   * 
   * @param from The start station.
   * @param to The end station that is not reachable.
   */
  public RoutingResult(final BusStation from, final BusStation to) {
    this.from = from;
    this.to = to;
    minutes = -1;
    edges = null;
    inr = true;
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

  /**
   * Getter.
   * 
   * @return The edges used by this route.
   */
  public Iterable<BusEdge> getEdges() {
    return edges;
  }

}
