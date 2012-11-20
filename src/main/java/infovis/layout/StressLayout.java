package infovis.layout;

import static infovis.util.VecUtil.*;
import infovis.busvis.LayoutNode;
import infovis.busvis.NodeDrawer;
import infovis.busvis.Weighter;
import infovis.busvis.Weighter.WeightedEdge;
import infovis.draw.BackgroundRealizer;
import infovis.util.ArrayUtil;
import infovis.util.VecUtil;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mdsj.StressMinimization;

/**
 * A stress based positioner technique.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class StressLayout extends DirectLayouter {

  /** The smallest distance a node must move to keep iterating. */
  private static final double EPS = 0.1;

  /** The smallest distance a node must move to keep iterating squared. */
  private static final double EPS_SQ = EPS * EPS;

  /** Weight of the whole route's length. */
  private static final double ROUTE_WEIGHT = 0.01;

  /** Weight of each edges length. */
  private static final double EDGE_WEIGHT = 1;

  /** Number of iterations of the majorization algorithm at start. */
  private static final int ITERATIONS_START = 75;

  /** Number of iterations of the majorization algorithm at iteration. */
  private static final int ITERATIONS_ITER = 10;

  /** Default positions. */
  private final Points defaults;

  /** The distance matrix. */
  private final double[][] dists;

  /** The weight matrix. */
  private final double[][] weights;

  /** The positions of the nodes on time A. */
  private final Point2D[] positionsA;

  /** The positions of the nodes on time B. */
  private final Point2D[] positionsB;

  /** Which positions to take. */
  private boolean takeA;

  /** The points. */
  private Points pos;

  /** The reference node id. */
  private int refID;

  /**
   * Creates a stress based positioner.
   * 
   * @param weighter The weighter.
   * @param drawer The drawer.
   */
  public StressLayout(final Weighter weighter, final NodeDrawer drawer) {
    super(weighter, drawer);

    final List<LayoutNode> nodes = weighter.nodes();
    final int n = nodes.size();
    positionsA = new Point2D[n];
    positionsB = new Point2D[n];
    defaults = new Points(n);
    for(final LayoutNode node : nodes) {
      defaults.setPoint(node.getId(), weighter.getDefaultPosition(node));
    }
    dists = ArrayUtil.fill(new double[n][n], -1d);
    weights = new double[n][n];
  }

  @Override
  protected void changedWeights(final Collection<LayoutNode> nodes,
      final Collection<LayoutNode> relevant, final LayoutNode ref,
      final Point2D refP, final Point2D diff) {
    Arrays.fill(getDestinationForWrite(), null);
    ArrayUtil.fill(dists, -1);
    ArrayUtil.fill(weights, 0);
    refID = ref.getId();

    final Map<NodeDyad, NodeDyad> dyads = new HashMap<NodeDyad, NodeDyad>();
    for(final LayoutNode n : nodes) {
      for(final WeightedEdge e : weighter.edgesTo(n)) {
        final NodeDyad dyad = new NodeDyad(e.from, e.to);
        final NodeDyad old = dyads.get(dyad);
        final NodeDyad nw = old == null ? dyad : old;
        if(nw != old) {
          dyads.put(nw, nw);
        }
        nw.addEdge(e);
      }
    }

    for(final NodeDyad d : dyads.values()) {
      final int a = d.a.getId();
      final int b = d.b.getId();
      dists[a][b] = dists[b][a] = d.getAccumWeight();
      weights[a][b] = weights[b][a] = EDGE_WEIGHT;
    }

    for(final LayoutNode a : nodes) {
      if(weighter.hasWeight(a, ref)) {
        final double weight = weighter.weight(a, ref);
        if(weight > 0) {
          final int id = a.getId();
          dists[id][refID] = dists[refID][id] = weight;
          weights[id][refID] = weights[refID][id] = ROUTE_WEIGHT;
        }
      }
    }

    relevant.addAll(nodes);
    refine(relevant, refP, true);
  }

  /**
   * Refines the set of nodes.
   * 
   * @param relevant The relevant nodes.
   * @param refP The reference point.
   * @param first Whether this is the first iteration.
   * @return Whether to on keep iterating.
   */
  private boolean refine(final Collection<LayoutNode> relevant,
      final Point2D refP, final boolean first) {
    // TODO maybe reactivate later
    if(!first) return false;
    final Point2D[] read = getDestinationForRead();
    pos = new Points(defaults);
    for(final LayoutNode nd : relevant) {
      // initialize the majorization with the original positions
      final int id = nd.getId();
      final Point2D init = first ? weighter.getDefaultPosition(nd) : read[id];
      if(!(Double.isNaN(init.getX()) || Double.isNaN(init.getY()))) {
        pos.setPoint(id, init);
      }
    }

    pos.majorize(refID, dists, weights, first ? ITERATIONS_START : ITERATIONS_ITER);

    final Point2D[] write = getDestinationForWrite();
    final Point2D offset = subVec(refP, pos.getPoint(refID));
    for(final LayoutNode node : relevant) {
      final int id = node.getId();
      write[id] = addVec(pos.getPoint(id), offset);
    }
    swap();
    if(first) return true;
    for(int i = 0; i < read.length; ++i) {
      final Point2D a = read[i];
      final Point2D b = write[i];
      if(VecUtil.getLengthSq(VecUtil.subVec(a, b)) > EPS_SQ) return true;
    }
    return false;
  }

  @Override
  protected boolean refinePositions(
      final Collection<LayoutNode> relevant, final Point2D refP) {
    return refine(relevant, refP, false);
  }

  /** Swaps the active with the inactive positions. */
  private void swap() {
    takeA = !takeA;
  }

  /**
   * Getter.
   * 
   * @return The active positions.
   */
  private Point2D[] getDestinationForRead() {
    return takeA ? positionsA : positionsB;
  }

  /**
   * Getter.
   * 
   * @return The inactive positions.
   */
  private Point2D[] getDestinationForWrite() {
    // write in other
    return takeA ? positionsB : positionsA;
  }

  @Override
  protected Point2D getDestination(final LayoutNode n) {
    return getDestinationForRead()[n.getId()];
  }

  @Override
  protected BackgroundRealizer backgroundRealizer() {
    return BackgroundRealizer.NO_BG;
  }

  /**
   * Unordered pair of {@link LayoutNode}s.
   * 
   * @author Leo Woerteler
   */
  private static class NodeDyad {

    /** First node. */
    final LayoutNode a;
    /** Second node. */
    final LayoutNode b;
    /** Number of accumulated distances. */
    int count;
    /** Sum of accumulated distances. */
    double sum;

    /**
     * Constructor.
     * 
     * @param a first ode
     * @param b second node
     */
    NodeDyad(final LayoutNode a, final LayoutNode b) {
      if(a.getId() < b.getId()) {
        this.a = a;
        this.b = b;
      } else {
        this.a = b;
        this.b = a;
      }
    }

    /**
     * Adds a distance to this dyad.
     * 
     * @param edge edge
     */
    void addEdge(final WeightedEdge edge) {
      count++;
      sum += edge.weight;
    }

    /**
     * Accumulated weight of this node dyad.
     * 
     * @return accumulated weight
     */
    double getAccumWeight() {
      return sum / count;
    }

    @Override
    public boolean equals(final Object obj) {
      if(!(obj instanceof NodeDyad)) return false;
      final NodeDyad other = (NodeDyad) obj;
      return a.equals(other.a) && b.equals(other.b);
    }

    @Override
    public int hashCode() {
      return 31 * a.hashCode() + b.hashCode();
    }

  } // NodeDyad

  /**
   * A collection of points.
   * 
   * @author Leo Woerteler
   */
  private static class Points {

    /** Positions of the stored points. */
    private final double[][] positions;

    /**
     * Constructor taking the number of points.
     * 
     * @param n number of points
     */
    Points(final int n) {
      positions = new double[2][n];
    }

    /**
     * Creates a copy of the given points.
     * 
     * @param p The points to copy.
     */
    Points(final Points p) {
      positions = ArrayUtil.copy(p.positions);
    }

    /**
     * Sets the point at the given position.
     * 
     * @param pos position
     * @param point point
     */
    void setPoint(final int pos, final Point2D point) {
      positions[0][pos] = point.getX();
      positions[1][pos] = point.getY();
    }

    /**
     * Gets the point at the given position.
     * 
     * @param pos position
     * @return point
     */
    Point2D getPoint(final int pos) {
      return new Point2D.Double(positions[0][pos], positions[1][pos]);
    }

    /**
     * Optimizes the layout of the given points according to the given distances
     * and weights.
     * 
     * @param mid centered point
     * @param dists distances
     * @param weights weights
     * @param iter number of iterations
     */
    void majorize(final int mid, final double[][] dists, final double[][] weights,
        final int iter) {
      final double[][] ds = ArrayUtil.copy(dists), ws = ArrayUtil.copy(weights);

      swapPoints(0, mid);
      swap2(ds, 0, mid);
      swap2(ws, 0, mid);
      StressMinimization.majorize(positions, ds, ws, iter);
      swapPoints(0, mid);
    }

    /**
     * Swaps two lines and columns in a symmetric matrix.
     * 
     * @param ds matrix
     * @param a first index
     * @param b second index
     */
    private static void swap2(final double[][] ds, final int a, final int b) {
      ArrayUtil.swap(ds, a, b);
      for(int i = 0; i < ds.length; i++) {
        ArrayUtil.swap(ds[i], a, b);
      }
    }

    /**
     * Swaps two points in this collection.
     * 
     * @param a first point's index
     * @param b second point's index
     */
    private void swapPoints(final int a, final int b) {
      ArrayUtil.swap(positions[0], a, b);
      ArrayUtil.swap(positions[1], a, b);
    }

  } // Points

}
