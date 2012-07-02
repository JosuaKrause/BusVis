package infovis.embed;

import static infovis.util.VecUtil.*;
import infovis.draw.BackgroundRealizer;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

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

  private void setPos(final SpringNode n, final Point2D pos) {
    posMap[n.getId()] = pos;
  }

  private Point2D getPos(final SpringNode n) {
    return posMap[n.getId()];
  }

  @Override
  protected void changedWeights(final Collection<SpringNode> nodes, final SpringNode ref,
      final Point2D refP, final Point2D diff) {
    final double binSize = weighter.getFactor() * 5;
    final Map<Integer, Set<SpringNode>> bins = new HashMap<Integer, Set<SpringNode>>();
    // initial position
    for(final SpringNode n : nodes) {
      final Point2D pos = weighter.getDefaultPosition(n);
      final double w = weighter.weight(n, ref);
      final Point2D p = addVec(setLength(subVec(addVec(pos, diff), refP), w), refP);
      setPos(n, p);
      final int bin0 = (int) (w / binSize);
      final int bin1 = (int) ((w + binSize * 0.5) / binSize);
      addToBin(bins, bin0, n);
      addToBin(bins, bin1, n);
    }
    // conflict resolution
    final List<SpringNode> bin = new ArrayList<SpringNode>();
    for(final Set<SpringNode> bs : bins.values()) {
      bin.clear();
      bin.addAll(bs);
      final ListIterator<SpringNode> it = bin.listIterator();
      for(int i = 0; i < 1000; ++i) {
        while(it.hasNext()) {
          final SpringNode a = it.next();
          for(final SpringNode b : bin) {
            resolveIfNeeded(a, b, refP);
          }
        }
        while(it.hasPrevious()) {
          final SpringNode a = it.previous();
          for(final SpringNode b : bin) {
            resolveIfNeeded(a, b, refP);
          }
        }
      }
    }
  }

  private void resolveIfNeeded(final SpringNode a, final SpringNode b,
      final Point2D center) {
    final double ra = drawer.nodeRadius(a);
    final double rb = drawer.nodeRadius(b);
    final Point2D pa = getPos(a);
    final Point2D pb = getPos(b);
    final double distSq = pa.distanceSq(pb);
    if(Double.isNaN(distSq)) return;
    final double rSq = (ra + rb) * (ra + rb);
    if(distSq >= rSq) return;
    final double dist = Math.sqrt(distSq);
    final double da = ra - dist;
    final double db = rb - dist;
    Point2D rotA;
    Point2D rotB;
    if(isClockwiseOf(center, pa, pb)) {
      rotA = rotate(pa, center, -da);
      rotB = rotate(pb, center, db);
    } else {
      rotA = rotate(pa, center, da);
      rotB = rotate(pb, center, -db);
    }
    setPos(a, rotA);
    setPos(b, rotB);
  }

  /**
   * Adds a node to a bin.
   * 
   * @param bins The bin map.
   * @param bin The bin.
   * @param n The node.
   */
  private static void addToBin(final Map<Integer, Set<SpringNode>> bins,
      final int bin, final SpringNode n) {
    if(!bins.containsKey(bin)) {
      bins.put(bin, new HashSet<SpringNode>());
    }
    bins.get(bin).add(n);
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
