package infovis.embed;

import infovis.gui.Canvas;
import infovis.gui.PainterAdapter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class EmbedderTest extends PainterAdapter implements NodeDrawer, Weighter {

  public static void main(final String[] args) {
    final EmbedderTest test = new EmbedderTest();
    final Canvas c = new Canvas(test, 800, 600);
    final JFrame frame = new JFrame("Test");
    frame.add(c);
    frame.pack();
    c.setBackground(Color.WHITE);
    c.reset(new Rectangle2D.Double(-400, -300, 800, 600));
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
    final Thread t = new Thread() {

      @Override
      public void run() {
        while(!isInterrupted()) {
          synchronized(this) {
            try {
              wait(30);
            } catch(final InterruptedException e) {
              interrupt();
              continue;
            }
          }
          for(final SpringNode n : test.nodes()) {
            n.move(test);
          }
          double mx = 0;
          double my = 0;
          double m = 0;
          for(final SpringNode n : test.nodes()) {
            mx += n.getDx();
            my += n.getDy();
            ++m;
          }
          mx /= -m;
          my /= -m;
          for(final SpringNode n : test.nodes()) {
            n.addMove(mx, my);
            n.step();
          }
          c.repaint();
        }
      }

    };
    t.setDaemon(true);
    t.start();
  }

  public EmbedderTest() {
    for(int i = 0; i < 100; ++i) {
      nodes.add(new SpringNode());
    }
  }

  @Override
  public void drawNode(final Graphics2D g, final SpringNode n) {
    final double x = n.getX();
    final double y = n.getY();
    for(final SpringNode o : nodes) {
      if(o == n) {
        g.setColor(Color.RED);
        g.fill(new Ellipse2D.Double(x - 2, y - 2, 4, 4));
        continue;
      }
      if(!hasWeight(n, o)) {
        continue;
      }
      g.setColor(new Color(0x10000000, true));
      final double ox = o.getX();
      final double oy = o.getY();
      g.draw(new Line2D.Double(x, y, ox, oy));
    }
  }

  @Override
  public double springConstant() {
    return 0.9;
  }

  private final List<SpringNode> nodes = new ArrayList<SpringNode>();

  @Override
  public Iterable<SpringNode> nodes() {
    return nodes;
  }

  @Override
  public double weight(final SpringNode from, final SpringNode to) {
    return Math.abs(nodes.indexOf(from) - nodes.indexOf(to)) * 17;
  }

  @Override
  public boolean hasWeight(final SpringNode from, final SpringNode to) {
    return Math.abs(nodes.indexOf(from) - nodes.indexOf(to) + 2) < 3;
  }

  @Override
  public void draw(final Graphics2D gfx) {
    gfx.setColor(Color.BLACK);
    for(final SpringNode n : nodes) {
      final Graphics2D g = (Graphics2D) gfx.create();
      drawNode(g, n);
      g.dispose();
    }
  }

}
