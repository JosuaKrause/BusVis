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
   * A shape defining the area, where a click is associated with the given node.
   * The position of the node must be added manually.
   * 
   * @param n The node.
   * @return The clickable shape of the node.
   */
  Shape nodeClickArea(SpringNode n);

  /**
   * Is called when the user clicks on the given node.
   * 
   * @param n The node that was clicked on.
   */
  void clickedAt(SpringNode n);

}
