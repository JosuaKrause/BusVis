package infovis.embed;

import infovis.gui.Canvas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * Tests the spring embedder with a simple example spring system.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class EmbedderTest implements NodeDrawer, Weighter {

  /**
   * Starts the test.
   * 
   * @param args Ignored.
   */
  public static void main(final String[] args) {
    final EmbedderTest test = new EmbedderTest();
    final SpringEmbedder embed = new SpringEmbedder(test, test);
    final JFrame frame = new JFrame("Test");
    final Canvas c = new Canvas(embed, 800, 600) {

      private static final long serialVersionUID = -6834426709928877533L;

      @Override
      public void setupKeyActions() {
        addAction(KeyEvent.VK_L, new AbstractAction() {

          private static final long serialVersionUID = 3840566617434458358L;

          @Override
          public void actionPerformed(final ActionEvent arg0) {
            test.toggleMode();
          }

        });
        addAction(KeyEvent.VK_Q, new AbstractAction() {

          private static final long serialVersionUID = -6529074015382752666L;

          @Override
          public void actionPerformed(final ActionEvent e) {
            frame.dispose();
          }

        });
      }

    };
    embed.addRefreshable(c);
    frame.add(c);
    frame.pack();
    c.setBackground(Color.WHITE);
    c.reset();
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);

  }

  /**
   * Creates a new embedder test.
   */
  public EmbedderTest() {
    for(int i = 0; i < 100; ++i) {
      nodes.add(new SpringNode());
    }
  }

  /**
   * Whether the weighter is in line or circle mode.
   */
  private boolean line = true;

  /**
   * Toggles the mode between line and circle.
   */
  protected void toggleMode() {
    line = !line;
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
    return 0.75;
  }

  /**
   * The list holding all nodes.
   */
  private final List<SpringNode> nodes = new ArrayList<SpringNode>();

  @Override
  public Iterable<SpringNode> nodes() {
    return nodes;
  }

  @Override
  public double weight(final SpringNode from, final SpringNode to) {
    return line ? Math.abs(nodes.indexOf(from) - nodes.indexOf(to)) * 17 : 5;
  }

  @Override
  public boolean hasWeight(final SpringNode from, final SpringNode to) {
    return line ? Math.abs(nodes.indexOf(from) - nodes.indexOf(to) + 2) < 3
        : (Math.abs(nodes.indexOf(from) - nodes.indexOf(to)) % (nodes.size() - 1)) < 2;
  }

  @Override
  public void clickedAt(final SpringNode n) {
    final Random r = new Random();
    n.addMove(r.nextGaussian() * 17, r.nextGaussian() * 17);
  }

  @Override
  public Shape nodeClickArea(final SpringNode n) {
    final double x = n.getX();
    final double y = n.getY();
    return new Ellipse2D.Double(x - 2, y - 2, 4, 4);
  }

}
