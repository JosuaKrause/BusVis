package infovis.embed;

import static infovis.util.VecUtil.*;
import infovis.draw.BackgroundRealizer;

import java.awt.geom.Point2D;
import java.util.Collection;

/**
 * A simple circular embedder.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class CircularEmbedder extends DirectEmbedder {

  /**
   * The positions of the nodes.
   */
  private final Point2D[] posMap;

  /**
   * Creates a circular embedder.
   * 
   * @param weighter The weighter.
   * @param drawer The drawer.
   */
  public CircularEmbedder(final Weighter weighter, final NodeDrawer drawer) {
    super(weighter, drawer);
    posMap = new Point2D[weighter.nodes().size()];
  }

  /**
   * Sets the position of the node.
   * 
   * @param n The node.
   * @param pos The position.
   */
  private void setPos(final SpringNode n, final Point2D pos) {
    posMap[n.getId()] = pos;
  }

  /**
   * Getter.
   * 
   * @param n The node.
   * @return The position.
   */
  private Point2D getPos(final SpringNode n) {
    return posMap[n.getId()];
  }

  @Override
  protected void changedWeights(final Collection<SpringNode> nodes, final SpringNode ref,
      final Point2D refP, final Point2D diff) {
    // initial position
    for(final SpringNode n : nodes) {
      final Point2D pos = weighter.getDefaultPosition(n);
      final double w = weighter.weight(n, ref);
      final Point2D p = addVec(setLength(subVec(addVec(pos, diff), refP), w), refP);
      setPos(n, p);
    }
    // conflict resolution
    int i = 1000;
    boolean hasChanged = true;
    while(hasChanged) {
      hasChanged = false;
      for(final SpringNode a : nodes) {
        for(final SpringNode b : nodes) {
          hasChanged |= resolveIfNeeded(a, b, refP);
        }
      }
      // we certainly do not want an infinite loop
      if(--i < 0) {
        break;
      }
    }
  }

  /**
   * Resolves overlapping of two nodes if they overlap.
   * 
   * @param a A node.
   * @param b A node.
   * @param center The center point.
   * @return Whether the position has changed.
   */
  private boolean resolveIfNeeded(final SpringNode a, final SpringNode b,
      final Point2D center) {
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
  protected Point2D getDestination(final SpringNode n, final Point2D pos,
      final SpringNode ref, final Point2D refP, final Point2D diff) {
    return getPos(n);
  }

  @Override
  protected BackgroundRealizer backgroundRealizer() {
    return BackgroundRealizer.CIRCLES;
  }

}
