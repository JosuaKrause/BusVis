package infovis.embed;

import java.awt.geom.Point2D;

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
   * An iteration over all nodes in the spring embedder system.
   * 
   * @return The iterable.
   */
  Iterable<SpringNode> nodes();

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

}
