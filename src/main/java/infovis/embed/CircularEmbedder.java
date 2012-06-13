package infovis.embed;

import static infovis.VecUtil.*;
import infovis.embed.pol.Interpolator;

import java.awt.geom.Point2D;

/**
 * A simple circular embedder.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class CircularEmbedder extends AbstractEmbedder {

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
  protected void step() {
    final SpringNode ref = weighter.getReferenceNode();
    if(weighter.hasChanged()) {
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
        n.startAnimationTo(dest, Interpolator.INTERPOLATOR, Interpolator.DURATION);
      }
    }
    for(final SpringNode n : weighter.nodes()) {
      n.animate();
    }
  }

  @Override
  protected boolean doesDrag() {
    return false;
  }

}
