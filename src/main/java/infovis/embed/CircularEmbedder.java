package infovis.embed;

import static infovis.util.VecUtil.*;

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
      posMap[n.getId()] = p;
      final int bin0 = (int) (w / binSize);
      final int bin1 = (int) ((w + binSize * 0.5) / binSize);
      addToBin(bins, bin0, n);
      addToBin(bins, bin1, n);
    }
    // conflict resolution
    final List<SpringNode> bin = new ArrayList<SpringNode>();
    for(final Set<SpringNode> b : bins.values()) {
      bin.clear();
      bin.addAll(b);
      final ListIterator<SpringNode> it = bin.listIterator();
      while(it.hasNext()) {
        it.next();
      }
      while(it.hasPrevious()) {
        it.previous();
      }
      // TODO
    }
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
    return posMap[n.getId()];
  }

  @Override
  public boolean drawCircles() {
    return true;
  }

}
