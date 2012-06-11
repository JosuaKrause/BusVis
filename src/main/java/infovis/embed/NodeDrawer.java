package infovis.embed;

import java.awt.Graphics2D;
import java.awt.Shape;

/**
 * Draws spring nodes and interacts with them.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface NodeDrawer {

  /**
   * Draws the given node. The graphics device must be translated manually.
   * 
   * @param g The device.
   * @param n The node.
   */
  void drawNode(Graphics2D g, SpringNode n);

  /**
   * Draws the background.
   * 
   * @param g The graphics context.
   */
  void drawBackground(Graphics2D g);

  /**
   * A shape defining the area, where a click is associated with the given node.
   * The position of the node must be added manually.
   * 
   * @param n The node.
   * @return The clickable shape of the node.
   */
  Shape nodeClickArea(SpringNode n);

  /**
   * Drags a single node.
   * 
   * @param n The node.
   * @param startX The original starting x position.
   * @param startY The original starting y position.
   * @param dx The x difference to the original starting point.
   * @param dy The y difference to the original starting point.
   */
  void dragNode(SpringNode n, double startX, double startY, double dx, double dy);

  /**
   * An iteration over all nodes in the spring embedder system.
   * 
   * @return The iterable.
   */
  Iterable<SpringNode> nodes();

}
