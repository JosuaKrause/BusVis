package infovis.routing;

import infovis.data.BusEdge;
import infovis.data.BusStation;
import infovis.data.BusTime;

import java.util.Collection;

/**
 * Encapsulates the result of a routing.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class RoutingResult {

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
   * The edges used by this route.
   */
  private final Collection<BusEdge> edges;

  /**
   * The start time.
   */
  private final BusTime startTime;

  /**
   * Creates a routing result for a reachable station.
   * 
   * @param from The start station.
   * @param to The end station.
   * @param minutes The travel time.
   * @param edges The edges used by this route.
   * @param startTime The start time.
   */
  public RoutingResult(final BusStation from, final BusStation to, final int minutes,
      final Collection<BusEdge> edges, final BusTime startTime) {
    this.from = from;
    this.to = to;
    this.minutes = minutes;
    this.edges = edges;
    this.startTime = startTime;
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
    minutes = from != to ? -1 : 0;
    edges = null;
    startTime = null;
  }

  /**
   * Creates the start node.
   * 
   * @param start The start station.
   */
  public RoutingResult(final BusStation start) {
    this(start, start);
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
    return edges == null && from != to;
  }

  /**
   * Getter.
   * 
   * @return Whether this route is just the start station.
   */
  public boolean isStartNode() {
    return from == to;
  }

  /**
   * Getter.
   * 
   * @return The edges used by this route.
   */
  public Iterable<BusEdge> getEdges() {
    return edges;
  }

  /**
   * Getter.
   * 
   * @return The start time.
   */
  public BusTime getStartTime() {
    return startTime;
  }

  /**
   * Getter.
   * 
   * @return The end time.
   */
  public BusTime getEndTime() {
    return startTime.later(minutes());
  }

}
