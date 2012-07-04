package infovis.layout;

import static infovis.util.VecUtil.*;
import infovis.busvis.NodeDrawer;
import infovis.busvis.SpringNode;
import infovis.busvis.Weighter;
import infovis.busvis.Weighter.WeightedEdge;
import infovis.draw.BackgroundRealizer;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An edge based positioner technique.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class EdgeLayouter extends DirectLayouter {

  /**
   * Creates an edge based positioner.
   * 
   * @param weighter The weighter.
   * @param drawer The drawer.
   */
  public EdgeLayouter(final Weighter weighter, final NodeDrawer drawer) {
    super(weighter, drawer);
  }

  /**
   * The positions of the nodes.
   */
  private final Map<SpringNode, Point2D> positions = new HashMap<SpringNode, Point2D>();

  @Override
  protected void changedWeights(final Collection<SpringNode> nodes, final SpringNode ref,
      final Point2D refP, final Point2D diff) {
    positions.clear();
    positions.put(ref, refP);
    for(final SpringNode n : nodes) {
      putPosition(n, refP, diff);
    }
  }

  /**
   * Sets the position of one node.
   * 
   * @param n The node.
   * @param refP The position of the reference node.
   * @param diff The vector from the default position of the reference node to
   *          the current position of the reference node.
   */
  private void putPosition(final SpringNode n, final Point2D refP, final Point2D diff) {
    final List<WeightedEdge> edges = weighter.edgesTo(n);
    if(edges.isEmpty()) {
      if(!positions.containsKey(n)) {
        positions.put(n, new Point2D.Double(Double.NaN, Double.NaN));
      }
      return;
    }
    final WeightedEdge lastEdge = edges.get(edges.size() - 1);
    final SpringNode l = lastEdge.from;
    if(!positions.containsKey(l)) {
      putPosition(l, refP, diff);
    }
    final double w = lastEdge.weight;
    final Point2D otherPos = positions.get(l);
    final Point2D ownDefault = addVec(weighter.getDefaultPosition(n), diff);
    final double x1 = refP.getX();
    final double y1 = refP.getY();
    final double x2 = ownDefault.getX();
    final double y2 = ownDefault.getY();
    final double px = otherPos.getX();
    final double py = otherPos.getY();
    final Point2D dir = subVec(ownDefault, refP);
    final int ccw = Line2D.relativeCCW(x1, y1, x2, y2, px, py);
    Point2D res;
    final double distSq = ccw != 0 ? Line2D.ptLineDistSq(x1, y1, x2, y2, px, py) : 0;
    if(ccw == 0 || distSq == 0) {
      res = addVec(otherPos, setLength(dir, w));
    } else {
      Point2D rot;
      if(ccw < 0) { // clock-wise
        rot = new Point2D.Double(-dir.getY(), dir.getX());
      } else { // counter clock-wise
        rot = new Point2D.Double(dir.getY(), -dir.getX());
      }
      final double wSq = w * w;
      if(wSq <= distSq) {
        res = addVec(otherPos, setLength(rot, w));
      } else {
        final double dist = Math.sqrt(distSq);
        res = addVec(addVec(otherPos, setLength(rot, dist)),
            setLength(dir, Math.sqrt(wSq - distSq)));
      }
    }
    // 1 counter clock-wise
    positions.put(n, res);
  }

  @Override
  protected Point2D getDestination(final SpringNode n, final Point2D pos, final SpringNode ref,
      final Point2D refP, final Point2D diff) {
    return positions.get(n);
  }

  @Override
  public BackgroundRealizer backgroundRealizer() {
    return BackgroundRealizer.NO_BG;
  }

}
