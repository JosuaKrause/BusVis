package infovis.embed;

import infovis.data.BusData;
import infovis.data.BusStation;
import infovis.gui.Canvas;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

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
    try {
      BusData.load("src/main/resources/");
    } catch(final IOException e) {
      e.printStackTrace();
      return;
    }
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
