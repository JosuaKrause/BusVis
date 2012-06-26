package infovis.embed;

import static infovis.util.VecUtil.*;
import infovis.embed.Weighter.WeightedEdge;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * An edge based positioner technique.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class EdgeEmbedder extends DirectEmbedder {

  /**
   * Creates an edge based positioner.
   * 
   * @param weighter The weighter.
   * @param drawer The drawer.
   */
  public EdgeEmbedder(final Weighter weighter, final NodeDrawer drawer) {
    super(weighter, drawer);
  }

  @Override
  protected Point2D getDestination(final SpringNode n, final Point2D pos, final SpringNode ref,
      final Point2D refP, final Point2D diff) {
    // FIXME fix order
    final List<WeightedEdge> edges = weighter.edgesTo(n);
    final WeightedEdge lastEdge = edges.get(edges.size() - 1);
    final SpringNode l = lastEdge.from;
    final Point2D p = new Point2D.Double(l.getPredictX(), l.getPredictY());
    return addVec(p, setLength(subVec(pos, p), lastEdge.weight));
  }

}
