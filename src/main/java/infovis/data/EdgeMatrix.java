package infovis.data;

import infovis.routing.RoutingResult;

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * The edge matrix gives an undirected representation of the bus network.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class EdgeMatrix {

  /**
   * Represents an undirected edge between two stations.
   * 
   * @author Joschi <josua.krause@googlemail.com>
   */
  public static final class UndirectedEdge {

    /**
     * The station with the lower id.
     */
    private final BusStation from;

    /**
     * The station with the higher id.
     */
    private final BusStation to;

    /**
     * The lines travelling between the stations.
     */
    private final BusLine[] lines;

    /**
     * The highlighted lines.
     */
    private final BitSet highlighted;

    /**
     * The highest highlighted line.
     */
    private int highest;

    /**
     * The number of highlighted lines.
     */
    private int count;

    /**
     * Creates an undirected edge. The stations must be already sorted.
     * 
     * @param from The source station.
     * @param to The destination station.
     * @param lines The lines.
     */
    protected UndirectedEdge(final BusStation from, final BusStation to,
        final BusLine[] lines) {
      assert from.getId() < to.getId();
      this.from = from;
      this.to = to;
      this.lines = lines;
      highlighted = new BitSet();
      highest = -1;
      count = 0;
    }

    /**
     * Getter.
     * 
     * @return The lower id station.
     */
    public BusStation getFrom() {
      return from;
    }

    /**
     * Getter.
     * 
     * @return The higher id station.
     */
    public BusStation getTo() {
      return to;
    }

    /**
     * Clears the highlights.
     */
    public void clearHighlighted() {
      highlighted.clear();
      highest = -1;
      count = 0;
    }

    /**
     * Adds an line to highlight.
     * 
     * @param hl The line to highlight.
     */
    public void addHighlighted(final BusLine hl) {
      for(int j = 0; j < lines.length; ++j) {
        if(hl.equals(lines[j])) {
          if(!highlighted.get(j)) {
            ++count;
          }
          highlighted.set(j);
          if(j > highest) {
            highest = j;
          }
          return;
        }
      }
      throw new IllegalStateException("line not found");
    }

    /**
     * Getter.
     * 
     * @return The highlighted lines.
     */
    public BusLine[] getHighlightedLines() {
      final BusLine[] res = new BusLine[count];
      if(count == 0) return res;
      int p = 0;
      int i = 0;
      for(;;) {
        if(p == 0) {
          if(highlighted.get(0)) {
            res[i++] = lines[0];
          }
        } else {
          res[i++] = lines[p];
        }
        if(p >= highest) {
          break;
        }
        p = highlighted.nextSetBit(p);
      }
      return res;
    }

    /**
     * Getter.
     * 
     * @return The non highlighted lines.
     */
    public BusLine[] getNonHighlightedLines() {
      final int l = lines.length - count;
      final BusLine[] res = new BusLine[l];
      if(l == 0) return res;
      int p = 0;
      for(int i = 0; i < lines.length; ++i) {
        if(!highlighted.get(i)) {
          res[p++] = lines[i];
        }
      }
      if(p < l) throw new IllegalStateException("some lines not matched (got "
          + p + " expected " + l + ") lines: " + lines.length);
      return res;
    }

    /**
     * Getter.
     * 
     * @return The number of lines connecting the two stations.
     */
    public int getLineDegree() {
      return lines.length;
    }

  }

  /**
   * The highest bus station id.
   */
  private final int maxId;

  /**
   * The matrix.
   */
  protected final UndirectedEdge[][] matrix;

  /**
   * Creates a matrix for the given manager.
   * 
   * @param mng The manager.
   */
  public EdgeMatrix(final BusStationManager mng) {
    int max = 0;
    for(final BusStation bs : mng.getStations()) {
      final int id = bs.getId();
      if(id > max) {
        max = id;
      }
    }
    maxId = max;
    matrix = new UndirectedEdge[max][];
    for(int i = 1; i <= max; ++i) {
      final BusStation to = mng.getForId(i);
      if(to == null) {
        continue;
      }
      final UndirectedEdge[] tmp = matrix[i - 1] = new UndirectedEdge[i];
      for(int j = 0; j < i; ++j) {
        final BusStation from = mng.getForId(j);
        if(from == null) {
          continue;
        }
        final BusLine[] lines = calcLines(from, to);
        if(lines.length > 0) {
          tmp[j] = new UndirectedEdge(from, to, lines);
        }
      }
    }
  }

  private BusLine[] calcLines(final BusStation from, final BusStation to) {
    final Set<BusLine> set = new HashSet<BusLine>();
    for(final BusEdge e : from.getEdges()) {
      if(e.getTo().equals(to)) {
        set.add(e.getLine());
      }
    }
    for(final BusEdge e : to.getEdges()) {
      if(e.getTo().equals(from)) {
        set.add(e.getLine());
      }
    }
    return set.toArray(new BusLine[set.size()]);
  }

  public UndirectedEdge getFor(final BusStation a, final BusStation b) {
    return getFor(a.getId(), b.getId());
  }

  public UndirectedEdge getFor(final int a, final int b) {
    if(a == b) return null;
    if(a > b) return getFor(b, a);
    return matrix[b - 1][a];
  }

  public void refreshHighlights(final Collection<RoutingResult> routes) {
    for(int i = 1; i <= maxId; ++i) {
      for(int j = 0; j < i; ++j) {
        final UndirectedEdge e = getFor(j, i);
        if(e == null) {
          continue;
        }
        e.clearHighlighted();
      }
    }
    for(final RoutingResult r: routes) {
      final Iterable<BusEdge> edges = r.getEdges();
      if(edges == null) {
        continue;
      }
      for(final BusEdge bd : edges) {
        final UndirectedEdge ue = getFor(bd.getFrom(), bd.getTo());
        ue.addHighlighted(bd.getLine());
      }
    }
  }

  public Iterable<UndirectedEdge> getEdgesFor(final BusStation station) {
    return getEdgesFor(station.getId());
  }

  public Iterable<UndirectedEdge> getEdgesFor(final int id) {
    if(id == 0) return Collections.EMPTY_LIST;
    return new Iterable<UndirectedEdge>() {

      @Override
      public Iterator<UndirectedEdge> iterator() {
        return new Iterator<UndirectedEdge>() {

          private final UndirectedEdge[] row = matrix[id - 1];

          private int cur;

          private UndirectedEdge next;

          {
            fetchNext();
          }

          private void fetchNext() {
            while(cur < row.length) {
              final UndirectedEdge c = row[cur++];
              if(c != null) {
                next = c;
                return;
              }
            }
            next = null;
          }

          @Override
          public boolean hasNext() {
            return next != null;
          }

          @Override
          public UndirectedEdge next() {
            final UndirectedEdge res = next;
            fetchNext();
            return res;
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }

        };
      }

    };
  }

}
