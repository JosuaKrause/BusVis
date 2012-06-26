package infovis.embed;

import infovis.gui.Canvas;
import infovis.gui.Context;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * Tests the spring embedder with a simple example spring system.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class EmbedderTest implements NodeDrawer, Weighter {

  /**
   * Starts the test.
   * 
   * @param args Ignored.
   */
  @SuppressWarnings("deprecation")
  public static void main(final String[] args) {
    final EmbedderTest test = new EmbedderTest();
    final SpringEmbedder embed = new SpringEmbedder(test, test);
    final JFrame frame = new JFrame("Test");
    final Canvas c = new Canvas(embed, 800, 600);
    c.addAction(KeyEvent.VK_L, new AbstractAction() {

      private static final long serialVersionUID = 3840566617434458358L;

      @Override
      public void actionPerformed(final ActionEvent arg0) {
        test.toggleMode();
      }

    });
    c.addAction(KeyEvent.VK_Q, new AbstractAction() {

      private static final long serialVersionUID = -6529074015382752666L;

      @Override
      public void actionPerformed(final ActionEvent e) {
        frame.dispose();
      }

    });
    c.addAction(KeyEvent.VK_M, new AbstractAction() {

      private static final long serialVersionUID = 8243341373949395480L;

      @Override
      public void actionPerformed(final ActionEvent e) {
        embed.setCorrectMovement(!embed.isCorrectingMovement());
      }

    });
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
  public void drawNode(final Graphics2D g, final Context ctx, final SpringNode n) {
    final double x = n.getX();
    final double y = n.getY();
    g.setColor(Color.RED);
    g.fill(new Ellipse2D.Double(x - 2, y - 2, 4, 4));
  }

  @Override
  public void drawLabel(final Graphics2D g, final Context ctx, final SpringNode n) {
    // no label
  }

  @Override
  public void drawEdges(final Graphics2D g, final Context ctx, final SpringNode n) {
    final double x = n.getX();
    final double y = n.getY();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
    g.setColor(Color.BLACK);
    for(final SpringNode o : nodes) {
      if(!areNeighbors(n, o)) {
        continue;
      }
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
    return areNeighbors(from, to) ? 17 : -17 * 2;
  }

  /**
   * Whether two nodes are neighbors.
   * 
   * @param from The one node.
   * @param to The other node.
   * @return Whether they are neighbors.
   */
  private boolean areNeighbors(final SpringNode from, final SpringNode to) {
    return line ? Math.abs(nodes.indexOf(from) - nodes.indexOf(to)) < 2
        : (Math.abs(nodes.indexOf(from) - nodes.indexOf(to)) % (nodes.size() - 1)) < 2;
  }

  @Override
  public boolean hasWeight(final SpringNode from, final SpringNode to) {
    return true;
  }

  @Override
  public void dragNode(final SpringNode n, final double startX, final double startY,
      final double dx, final double dy) {
    final double x = startX + dx;
    final double y = startY + dy;
    n.setPosition(x, y);
  }

  @Override
  public void selectNode(final SpringNode n) {
    // nothing
  }

  @Override
  public Shape nodeClickArea(final SpringNode n, final boolean real) {
    final double x = real ? n.getX() : n.getPredictX();
    final double y = real ? n.getY() : n.getPredictY();
    return new Ellipse2D.Double(x - 2, y - 2, 4, 4);
  }

  @Override
  public void drawBackground(final Graphics2D g, final Context ctx) {
    // void
  }

  @Override
  public Point2D getDefaultPosition(final SpringNode node) {
    return new Point2D.Double();
  }

  @Override
  public SpringNode getReferenceNode() {
    return null;
  }

  @Override
  public String getTooltipText(final SpringNode n) {
    return null;
  }

  @Override
  public void moveMouse(final Point2D cur) {
    // nothing to do
  }

  @Override
  public int changes() {
    return Weighter.NO_CHANGE;
  }

  @Override
  public Rectangle2D getBoundingBox() {
    return null;
  }

  @Override
  public boolean inAnimation() {
    return false;
  }

  @Override
  public Collection<WeightedEdge> edgesTo(final SpringNode to) {
    return Collections.EMPTY_LIST;
  }

  @Override
  public void setAnimator(final Animator animator) {
    // no need to remember the animator
  }

}
