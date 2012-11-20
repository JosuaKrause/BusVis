package infovis.layout;

import static infovis.busvis.Weighter.ChangeType.*;
import static infovis.util.VecUtil.*;
import infovis.busvis.LayoutNode;
import infovis.busvis.NodeDrawer;
import infovis.busvis.Weighter;
import infovis.busvis.Weighter.ChangeType;
import infovis.ctrl.Controller;
import infovis.data.BusTime;
import infovis.util.Interpolator;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

/**
 * A direct layouter. Positions the nodes each time the weight changes. The
 * nodes move with animation.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public abstract class DirectLayouter extends AbstractLayouter {

  /** The weighter. */
  protected final Weighter weighter;

  /**
   * Creates a layouter.
   * 
   * @param weighter The weighter.
   * @param drawer The drawer.
   */
  public DirectLayouter(final Weighter weighter, final NodeDrawer drawer) {
    super(drawer);
    this.weighter = weighter;
  }

  /** The current set of relevant (ie. valid) nodes. */
  private final Collection<LayoutNode> relevantNodes = new ArrayList<LayoutNode>();

  /** Whether to further iterate. */
  private boolean iterateAfterwards;

  /** Number of iterations. */
  private int count;

  @Override
  protected boolean step() {
    final ChangeType change = weighter.changes();
    final int duration;
    if(change == FAST_FORWARD_CHANGE) {
      duration = BusTime.MILLISECONDS_PER_SECOND
          - Calendar.getInstance().get(Calendar.MILLISECOND) - 1;
    } else {
      duration = 0;
    }
    final LayoutNode ref = weighter.getReferenceNode();
    if(change != NO_CHANGE) {
      final Point2D refP;
      final Point2D diff;
      if(ref != null) {
        final Point2D orig = weighter.getDefaultPosition(ref);
        refP = ref.getPos();
        diff = subVec(refP, orig);
      } else {
        refP = null;
        diff = null;
      }
      final Collection<LayoutNode> nodes = weighter.nodes();
      relevantNodes.clear();
      iterateAfterwards = true;
      if(refP != null) {
        changedWeights(nodes, relevantNodes, ref, refP, diff);
      }
      for(final LayoutNode n : nodes) {
        final Point2D pos = weighter.getDefaultPosition(n);
        if(n == ref) {
          continue;
        }
        final Point2D dest;
        if(refP == null) {
          dest = pos;
        } else {
          dest = getDestination(n);
        }
        switch(change) {
          case FAST_ANIMATION_CHANGE:
            n.startAnimationTo(dest, Interpolator.LINEAR, LayoutNode.FAST);
            break;
          case FAST_FORWARD_CHANGE:
            n.startAnimationTo(dest, Interpolator.LINEAR, duration);
            break;
          case REALTIME_CHANGE:
            n.startAnimationTo(dest, Interpolator.LINEAR, Controller.REALTIME
                * BusTime.MILLISECONDS_PER_SECOND);
            break;
          case PREPARE_CHANGE:
            n.startAnimationTo(dest, Interpolator.SMOOTH, LayoutNode.LONG);
            break;
          default:
            n.startAnimationTo(dest, Interpolator.SMOOTH, LayoutNode.NORMAL);
        }
      }
    } else if(iterateAfterwards) {
      final Point2D refP = ref != null ? ref.getPos() : null;
      if(relevantNodes.isEmpty() || refP == null) {
        iterateAfterwards = false;
      }
      if(iterateAfterwards) {
        ++count;
        if(count > 50) {
          count = 0;
          final long start = System.currentTimeMillis();
          iterateAfterwards = refinePositions(relevantNodes, refP);
          final long transition = LayoutNode.FAST + (System.currentTimeMillis() - start);
          for(final LayoutNode n : relevantNodes) {
            if(n == ref) {
              continue;
            }
            n.changeAnimationTo(getDestination(n), Interpolator.LINEAR, (int) transition);
          }
        }
      }
    }
    boolean needsRedraw = weighter.inAnimation();
    for(final LayoutNode n : weighter.nodes()) {
      n.animate();
      needsRedraw = needsRedraw || n.lazyInAnimation();
    }
    return iterateAfterwards || needsRedraw;
  }

  /**
   * Refines the position after the initial layout until this method return
   * <code>false</code>.
   * 
   * @param relevant The relevant nodes.
   * @param refP The reference point.
   * @return Whether to keep refining the layout.
   */
  protected boolean refinePositions(
      @SuppressWarnings("unused") final Collection<LayoutNode> relevant,
      @SuppressWarnings("unused") final Point2D refP) {
    return false;
  }

  /**
   * Is called when weights have changed.
   * 
   * @param nodes The nodes.
   * @param relevant This array must be filled with nodes that are relevant ie.
   *          valid.
   * @param ref The reference node.
   * @param refP The position of the reference node.
   * @param diff The vector from the reference nodes default position to its
   *          current position.
   */
  @SuppressWarnings("unused")
  protected void changedWeights(final Collection<LayoutNode> nodes,
      final Collection<LayoutNode> relevant, final LayoutNode ref,
      final Point2D refP, final Point2D diff) {
    // nothing to do
  }

  /**
   * Calculates the destination of the given node.
   * 
   * @param n The node.
   * @return The desired position of the given node.
   */
  protected abstract Point2D getDestination(LayoutNode n);

}
