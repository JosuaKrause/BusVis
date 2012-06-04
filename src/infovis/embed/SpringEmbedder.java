package infovis.embed;

import infovis.gui.PainterAdapter;
import infovis.gui.Refreshable;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

/**
 * Simulates a spring embedder.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class SpringEmbedder extends PainterAdapter {

  /**
   * The frame rate of the spring embedder.
   */
  public static final long FRAMERATE = 60;

  /**
   * The waiting time resulting from the {@link #FRAMERATE}.
   */
  protected static final long FRAMEWAIT = Math.max(1000 / FRAMERATE, 1);

  /**
   * The weighter, defining edges between nodes.
   */
  private final Weighter weighter;

  /**
   * The drawer, drawing nodes.
   */
  private final NodeDrawer drawer;

  /**
   * A list of refreshables that are refreshed, when a step has occured.
   */
  private final List<Refreshable> receivers;

  /**
   * Whether this object is already disposed or can still be used.
   */
  protected volatile boolean disposed;

  /**
   * Creates a spring embedder and automatically starts it.
   * 
   * @param weighter The weighter.
   * @param drawer The drawer.
   */
  public SpringEmbedder(final Weighter weighter, final NodeDrawer drawer) {
    this.weighter = weighter;
    this.drawer = drawer;
    receivers = new LinkedList<Refreshable>();
    final Thread t = new Thread() {

      @Override
      public void run() {
        try {
          while(!isInterrupted() && !disposed) {
            synchronized(this) {
              try {
                wait(FRAMEWAIT);
              } catch(final InterruptedException e) {
                interrupt();
                continue;
              }
            }
            step();
          }
        } finally {
          disposed = true;
        }
      }

    };
    t.setDaemon(true);
    t.start();
  }

  /**
   * Simulates one step.
   */
  protected void step() {
    double mx = 0;
    double my = 0;
    double m = 0;
    for(final SpringNode n : weighter.nodes()) {
      n.move(weighter);
      mx += n.getDx();
      my += n.getDy();
      ++m;
    }
    mx /= -m;
    my /= -m;
    for(final SpringNode n : weighter.nodes()) {
      if(correctMovement) {
        n.addMove(mx, my);
      }
      n.step();
    }
    for(final Refreshable r : receivers) {
      r.refresh();
    }
  }

  /**
   * Whether to correct the movement of the nodes by removing overall movements.
   */
  private boolean correctMovement;

  /**
   * Getter.
   * 
   * @return Whether the movement is corrected.
   */
  public boolean isCorrectingMovement() {
    return correctMovement;
  }

  /**
   * Setter.
   * 
   * @param correctMovement Sets whether to correct overall movements.
   */
  public void setCorrectMovement(final boolean correctMovement) {
    this.correctMovement = correctMovement;
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

  @Override
  public void draw(final Graphics2D gfx) {
    for(final SpringNode n : weighter.nodes()) {
      final Graphics2D g = (Graphics2D) gfx.create();
      drawer.drawNode(g, n);
      g.dispose();
    }
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
    selected.clear();
    for(final SpringNode n : weighter.nodes()) {
      final Shape s = drawer.nodeClickArea(n);
      if(s.contains(p)) {
        selected.add(new SelectedNode(n));
      }
    }
    return !selected.isEmpty();
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
  }

  /**
   * Tests whether this spring embedder is disposed.
   * 
   * @return If it is disposed.
   */
  public boolean isDisposed() {
    return disposed;
  }

}
