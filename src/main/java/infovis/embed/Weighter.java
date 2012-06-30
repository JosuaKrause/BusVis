package infovis.embed;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.List;

/**
 * Defines weights between {@link SpringNode}s in a spring embedder system.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface Weighter {

  /**
   * The weight of the directed edge.
   * 
   * @param from The source of the edge.
   * @param to The target of the edge.
   * @return The weight of the edge. Negative weights push nodes apart until the
   *         given distance is reached.
   */
  double weight(SpringNode from, SpringNode to);

  /**
   * Whether there is a directed edge between the given nodes.
   * 
   * @param from The source of the edge.
   * @param to The target of the edge.
   * @return Whether there is an edge.
   */
  boolean hasWeight(SpringNode from, SpringNode to);

  /**
   * A list of all nodes in the spring embedder system.
   * 
   * @return The list.
   */
  Collection<SpringNode> nodes();

  /**
   * Getter.
   * 
   * @return The constant of the springs.
   */
  double springConstant();

  /**
   * Getter.
   * 
   * @param node The node.
   * @return The default position of the node.
   */
  Point2D getDefaultPosition(SpringNode node);

  /**
   * Getter.
   * 
   * @return The reference node or <code>null</code> if it is not assigned.
   */
  SpringNode getReferenceNode();

  /**
   * This method returns how the weights have changed. The change status is
   * cleared after this call.
   * 
   * @return How the weights have changed. The result is a combination of change
   *         types.
   */
  int changes();

  /**
   * Signals no change.
   */
  int NO_CHANGE = 0;

  /**
   * Signals a normal change.
   */
  int NORMAL_CHANGE = 1;

  /**
   * Signals that a fast animation should be used.
   */
  int FAST_ANIMATION_CHANGE = 2;

  /**
   * Getter.
   * 
   * @return Whether this weighter is in animation.
   */
  boolean inAnimation();

  /**
   * A weighted edge.
   * 
   * @author Joschi <josua.krause@googlemail.com>
   */
  final class WeightedEdge {
    /**
     * The start node.
     */
    public final SpringNode from;

    /**
     * The end node.
     */
    public final SpringNode to;

    /**
     * The weight of the edge.
     */
    public final double weight;

    /**
     * Creates a weighted edge.
     * 
     * @param from The start node.
     * @param to The destination node.
     * @param weight The weight.
     */
    public WeightedEdge(final SpringNode from, final SpringNode to, final double weight) {
      this.from = from;
      this.to = to;
      this.weight = weight;
    }
  }

  /**
   * The edges leading from the reference node to the given node.
   * 
   * @param to The destination.
   * @return The path from the reference node to the given node.
   */
  List<WeightedEdge> edgesTo(SpringNode to);

  /**
   * Sets the animator to be notified when the weights change.
   * 
   * @param animator The animator.
   */
  void setAnimator(Animator animator);

}
