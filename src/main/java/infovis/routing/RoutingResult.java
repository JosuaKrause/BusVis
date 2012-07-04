package infovis.routing;

import infovis.data.BusEdge;
import infovis.data.BusLine;
import infovis.data.BusStation;
import infovis.data.BusTime;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Encapsulates the result of a routing.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class RoutingResult {

  /** The start bus station. */
  private final BusStation from;

  /** The end bus station. */
  private final BusStation to;

  /** The travel time. */
  private final int seconds;

  /** The edges used by this route. */
  private final Collection<BusEdge> edges;

  /** The start time. */
  private final BusTime startTime;

  /**
   * Creates a routing result for a reachable station.
   * 
   * @param from The start station.
   * @param to The end station.
   * @param edges The edges used by this route.
   * @param startTime The start time.
   * @param seconds The travel time.
   */
  public RoutingResult(final BusStation from, final BusStation to, final BusEdge[] edges,
      final BusTime startTime, final int seconds) {
    this(from, to, Arrays.asList(edges), startTime, seconds);
  }

  /**
   * Creates a routing result for a reachable station.
   * 
   * @param from The start station.
   * @param to The end station.
   * @param edges The edges used by this route.
   * @param startTime The start time.
   * @param seconds The travel time.
   */
  public RoutingResult(final BusStation from, final BusStation to,
      final Collection<BusEdge> edges, final BusTime startTime, final int seconds) {
    this.from = from;
    this.to = to;
    this.seconds = seconds;
    this.edges = Collections.unmodifiableCollection(edges);
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
    seconds = from != to ? -1 : 0;
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
   * @return The travel time in seconds.
   */
  public int seconds() {
    if(seconds < 0) throw new IllegalStateException("has no time");
    return seconds;
  }

  /**
   * Getter.
   * 
   * @return The travel time in minutes.
   */
  public int minutes() {
    return (seconds + BusTime.SECONDS_PER_MINUTE - 1) / BusTime.SECONDS_PER_MINUTE;
  }

  /**
   * Getter.
   * 
   * @return Whether the destination is not reachable.
   */
  public boolean isReachable() {
    return edges != null || from == to;
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
  public Collection<BusEdge> getEdges() {
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
    return startTime.later(0, seconds());
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('[');
    sb.append("\n  from=").append(from.getName()).append(",\n  to=").append(to.getName());

    if(isStartNode()) return sb.append("]").toString();
    if(!isReachable()) return sb.append(", state=NOT_REACHABLE]").toString();

    sb.append(",\n  steps=[\n    Start at ").append(from.getName());
    if(!from.equals(to)) {
      BusEdge prev = null;
      for(final BusEdge curr : edges) {
        if(prev == null || !prev.sameTour(curr)) {
          final int wait = (prev == null ? startTime : prev.getEnd()).minutesTo(curr.getStart());
          if(prev != null) {
            sb.append(prev.getTo().getName());
          }

          if(curr.getLine() == BusLine.WALK) {
            sb.append(",\n    walk ").append(
                BusTime.minutesToString(curr.travelSeconds() / 60)).append(" to ");
          } else {
            sb.append(",\n    wait for ").append(wait).append(" minutes, take bus line ").append(
                curr.getLine().getName()).append(" for ").append(
                    BusTime.minutesToString(curr.travelSeconds() / 60)).append(" to ");
          }
        }
        prev = curr;
      }
      sb.append(prev.getTo().getName());
    }
    return sb.append("\n  ],\n  time=").append(BusTime.minutesToString(seconds / 60)).append(
        "\n]").toString();
  }

}
