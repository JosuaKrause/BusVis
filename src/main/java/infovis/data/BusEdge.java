package infovis.data;

import java.util.Comparator;

/**
 * An edge for a bus to drive. It has a starting point and a destination. As
 * well as a starting and arrival time.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class BusEdge implements Comparable<BusEdge> {

  /** Bus line. */
  private final BusLine line;

  /** Tour number, unique per line. */
  private final int tourNr;

  /** The departure time. */
  private final BusTime start;

  /** The arriving time. */
  private final BusTime end;

  /** The starting bus station. */
  private final BusStation from;

  /** The destination. */
  private final BusStation to;

  /**
   * Creates a new edge.
   * 
   * @param line bus line
   * @param tourNr tour number, unique per line
   * @param from The starting point.
   * @param to The destination.
   * @param start The departure time.
   * @param end The arrival time.
   */
  public BusEdge(final BusLine line, final int tourNr, final BusStation from,
      final BusStation to,
      final BusTime start,
      final BusTime end) {
    this.line = line;
    this.tourNr = tourNr;
    this.start = start;
    this.end = end;
    this.from = from;
    this.to = to;
  }

  /**
   * Getter.
   * 
   * @return bus line
   */
  public BusLine getLine() {
    return line;
  }

  /**
   * Getter.
   * 
   * @return The departure time.
   */
  public BusTime getStart() {
    return start;
  }

  /**
   * Getter.
   * 
   * @return The arrival time.
   */
  public BusTime getEnd() {
    return end;
  }

  /**
   * Getter.
   * 
   * @return The starting station.
   */
  public BusStation getFrom() {
    return from;
  }

  /**
   * Getter.
   * 
   * @return The destination.
   */
  public BusStation getTo() {
    return to;
  }

  /**
   * Getter.
   * 
   * @return the tour number
   */
  public int getTourNr() {
    return tourNr;
  }

  @Override
  public int compareTo(final BusEdge o) {
    final int cmp = start.compareTo(o.start);
    return cmp == 0 ? end.compareTo(o.end) : cmp;
  }

  /**
   * Duration of this edge in minutes.
   * 
   * @return time needed from start to end
   */
  public int travelMinutes() {
    return start.minutesTo(end);
  }

  /**
   * Creates a comparator that assumes the reference time as lowest possible
   * value. The edges are primarily sorted by the end time.
   * 
   * @param time The time.
   * @return The comparator.
   */
  public static Comparator<BusEdge> createRelativeComparator(final BusTime time) {
    final Comparator<BusTime> cmp = time.createRelativeComparator();
    return new Comparator<BusEdge>() {

      @Override
      public int compare(final BusEdge o1, final BusEdge o2) {
        final int c = cmp.compare(o1.getEnd(), o2.getEnd());
        return c == 0 ? cmp.compare(o1.getStart(), o2.getStart()) : c;
      }

    };
  }

  @Override
  public String toString() {
    return String.format("%s[%s, from=%s, to=%s, %s, %s]", getClass().getSimpleName(),
        line.getName(), from.getName(), to.getName(), start, end);
  }

  /**
   * Checks if this edge is on the same tour of the same line as the given edge.
   * 
   * @param other edge
   * @return <code>true</code> if the edges are an the same tour,
   *         <code>false</code> otherwise
   */
  public boolean sameTour(final BusEdge other) {
    return line.equals(other.line) && tourNr == other.tourNr;
  }
}
