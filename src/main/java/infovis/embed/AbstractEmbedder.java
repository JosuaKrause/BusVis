package infovis.embed;

import infovis.gui.Context;
import infovis.gui.PainterAdapter;
import infovis.gui.Refreshable;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

/**
 * An abstract embedding visualization.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public abstract class AbstractEmbedder extends PainterAdapter implements Animator {

  /**
   * The frame rate of the spring embedder.
   */
  public static final long FRAMERATE = 60;

  /**
   * The waiting time resulting from the {@link #FRAMERATE}.
   */
  protected static final long FRAMEWAIT = Math.max(1000 / FRAMERATE, 1);

  /**
   * A list of refreshables that are refreshed, when a step has occured.
   */
  private final List<Refreshable> receivers;

  /**
   * The animator thread.
   */
  private final Thread animator;

  /**
   * Whether this object is already disposed or can still be used.
   */
  private volatile boolean disposed;

  /**
   * The drawer, drawing nodes.
   */
  protected final NodeDrawer drawer;

  /**
   * Creates an abstract embedder.
   * 
   * @param drawer The node drawer.
   */
  public AbstractEmbedder(final NodeDrawer drawer) {
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
            if(step()) {
              for(final Refreshable r : receivers) {
                r.refresh();
              }
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
   * Simulates one step.
   * 
   * @return Whether a redraw is necessary.
   */
  protected abstract boolean step();

  /**
   * Adds a refreshable that is refreshed each step.
   * 
   * @param r The refreshable.
   */
  public void addRefreshable(final Refreshable r) {
    if(disposed) throw new IllegalStateException("object already disposed");
    receivers.add(r);
  }

  @Override
  public void draw(final Graphics2D gfx, final Context ctx) {
    final Graphics2D g2 = (Graphics2D) gfx.create();
    drawer.drawBackground(g2, ctx);
    g2.dispose();
    for(final SpringNode n : drawer.nodes()) {
      final Graphics2D g = (Graphics2D) gfx.create();
      drawer.drawEdges(g, ctx, n);
      g.dispose();
    }
    for(final SpringNode n : drawer.nodes()) {
      final Graphics2D g = (Graphics2D) gfx.create();
      drawer.drawNode(g, ctx, n);
      g.dispose();
    }
  }

  @Override
  public void drawHUD(final Graphics2D gfx, final Context ctx) {
    for(final SpringNode n : drawer.nodes()) {
      final Graphics2D g = (Graphics2D) gfx.create();
      drawer.drawLabel(g, ctx, n);
      g.dispose();
    }
  }

  @Override
  public void moveMouse(final Point2D cur) {
    drawer.moveMouse(cur);
  }

  /**
   * A selected node.
   * 
   * @author Joschi <josua.krause@googlemail.com>
   */
  private static final class SelectedNode {

    /**
     * The actual node.
     */
    public final SpringNode node;

    /**
     * The x position at the time of selection.
     */
    public final double x;

    /**
     * The y position at the time of selection.
     */
    public final double y;

    /**
     * Creates a selected node.
     * 
     * @param node The selected node.
     */
    public SelectedNode(final SpringNode node) {
      this.node = node;
      x = node.getX();
      y = node.getY();
    }

  }

  /**
   * A list of all currently selected nodes.
   */
  private final List<SelectedNode> selected = new LinkedList<SelectedNode>();

  @Override
  public boolean acceptDrag(final Point2D p) {
    if(!doesDrag()) return false;
    selected.clear();
    for(final SpringNode n : drawer.nodes()) {
      final Shape s = drawer.nodeClickArea(n, true);
      if(s.contains(p)) {
        selected.add(new SelectedNode(n));
      }
    }
    return !selected.isEmpty();
  }

  @Override
  public boolean click(final Point2D p) {
    if(doesDrag()) return false;
    for(final SpringNode n : drawer.nodes()) {
      final Shape s = drawer.nodeClickArea(n, true);
      if(s.contains(p)) {
        drawer.selectNode(n);
        return true;
      }
    }
    return false;
  }

  /**
   * Getter.
   * 
   * @return Whether the embedder allows node dragging.
   */
  protected boolean doesDrag() {
    return true;
  }

  @Override
  public String getTooltip(final Point2D p) {
    String str = null;
    for(final SpringNode n : drawer.nodes()) {
      final Shape s = drawer.nodeClickArea(n, true);
      if(s.contains(p)) {
        final String text = drawer.getTooltipText(n);
        if(text != null) {
          str = text;
        }
      }
    }
    return str;
  }

  @Override
  public void drag(final Point2D start, final Point2D cur, final double dx,
      final double dy) {
    for(final SelectedNode n : selected) {
      drawer.dragNode(n.node, n.x, n.y, dx, dy);
    }
  }

  @Override
  public void endDrag(final Point2D start, final Point2D cur, final double dx,
      final double dy) {
    super.endDrag(start, cur, dx, dy);
    selected.clear();
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
   * Tests whether this spring embedder is disposed.
   * 
   * @return If it is disposed.
   */
  public boolean isDisposed() {
    return disposed;
  }

  @Override
  public Rectangle2D getBoundingBox() {
    return drawer.getBoundingBox();
  }

}
