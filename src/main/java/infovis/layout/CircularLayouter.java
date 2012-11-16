package infovis.layout;

import static infovis.util.VecUtil.*;
import infovis.busvis.LayoutNode;
import infovis.busvis.NodeDrawer;
import infovis.busvis.Weighter;
import infovis.draw.BackgroundRealizer;

import java.awt.geom.Point2D;
import java.util.Collection;

/**
 * A simple circular layouter.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class CircularLayouter extends DirectLayouter {

  /** The positions of the nodes. */
  private final Point2D[] posMap;

  /**
   * Creates a circular layouter.
   * 
   * @param weighter The weighter.
   * @param drawer The drawer.
   */
  public CircularLayouter(final Weighter weighter, final NodeDrawer drawer) {
    super(weighter, drawer);
    posMap = new Point2D[weighter.nodes().size()];
  }

  /**
   * Sets the position of the node.
   * 
   * @param n The node.
   * @param pos The position.
   */
  private void setPos(final LayoutNode n, final Point2D pos) {
    posMap[n.getId()] = pos;
  }

  /**
   * Getter.
   * 
   * @param n The node.
   * @return The position.
   */
  private Point2D getPos(final LayoutNode n) {
    return posMap[n.getId()];
  }

  @Override
  protected void changedWeights(final Collection<LayoutNode> nodes,
      final Collection<LayoutNode> relevant, final LayoutNode ref,
      final Point2D refP, final Point2D diff) {
    // initial position
    for(final LayoutNode n : nodes) {
      final Point2D pos = weighter.getDefaultPosition(n);
      final double w = weighter.weight(n, ref);
      final Point2D p;
      if(Double.isNaN(w)) {
        p = new Point2D.Double(Double.NaN, Double.NaN);
      } else {
        p = addVec(setLength(subVec(addVec(pos, diff), refP), w), refP);
        relevant.add(n);
      }
      setPos(n, p);
    }
    // conflict resolution
    resolve(relevant, refP, 100);
  }

  /**
   * Resolves overlaps.
   * 
   * @param nodes The nodes that may overlap.
   * @param refP The reference node.
   * @param maxIter Maximum number of allowed iterations.
   * @return Whether more iterations are needed.
   */
  private boolean resolve(final Collection<LayoutNode> nodes,
      final Point2D refP, final int maxIter) {
    int i = maxIter;
    boolean hasChanged = true;
    while(hasChanged) {
      hasChanged = false;
      for(final LayoutNode a : nodes) {
        for(final LayoutNode b : nodes) {
          hasChanged |= resolveIfNeeded(a, b, refP);
        }
      }
      // we certainly do not want an infinite loop
      if(--i < 0) {
        break;
      }
    }
    return hasChanged;
  }

  @Override
  protected boolean refinePositions(
      final Collection<LayoutNode> relevant, final Point2D refP) {
    return resolve(relevant, refP, 100);
  }

  /**
   * Resolves overlapping of two nodes if they overlap.
   * 
   * @param a A node.
   * @param b A node.
   * @param center The center point.
   * @return Whether the position has changed.
   */
  private boolean resolveIfNeeded(final LayoutNode a,
      final LayoutNode b, final Point2D center) {
    if(a.equals(b)) return false;
    final double ra = drawer.nodeRadius(a);
    final double rb = drawer.nodeRadius(b);
    final Point2D pa = getPos(a);
    final Point2D pb = getPos(b);
    final double distSq = pa.distanceSq(pb);
    if(Double.isNaN(distSq)) return false;
    final double rSum = ra + rb;
    final double rSq = rSum * rSum;
    if(distSq >= rSq) return false;
    final double da = rSum;
    final double db = rSum;
    if(Double.isNaN(da) || Double.isNaN(db)) return false;
    Point2D rotA;
    Point2D rotB;
    if(isClockwiseOf(center, pa, pb)) {
      rotA = rotate(pa, center, -db);
      rotB = rotate(pb, center, da);
    } else {
      rotA = rotate(pa, center, db);
      rotB = rotate(pb, center, -da);
    }
    setPos(a, rotA);
    setPos(b, rotB);
    return true;
  }

  @Override
  protected Point2D getDestination(final LayoutNode n) {
    return getPos(n);
  }

  @Override
  protected BackgroundRealizer backgroundRealizer() {
    return BackgroundRealizer.CIRCLES;
  }

}
