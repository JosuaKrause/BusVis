package infovis.embed;

import static infovis.util.VecUtil.*;

import java.awt.geom.Point2D;

/**
 * A simple circular embedder.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class CircularEmbedder extends DirectEmbedder {

  /**
   * Creates a circular embedder.
   * 
   * @param weighter The weighter.
   * @param drawer The drawer.
   */
  public CircularEmbedder(final Weighter weighter, final NodeDrawer drawer) {
    super(weighter, drawer);
  }

  @Override
  protected Point2D getDestination(final SpringNode n, final Point2D pos,
      final SpringNode ref,
      final Point2D refP, final Point2D diff) {
    final double w = weighter.weight(n, ref);
    return addVec(setLength(subVec(addVec(pos, diff), refP), w), refP);
  }

  @Override
  public boolean drawCircles() {
    return true;
  }

}
