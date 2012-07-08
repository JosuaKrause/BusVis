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
      final Point2D diff;
      final Point2D refP;
      if(ref != null) {
        final Point2D orig = weighter.getDefaultPosition(ref);
        refP = ref.getPos();
        diff = subVec(refP, orig);
      } else {
        refP = null;
        diff = null;
      }
      final Collection<LayoutNode> nodes = weighter.nodes();
      if(refP != null) {
        changedWeights(nodes, ref, refP, diff);
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
          dest = getDestination(n, pos, ref, refP, diff);
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
    }
    boolean needsRedraw = weighter.inAnimation();
    for(final LayoutNode n : weighter.nodes()) {
      n.animate();
      needsRedraw = needsRedraw || n.inAnimation();
    }
    return needsRedraw;
  }

  /**
   * Is called when weights have changed.
   * 
   * @param ref The reference node.
   * @param refP The position of the reference node.
   * @param diff The vector from the reference nodes default position to its
   *          current position.
   * @param nodes The nodes.
   */
  @SuppressWarnings("unused")
  protected void changedWeights(final Collection<LayoutNode> nodes, final LayoutNode ref,
      final Point2D refP, final Point2D diff) {
    // nothing to do
  }

  /**
   * Calculates the destination of the given node.
   * 
   * @param n The node.
   * @param pos The default position of this node.
   * @param ref The reference node.
   * @param refP The position of the reference node.
   * @param diff The vector from the reference nodes default position to its
   *          current position.
   * @return The desired position of the given node.
   */
  protected abstract Point2D getDestination(LayoutNode n, Point2D pos, LayoutNode ref,
      Point2D refP, Point2D diff);

}
