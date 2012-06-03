package infovis.embed;

import infovis.gui.PainterAdapter;
import infovis.gui.Refreshable;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

public class SpringEmbedder extends PainterAdapter {

  public static final long FRAMERATE = 60;

  protected static final long FRAMEWAIT = Math.max(1000 / FRAMERATE, 1);

  private final Weighter weighter;

  private final NodeDrawer drawer;

  private final List<Refreshable> receivers;

  protected volatile boolean disposed;

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

  public void addRefreshable(final Refreshable r) {
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

  public void dispose() {
    disposed = true;
    receivers.clear();
  }

}
