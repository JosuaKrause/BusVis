package infovis.embed;

import static infovis.util.VecUtil.*;
import infovis.util.Interpolator;

import java.awt.geom.Point2D;

/**
 * A simple circular embedder.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class CircularEmbedder extends AbstractEmbedder {

  /**
   * The weighter.
   */
  private final Weighter weighter;

  /**
   * Creates a circular embedder.
   * 
   * @param weighter The weighter.
   * @param drawer The drawer.
   */
  public CircularEmbedder(final Weighter weighter, final NodeDrawer drawer) {
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
        Point2D dest;
        if(refP == null) {
          dest = pos;
        } else {
          if(!weighter.hasWeight(n, ref)) {
            dest = new Point2D.Double();
          } else {
            final double w = weighter.weight(n, ref);
            dest = addVec(setLength(subVec(addVec(pos, diff), refP), w), refP);
          }
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

  @Override
  protected boolean doesDrag() {
    return false;
  }

}
