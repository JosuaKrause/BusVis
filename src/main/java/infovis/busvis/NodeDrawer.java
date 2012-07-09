package infovis.busvis;

import infovis.data.BusLine;
import infovis.draw.BackgroundRealizer;
import infovis.gui.Context;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.BitSet;
import java.util.Collection;
import java.util.Set;

/**
 * Draws layouted nodes and interacts with them.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface NodeDrawer {

  /**
   * Draws the given node. The graphics device must be translated manually.
   * 
   * @param g The device.
   * @param ctx The canvas context.
   * @param n The node.
   * @param secondarySelected If the node is secondary selected.
   */
  void drawNode(Graphics2D g, Context ctx, LayoutNode n, boolean secondarySelected);

  /**
   * Draws the given node. The graphics device must be translated manually.
   * 
   * @param g The device.
   * @param ctx The canvas context.
   * @param n The secondary selected node.
   */
  void drawSecondarySelected(Graphics2D g, Context ctx, LayoutNode n);

  /**
   * Draws the edges of the given node. The graphics device must be translated
   * manually.
   * 
   * @param g The device.
   * @param ctx The canvas context.
   * @param n The node.
   * @param visibleLines Accumulates all visible lines.
   * @param secondarySelection If a secondary selection is made, this value is
   *          <code>true</code>.
   */
  void drawEdges(Graphics2D g, Context ctx, LayoutNode n,
      Set<BusLine> visibleLines, boolean secondarySelection);

  /**
   * Draws the label of a given node. Note that the coordinates are in component
   * coordinates.
   * 
   * @param g The device.
   * @param ctx The canvas context.
   * @param n The node.
   * @param hovered If the node is hovered.
   * @param addText Text to add to the label. If this value is not
   *          <code>null</code> the label will be drawn.
   */
  void drawLabel(Graphics2D g, Context ctx, LayoutNode n, boolean hovered, String addText);

  /**
   * Draws the background.
   * 
   * @param g The graphics context.
   * @param ctx The canvas context.
   * @param background How to draw the background.
   */
  void drawBackground(Graphics2D g, Context ctx, BackgroundRealizer background);

  /**
   * Draws the labels of a route.
   * 
   * @param g The graphics context.
   * @param ctx The canvas context.
   * @param n The destination.
   * @param visited The method sets all nodes that are visited.
   */
  void drawRouteLabels(Graphics2D g, Context ctx, LayoutNode n, BitSet visited);

  /**
   * Draws the legend.
   * 
   * @param g The graphics context.
   * @param ctx The canvas context.
   * @param visibleLines The set of visible bus lines.
   */
  void drawLegend(Graphics2D g, Context ctx, Set<BusLine> visibleLines);

  /**
   * A shape defining the area, where a click is associated with the given node.
   * 
   * @param n The node.
   * @param real Whether the real position or the position after the complete
   *          animation should be returned.
   * @return The clickable shape of the node.
   */
  Shape nodeClickArea(LayoutNode n, boolean real);

  /**
   * The radius of the node.
   * 
   * @param n The node.
   * @return The radius.
   */
  double nodeRadius(LayoutNode n);

  /**
   * Drags a single node.
   * 
   * @param n The node.
   * @param startX The original starting x position.
   * @param startY The original starting y position.
   * @param dx The x difference to the original starting point.
   * @param dy The y difference to the original starting point.
   */
  void dragNode(LayoutNode n, double startX, double startY, double dx, double dy);

  /**
   * A click on the node occured.
   * 
   * @param n The node.
   */
  void selectNode(LayoutNode n);

  /**
   * A secondary selection occured.
   * 
   * @param nodes The node that are clicked on.
   */
  void secondarySelection(Collection<LayoutNode> nodes);

  /**
   * Getter.
   * 
   * @return Whether any node is secondary selected.
   */
  boolean hasSecondarySelection();

  /**
   * Getter.
   * 
   * @return A list of all secondary selected nodes.
   */
  Collection<LayoutNode> secondarySelected();

  /**
   * Getter.
   * 
   * @param node The node.
   * @return If the node is secondary selected.
   */
  boolean isSecondarySelected(LayoutNode node);

  /**
   * An iteration over all nodes in the layout.
   * 
   * @return The iterable.
   */
  Iterable<LayoutNode> nodes();

  /**
   * Getter.
   * 
   * @param i The id of the node.
   * @return The node.
   */
  LayoutNode getNode(int i);

  /**
   * Is called when the user moves the mouse.
   * 
   * @param cur The current mouse position.
   */
  void moveMouse(Point2D cur);

  /**
   * Calculates the bounding box of the paint area. This method may return
   * <code>null</code> to indicate that the bounding box is not important.
   * 
   * @param background The background.
   * @return The bounding box or <code>null</code>.
   */
  Rectangle2D getBoundingBox(BackgroundRealizer background);

  /**
   * Sets the animator associated with this drawer.
   * 
   * @param animator The animator.
   */
  void setAnimator(Animator animator);

  /**
   * Getter.
   * 
   * @return Whether to show the legend.
   */
  boolean showLegend();

}
