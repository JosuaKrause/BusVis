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
      n.addMove(mx, my);
      n.step();
    }
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

  @Override
  public void draw(final Graphics2D gfx) {
    for(final SpringNode n : weighter.nodes()) {
      final Graphics2D g = (Graphics2D) gfx.create();
      drawer.drawNode(g, n);
      g.dispose();
    }
  }

  @Override
  public boolean click(final Point2D p) {
    boolean accepted = false;
    for(final SpringNode n : weighter.nodes()) {
      final Shape s = drawer.nodeClickArea(n);
      if(s.contains(p)) {
        drawer.clickedAt(n);
        accepted = true;
      }
    }
    return accepted;
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

}
