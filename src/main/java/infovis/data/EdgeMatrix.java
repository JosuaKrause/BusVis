package infovis.data;

import infovis.routing.RoutingResult;

import java.util.BitSet;
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
    private final BusStation lower;

    /**
     * The station with the higher id.
     */
    private final BusStation higher;

    /**
     * The lines traveling between the stations.
     */
    private final BusLine[] lines;

    /**
     * The highlighted lines.
     */
    private final BitSet highlighted;

    /**
     * The number of highlighted lines.
     */
    private int count;

    /**
     * Creates an undirected edge. The stations must be already sorted.
     * 
     * @param lower The source station.
     * @param higher The destination station.
     * @param lines The lines.
     */
    protected UndirectedEdge(final BusStation lower, final BusStation higher,
        final BusLine[] lines) {
      assert lower.getId() < higher.getId();
      this.lower = lower;
      this.higher = higher;
      this.lines = lines;
      highlighted = new BitSet();
      count = 0;
    }

    /**
     * Getter.
     * 
     * @return The lower id station.
     */
    public BusStation getLower() {
      return lower;
    }

    /**
     * Getter.
     * 
     * @return The higher id station.
     */
    public BusStation getHigher() {
      return higher;
    }

    /**
     * Returns the number in the drawing order of the given non-highlighted bus
     * line. That means the given line is drawn as <code>n</code>'th line when
     * <code>n</code> is the result of this function. Note that this number is
     * always smaller than the result of
     * {@link #getNumberOfHighlighted(BusLine)}.
     * 
     * @param line The non-highlighted line.
     * @return The number in the drawing order.
     */
    public synchronized int getNumberOfNonHighlighted(final BusLine line) {
      final BusLine[] lines = getNonHighlightedLines();
      for(int i = 0; i < lines.length; ++i) {
        if(line.equals(lines[i])) return i;
      }
      throw new IllegalStateException("line is highlighted");
    }

    /**
     * Returns the number in the drawing order of the given highlighted bus
     * line. That means the given line is drawn as <code>n</code>'th line when
     * <code>n</code> is the result of this function. Note that this number is
     * always greater than the result of
     * {@link #getNumberOfNonHighlighted(BusLine)}.
     * 
     * @param line The highlighted line.
     * @return The number in the drawing order.
     */
    public synchronized int getNumberOfHighlighted(final BusLine line) {
      final int off = lines.length - count; // number of non highlighted lines
      final BusLine[] lines = getHighlightedLines();
      for(int i = 0; i < lines.length; ++i) {
        if(line.equals(lines[i])) return off + i - walkingHighlighted();
      }
      throw new IllegalStateException("line not highlighted");
    }

    /**
     * Clears the highlights.
     */
    public synchronized void clearHighlighted() {
      highlighted.clear();
      count = 0;
      walkLight = 1;
    }

    /**
     * Adds an line to highlight.
     * 
     * @param hl The line to highlight.
     */
    public synchronized void addHighlighted(final BusLine hl) {
      if(BusLine.WALK.equals(hl)) {
        walkLight = 0;
      }
      for(int j = 0; j < lines.length; ++j) {
        if(hl.equals(lines[j])) {
          if(!highlighted.get(j)) {
            ++count;
          }
          highlighted.set(j);
          return;
        }
      }
      throw new IllegalStateException("line not found");
    }

    /**
     * Getter.
     * 
     * @return All lines in the correct order.
     */
    public synchronized BusLine[] getLines() {
      final int l = lines.length - count;
      final BusLine[] res = new BusLine[lines.length];
      System.arraycopy(getNonHighlightedLines(), 0, res, 0, l);
      System.arraycopy(getHighlightedLines(), 0, res, l, lines.length - l);
      return res;
    }

    /**
     * Getter.
     * 
     * @return The highlighted lines.
     */
    public synchronized BusLine[] getHighlightedLines() {
      final BusLine[] res = new BusLine[count];
      if(count == 0) return res;
      int p = 0;
      for(int i = highlighted.nextSetBit(0); i >= 0; i = highlighted.nextSetBit(i + 1)) {
        res[p++] = lines[i];
      }
      return res;
    }

    /**
     * Getter.
     * 
     * @return The non highlighted lines.
     */
    public synchronized BusLine[] getNonHighlightedLines() {
      final int l = lines.length - count;
      final BusLine[] res = new BusLine[l];
      if(l == 0) return res;
      int i = highlighted.nextClearBit(0);
      for(int p = 0; p < l; ++p) {
        res[p] = lines[i];
        i = highlighted.nextClearBit(i + 1);
      }
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

    /**
     * <code>0</code> when walking is highlighted and <code>1</code> otherwise.
     */
    private int walkLight;

    /**
     * Getter.
     * 
     * @return Returns <code>0</code> if walking is highlighted and
     *         <code>1</code> if not.
     */
    public int walkingHighlighted() {
      return walkLight;
    }

  }

  /**
   * The map for the maximal number of lines per station.
   */
  private final int[] maxLines;

  /**
   * The map for the degree of a station.
   */
  private final int[] degree;

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
   * @param bse The bus station enumerator.
   */
  public EdgeMatrix(final BusStationEnumerator bse) {
    maxId = bse.maxId();
    maxLines = new int[maxId + 1];
    degree = new int[maxId + 1];
    matrix = new UndirectedEdge[maxId][];
    for(int i = 1; i <= maxId; ++i) {
      final BusStation higher = bse.getForId(i);
      final UndirectedEdge[] tmp = matrix[i - 1] = new UndirectedEdge[i];
      for(int j = 0; j < i; ++j) {
        final BusStation lower = bse.getForId(j);
        final BusLine[] lines = calcLines(lower, higher);
        updateLinesAndDegree(lower.getId(), higher.getId(), lines.length);
        tmp[j] = new UndirectedEdge(lower, higher, lines);
      }
    }
  }

  /**
   * Updates the degree and the maximum lines of the given bus stations. This
   * method should only be called during initialization.
   * 
   * @param a One station id.
   * @param b Another station id.
   * @param numLines The number of lines between them.
   */
  private void updateLinesAndDegree(final int a, final int b, final int numLines) {
    if(numLines > 1) {
      ++degree[a];
      ++degree[b];
    }
    if(maxLines[a] < numLines) {
      maxLines[a] = numLines;
    }
    if(maxLines[b] < numLines) {
      maxLines[b] = numLines;
    }
  }

  /**
   * Calculates the lines for a pair of {@link BusStation}s.
   * 
   * @param a One station.
   * @param b Another station.
   * @return The lines connecting the stations.
   */
  private static BusLine[] calcLines(final BusStation a, final BusStation b) {
    final Set<BusLine> set = new HashSet<BusLine>();
    set.add(BusLine.WALK);
    for(final BusEdge e : a.getEdges()) {
      if(e.getTo().equals(b)) {
        set.add(e.getLine());
      }
    }
    for(final BusEdge e : b.getEdges()) {
      if(e.getTo().equals(a)) {
        set.add(e.getLine());
      }
    }
    return set.toArray(new BusLine[set.size()]);
  }

  /**
   * Getter.
   * 
   * @param a A station.
   * @param b Another station.
   * @return The undirected edge between those or <code>null</code> if there is
   *         no line.
   */
  public UndirectedEdge getFor(final BusStation a, final BusStation b) {
    return getFor(a.getId(), b.getId());
  }

  /**
   * Getter.
   * 
   * @param a A station id.
   * @param b Another station id.
   * @return The undirected edge between those or <code>null</code> if there is
   *         no line.
   */
  private UndirectedEdge getFor(final int a, final int b) {
    if(a == b) return null;
    if(a > b) return getFor(b, a);
    return matrix[b - 1][a];
  }

  /**
   * Refreshes the highlights according to the given routes.
   * 
   * @param routes The routes.
   */
  public synchronized void refreshHighlights(final RoutingResult[] routes) {
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

  /**
   * Getter.
   * 
   * @param station A station.
   * @return All undirected edges of a station (only to stations with a lower
   *         id).
   */
  public Iterable<UndirectedEdge> getEdgesFor(final BusStation station) {
    return getEdgesFor(station.getId());
  }

  /**
   * Getter.
   * 
   * @param id A station id.
   * @return All undirected edges of a station (only to stations with a lower
   *         id).
   */
  private Iterable<UndirectedEdge> getEdgesFor(final int id) {
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

  /**
   * Getter.
   * 
   * @param station The station.
   * @return The maximum number of lines for this station.
   */
  public int getMaxLines(final BusStation station) {
    return maxLines[station.getId()];
  }

  /**
   * Getter.
   * 
   * @param station The station.
   * @return The degree of this station.
   */
  public int getDegree(final BusStation station) {
    return degree[station.getId()];
  }

}
