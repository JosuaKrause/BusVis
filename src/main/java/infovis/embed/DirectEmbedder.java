package infovis.embed;

import static infovis.util.VecUtil.*;
import infovis.util.Interpolator;

import java.awt.geom.Point2D;

/**
 * A direct embedder. Positions the nodes each time the weight changes. The
 * nodes move with animation.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public abstract class DirectEmbedder extends AbstractEmbedder {

  /**
   * The weighter.
   */
  protected final Weighter weighter;

  /**
   * Creates a edge embedder.
   * 
   * @param weighter The weighter.
   * @param drawer The drawer.
   */
  public DirectEmbedder(final Weighter weighter, final NodeDrawer drawer) {
    super(drawer);
    this.weighter = weighter;
  }

  @Override
  protected boolean step() {
    final SpringNode ref = weighter.getReferenceNode();
    final int changes = weighter.changes();
    if(changes != Weighter.NO_CHANGE) {
      Point2D diff;
      Point2D refP;
      if(ref != null) {
        final Point2D orig = weighter.getDefaultPosition(ref);
        refP = ref.getPos();
        diff = subVec(refP, orig);
      } else {
        refP = null;
        diff = null;
      }
      for(final SpringNode n : weighter.nodes()) {
        final Point2D pos = weighter.getDefaultPosition(n);
        if(n == ref) {
          continue;
        }
        final Point2D dest;
        if(refP == null) {
          dest = pos;
        } else {
          dest = getDestination(n, pos, ref, refP, diff);
        }
        if((changes & Weighter.FAST_ANIMATION_CHANGE) != 0) {
          n.startAnimationTo(dest, Interpolator.LINEAR, Interpolator.FAST);
        } else {
          n.startAnimationTo(dest, Interpolator.SMOOTH, Interpolator.NORMAL);
        }
      }
    }
    boolean needsRedraw = weighter.inAnimation();
    for(final SpringNode n : weighter.nodes()) {
      n.animate();
      needsRedraw = needsRedraw || n.inAnimation();
    }
    return needsRedraw;
  }

  /**
   * Calculates the destination of the given node.
   * 
   * @param n The node.
   * @param pos The default position of this node.
   * @param ref The reference node.
   * @param refP The position of the reference node.
   * @param diff The difference of the two nodes.
   * @return The desired position of the given node.
   */
  protected abstract Point2D getDestination(SpringNode n, Point2D pos, SpringNode ref,
      Point2D refP, Point2D diff);

  @Override
  protected boolean doesDrag() {
    return false;
  }

}