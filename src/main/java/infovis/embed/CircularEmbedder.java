package infovis.embed;

import static infovis.VecUtil.*;

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
    Point2D refP;
    if(ref != null) {
      refP = ref.getPos();
    } else {
      refP = null;
    }
    for(final SpringNode n : weighter.nodes()) {
      final Point2D pos = weighter.getDefaultPosition(n);
      if(n == ref) {
        continue;
      }
      if(refP == null) {
        n.setPosition(pos);
        continue;
      }
      if(!weighter.hasWeight(n, ref)) {
        n.setPosition(new Point2D.Double());
        continue;
      }
      final double w = weighter.weight(n, ref);
      n.setPosition(addVec(setLength(subVec(pos, refP), w), refP));
    }
  }

}
