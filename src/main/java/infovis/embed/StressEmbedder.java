package infovis.embed;

import static infovis.util.VecUtil.*;
import infovis.embed.Weighter.WeightedEdge;
import infovis.util.ArrayUtil;

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
public class StressEmbedder extends DirectEmbedder {
  /** Weight of the whole route's length. */
  private static final double ROUTE_WEIGHT = 0.01;
  /** Weight of each edges length. */
  private static final double EDGE_WEIGHT = 1;
  /** Number of iterations of the majorization algorithm. */
  private static final int ITERATIONS = 75;

  /** Default positions. */
  private final Points defaults;

  /** The positions of the nodes. */
  private final Point2D[] positions;

  /**
   * Creates a stress based positioner.
   * 
   * @param weighter The weighter.
   * @param drawer The drawer.
   */
  public StressEmbedder(final Weighter weighter, final NodeDrawer drawer) {
    super(weighter, drawer);

    final List<SpringNode> nodes = weighter.nodes();
    final int n = nodes.size();
    positions = new Point2D[n];
    defaults = new Points(n);
    for(final SpringNode node : nodes) {
      defaults.setPoint(node.getId(), weighter.getDefaultPosition(node));
    }
  }

  @Override
  protected void changedWeights(final Collection<SpringNode> nodes, final SpringNode ref,
      final Point2D refP, final Point2D diff) {
    Arrays.fill(positions, null);

    final Map<NodeDyad, NodeDyad> dyads = new HashMap<NodeDyad, NodeDyad>();
    for(final SpringNode n: nodes) {
      for(final WeightedEdge e : weighter.edgesTo(n)) {
        final NodeDyad dyad = new NodeDyad(e.from, e.to);
        final NodeDyad old = dyads.get(dyad), nw = old == null ? dyad : old;
        if(nw != old) {
          dyads.put(nw, nw);
        }
        nw.addEdge(e);
      }
    }

    final int n = nodes.size();
    final double[][] dists = ArrayUtil.fill(new double[n][n], -1), weights = new double[n][n];

    for(final NodeDyad d : dyads.values()) {
      final int a = d.a.getId(), b = d.b.getId();
      dists[a][b] = dists[b][a] = d.getAccumWeight();
      weights[a][b] = weights[b][a] = EDGE_WEIGHT;
    }

    final int refID = ref.getId();
    for(final SpringNode a : nodes) {
      if(weighter.hasWeight(a, ref)) {
        final double weight = weighter.weight(a, ref);
        if(weight > 0) {
          final int id = a.getId();
          dists[id][refID] = dists[refID][id] = weight;
          weights[id][refID] = weights[refID][id] = ROUTE_WEIGHT;
        }
      }
    }

    final Points pos = defaults.copy();
    for(final SpringNode nd : nodes) {
      // initialize the majorization with the positions from the radial layout
      final int id = nd.getId();
      final Point2D p = pos.getPoint(id);
      final double w = weighter.weight(nd, ref);
      final Point2D init = addVec(setLength(subVec(addVec(p, diff), refP), w), refP);
      if(!(Double.isNaN(init.getX()) || Double.isNaN(init.getY()))) {
        pos.setPoint(id, init);
      }
    }
    pos.majorize(refID, dists, weights, ITERATIONS);

    final Point2D offset = subVec(pos.getPoint(refID), refP);
    for(final SpringNode node : nodes) {
      final int id = node.getId();
      positions[id] = subVec(pos.getPoint(id), offset);
    }
  }

  @Override
  public boolean drawCircles() {
    return true;
  }

  @Override
  protected Point2D getDestination(final SpringNode n, final Point2D pos,
      final SpringNode ref, final Point2D refP, final Point2D diff) {
    return positions[n.getId()];
  }

  /**
   * Unordered pair of {@link SpringNode}s.
   * 
   * @author Leo Woerteler
   */
  private static class NodeDyad {
    /** First node. */
    final SpringNode a;
    /** Second node. */
    final SpringNode b;
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
    NodeDyad(final SpringNode a, final SpringNode b) {
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
  }

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
    Point2D.Double getPoint(final int pos) {
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
      final double[] tempRow = ds[a];
      ds[a] = ds[b];
      ds[b] = tempRow;
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
      final double x = positions[0][a], y = positions[1][a];
      positions[0][a] = positions[0][b];
      positions[1][a] = positions[1][b];
      positions[0][b] = x;
      positions[1][b] = y;
    }

    /**
     * Creates a copy of these points.
     * 
     * @return deep-copy of this object
     */
    Points copy() {
      final Points copy = new Points(positions[0].length);
      for(int i = 0; i < positions.length; i++) {
        copy.positions[i] = positions[i].clone();
      }
      return copy;
    }
  }
}
