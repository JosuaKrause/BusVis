package infovis.embed;

import infovis.data.BusLine;
import infovis.data.BusStation;
import infovis.data.BusTime;
import infovis.gui.Canvas;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * A canvas showing distances between bus stations.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class BusCanvas extends Canvas {

  /**
   * Starts a sample application.
   * 
   * @param args Ignored.
   */
  public static void main(final String[] args) {
    BusStation.clearStations();
    // TODO load real data
    final BusLine line = new BusLine("1", Color.RED);
    final BusLine other = new BusLine("2", Color.BLUE);
    final BusStation a = BusStation.createStation("a", 0, 0, 0);
    final BusStation b = BusStation.createStation("b", 1, 0, 15);
    final BusStation c = BusStation.createStation("c", 2, 0, 30);
    final BusStation d = BusStation.createStation("d", 3, 0, 45);
    final BusStation e = BusStation.createStation("e", 4, 0, 60);
    a.addEdge(line, c, new BusTime(3, 10), new BusTime(3, 13));
    a.addEdge(line, b, new BusTime(3, 10), new BusTime(3, 12));
    a.addEdge(line, d, new BusTime(3, 10), new BusTime(3, 11));
    b.addEdge(line, a, new BusTime(3, 10), new BusTime(3, 20));
    b.addEdge(line, c, new BusTime(3, 9), new BusTime(3, 10));
    c.addEdge(line, a, new BusTime(2, 0), new BusTime(2, 1));
    d.addEdge(line, a, new BusTime(0, 1), new BusTime(0, 2));
    d.addEdge(line, b, new BusTime(0, 2), new BusTime(0, 3));
    d.addEdge(line, c, new BusTime(0, 3), new BusTime(0, 4));
    d.addEdge(line, e, new BusTime(0, 4), new BusTime(0, 5));
    final BusStation f = BusStation.createStation("f", 5, 15, 15);
    final BusStation g = BusStation.createStation("g", 6, 15, 30);
    final BusStation h = BusStation.createStation("h", 7, 15, 45);
    e.addEdge(line, h, new BusTime(0, 0), new BusTime(0, 6));
    e.addEdge(line, h, new BusTime(0, 6), new BusTime(0, 8));
    e.addEdge(line, h, new BusTime(0, 50), new BusTime(1, 0));
    e.addEdge(line, f, new BusTime(0, 0), new BusTime(0, 2));
    e.addEdge(other, f, new BusTime(0, 0), new BusTime(0, 1));
    e.addEdge(line, g, new BusTime(0, 1), new BusTime(0, 3));
    f.addEdge(line, h, new BusTime(1, 2), new BusTime(1, 3));
    f.addEdge(line, h, new BusTime(0, 2), new BusTime(0, 5));
    g.addEdge(other, h, new BusTime(0, 3), new BusTime(0, 4));
    g.addEdge(line, h, new BusTime(0, 4), new BusTime(0, 7));
    // ini
    final JFrame frame = new JFrame("Bus test");
    final BusCanvas canvas = createBusCanvas(800, 600);
    frame.add(canvas);
    frame.pack();
    canvas.reset();
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
  }

  /**
   * SVUID
   */
  private static final long serialVersionUID = 5517376336494016259L;

  /**
   * The spring embedder.
   */
  protected final SpringEmbedder embed;

  /**
   * The distance measure.
   */
  private final StationDistance dist;

  /**
   * Creates a bus canvas.
   * 
   * @param width The width.
   * @param height The height.
   * @return The bus canvas.
   */
  public static BusCanvas createBusCanvas(final int width, final int height) {
    final StationDistance dist = new StationDistance();
    final SpringEmbedder embed = new SpringEmbedder(dist, dist);
    final BusCanvas res = new BusCanvas(embed, dist, width, height);
    embed.addRefreshable(res);
    res.setBackground(Color.WHITE);
    return res;
  }

  /**
   * Private constructor.
   * 
   * @param embed The embedder.
   * @param dist The distance measure.
   * @param width The width.
   * @param height The height.
   */
  private BusCanvas(final SpringEmbedder embed, final StationDistance dist,
      final int width, final int height) {
    super(embed, width, height);
    this.embed = embed;
    this.dist = dist;
  }

  @Override
  public void setupKeyActions() {
    addAction(KeyEvent.VK_M, new AbstractAction() {

      private static final long serialVersionUID = 5564826945375201457L;

      @Override
      public void actionPerformed(final ActionEvent e) {
        embed.setCorrectMovement(!embed.isCorrectingMovement());
      }

    });
  }

  @Override
  public void reset() {
    Rectangle2D bbox = null;
    for(final SpringNode n : dist.nodes()) {
      final Rectangle2D b = dist.nodeClickArea(n).getBounds2D();
      if(bbox == null) {
        bbox = b;
      } else {
        bbox.add(b);
      }
    }
    if(bbox == null) {
      super.reset();
    } else {
      reset(bbox);
    }
  }

}
