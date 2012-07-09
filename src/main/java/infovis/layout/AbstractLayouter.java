package infovis.layout;

import infovis.busvis.Animator;
import infovis.busvis.LayoutNode;
import infovis.busvis.NodeDrawer;
import infovis.data.BusLine;
import infovis.draw.BackgroundRealizer;
import infovis.gui.Context;
import infovis.gui.PainterAdapter;
import infovis.gui.Refreshable;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.BitSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;

/**
 * An abstract visualization.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public abstract class AbstractLayouter extends PainterAdapter implements Animator {

  /** The frame rate of the layouter. */
  public static final long FRAMERATE = 60;

  /** The waiting time resulting from the {@link #FRAMERATE}. */
  protected static final long FRAMEWAIT = Math.max(1000 / FRAMERATE, 1);

  /** A list of refreshables that are refreshed, when a frame can be drawn. */
  private final List<Refreshable> receivers;

  /** The animator thread. */
  private final Thread animator;

  /** Whether this object is already disposed or can still be used. */
  private volatile boolean disposed;

  /** The drawer, drawing nodes. */
  protected final NodeDrawer drawer;

  /**
   * Creates an abstract layouter.
   * 
   * @param drawer The node drawer.
   */
  public AbstractLayouter(final NodeDrawer drawer) {
    this.drawer = drawer;
    final List<Refreshable> receivers = new LinkedList<Refreshable>();
    animator = new Thread() {

      @Override
      public void run() {
        try {
          while(!isInterrupted() && !isDisposed()) {
            synchronized(this) {
              try {
                wait(FRAMEWAIT);
              } catch(final InterruptedException e) {
                interrupt();
                continue;
              }
            }
            boolean needsRedraw;
            synchronized(this) {
              needsRedraw = step();
            }
            if(needsRedraw) {
              refreshAll();
            }
          }
        } finally {
          dispose();
        }
      }

    };
    animator.setDaemon(true);
    animator.start();
    this.receivers = receivers;
    drawer.setAnimator(this);
  }

  /**
   * Getter.
   * 
   * @return The animation lock.
   */
  public Object getAnimationLock() {
    return animator;
  }

  /**
   * Simulates one step.
   * 
   * @return Whether a redraw is necessary.
   */
  protected abstract boolean step();

  /** Refreshes all refreshables. */
  protected void refreshAll() {
    for(final Refreshable r : receivers) {
      r.refresh();
    }
  }

  /**
   * Adds a refreshable that is refreshed each step.
   * 
   * @param r The refreshable.
   */
  public void addRefreshable(final Refreshable r) {
    if(disposed) throw new IllegalStateException("object already disposed");
    receivers.add(r);
  }

  /** Accumulates all visible lines. */
  private final Set<BusLine> visibleLines = new HashSet<BusLine>();

  @Override
  public void draw(final Graphics2D gfx, final Context ctx) {
    visibleLines.clear();
    final Graphics2D g2 = (Graphics2D) gfx.create();
    drawer.drawBackground(g2, ctx, backgroundRealizer());
    g2.dispose();
    for(final LayoutNode n : drawer.nodes()) {
      final Graphics2D g = (Graphics2D) gfx.create();
      drawer.drawEdges(g, ctx, n, visibleLines, drawer.hasSecondarySelection());
      g.dispose();
    }
    for(final LayoutNode sel : drawer.secondarySelected()) {
      final Graphics2D g = (Graphics2D) gfx.create();
      drawer.drawSecondarySelected(g, ctx, sel);
      g.dispose();
    }
    for(final LayoutNode n : drawer.nodes()) {
      final Graphics2D g = (Graphics2D) gfx.create();
      drawer.drawNode(g, ctx, n, drawer.isSecondarySelected(n));
      g.dispose();
    }
  }

  @Override
  public void drawHUD(final Graphics2D gfx, final Context ctx) {
    if(drawer.hasSecondarySelection()) {
      final BitSet tmp = new BitSet();
      for(final LayoutNode n : drawer.secondarySelected()) {
        final Graphics2D g = (Graphics2D) gfx.create();
        drawer.drawRouteLabels(g, ctx, n, tmp);
        g.dispose();
      }
      tmp.xor(hovered);
      tmp.and(hovered);
      for(int i = tmp.nextSetBit(0); i >= 0; i = tmp.nextSetBit(i + 1)) {
        final Graphics2D g = (Graphics2D) gfx.create();
        drawer.drawLabel(g, ctx, drawer.getNode(i), true, null);
        g.dispose();
      }
    } else {
      for(final LayoutNode n : drawer.nodes()) {
        final Graphics2D g = (Graphics2D) gfx.create();
        drawer.drawLabel(g, ctx, n, hovered.get(n.getId()), null);
        g.dispose();
      }
    }
    if(drawer.showLegend()) {
      final Graphics2D g2 = (Graphics2D) gfx.create();
      drawer.drawLegend(g2, ctx, visibleLines);
      g2.dispose();
    }
  }

  /** The lookup for hovered nodes. */
  private final BitSet hovered = new BitSet();

  @Override
  public void moveMouse(final Point2D cur) {
    drawer.moveMouse(cur);
    for(final LayoutNode n : drawer.nodes()) {
      final Shape s = drawer.nodeClickArea(n, true);
      hovered.set(n.getId(), s.contains(cur));
    }
    if(!hovered.isEmpty()) {
      refreshAll();
    }
  }

  @Override
  public boolean click(final Point2D p, final MouseEvent e) {
    if(SwingUtilities.isRightMouseButton(e)) {
      final Set<LayoutNode> secSel = new HashSet<LayoutNode>();
      for(final LayoutNode n : drawer.nodes()) {
        final Shape s = drawer.nodeClickArea(n, true);
        if(s.contains(p)) {
          secSel.add(n);
        }
      }
      drawer.secondarySelection(secSel);
      return true;
    }
    if(SwingUtilities.isLeftMouseButton(e)) {
      for(final LayoutNode n : drawer.nodes()) {
        final Shape s = drawer.nodeClickArea(n, true);
        if(s.contains(p)) {
          drawer.selectNode(n);
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Disposes the object by cleaning all refreshables and stopping the
   * simulation thread. The object cannot be used anymore after a call to this
   * method.
   */
  public void dispose() {
    disposed = true;
    receivers.clear();
    animator.interrupt();
  }

  @Override
  public void forceNextFrame() {
    synchronized(animator) {
      animator.notifyAll();
    }
  }

  /**
   * Tests whether this layouter is disposed.
   * 
   * @return If it is disposed.
   */
  public boolean isDisposed() {
    return disposed;
  }

  @Override
  public Rectangle2D getBoundingBox() {
    return drawer.getBoundingBox(backgroundRealizer());
  }

  /**
   * Getter.
   * 
   * @return How the background is drawn.
   */
  protected abstract BackgroundRealizer backgroundRealizer();

}
