package infovis.embed;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;

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
   * Draws the edges of the given node. The graphics device must be translated
   * manually.
   * 
   * @param g The device.
   * @param n The node.
   */
  void drawEdges(Graphics2D g, SpringNode n);

  /**
   * Draws the label of a given node.
   * 
   * @param g The device.
   * @param n The node.
   */
  void drawLabel(Graphics2D g, SpringNode n);

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
   * @param real Whether the real position or the position after the complete
   *          animation should be returned.
   * @return The clickable shape of the node.
   */
  Shape nodeClickArea(SpringNode n, boolean real);

  /**
   * Defines the tool-tip text for the given node.
   * 
   * @param n The node.
   * @return The tool-tip text or <code>null</code> if none is needed.
   */
  String getTooltipText(SpringNode n);

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
   * A click on the node occured.
   * 
   * @param n The node.
   */
  void selectNode(SpringNode n);

  /**
   * An iteration over all nodes in the spring embedder system.
   * 
   * @return The iterable.
   */
  Iterable<SpringNode> nodes();

  /**
   * Is called when the user moves the mouse.
   * 
   * @param cur The current mouse position.
   */
  void moveMouse(Point2D cur);

}
