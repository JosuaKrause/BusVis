package infovis.embed;

import infovis.data.BusData;
import infovis.data.BusStationManager;
import infovis.gui.Canvas;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * A canvas showing distances between bus stations.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class BusCanvas extends Canvas {

  /**
   * Starts a sample application.
   * 
   * @param args Ignored.
   */
  public static void main(final String[] args) {
    final BusStationManager m;
    try {
      m = BusData.load("src/main/resources/");
      m.setMaxTimeHours(3);
    } catch(final IOException e) {
      e.printStackTrace();
      return;
    }
    // ini
    final JFrame frame = new JFrame("Bus test");
    final BusCanvas canvas = createBusCanvas(m, 800, 600);
    frame.add(canvas);
    frame.pack();
    canvas.reset();
    canvas.addAction(KeyEvent.VK_Q, new AbstractAction() {

      private static final long serialVersionUID = -3089254363439068506L;

      @Override
      public void actionPerformed(final ActionEvent e) {
        frame.dispose();
      }

    });
    canvas.addAction(KeyEvent.VK_F, new AbstractAction() {

      private static final long serialVersionUID = 3038019958008049173L;

      @Override
      public void actionPerformed(final ActionEvent e) {
        frame.setExtendedState(frame.getExtendedState() == Frame.MAXIMIZED_BOTH ? Frame.NORMAL
            : Frame.MAXIMIZED_BOTH);
      }

    });
    frame.addWindowStateListener(new WindowStateListener() {

      @Override
      public void windowStateChanged(final WindowEvent e) {
        SwingUtilities.invokeLater(new Runnable() {

          @Override
          public void run() {
            canvas.reset();
          }

        });
      }

    });
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
  }

  /**
   * SVUID.
   */
  private static final long serialVersionUID = 5517376336494016259L;

  /**
   * The spring embedder.
   */
  protected final AbstractEmbedder embed;

  /**
   * The distance measure.
   */
  private final StationDistance dist;

  /**
   * Creates a bus canvas.
   * 
   * @param manager The bus station manager.
   * @param width The width.
   * @param height The height.
   * @return The bus canvas.
   */
  public static BusCanvas createBusCanvas(final BusStationManager manager,
      final int width, final int height) {
    final StationDistance dist = new StationDistance(manager);
    dist.setMinDist(60.0);
    dist.setFactor(10);
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
  private BusCanvas(final AbstractEmbedder embed, final StationDistance dist,
      final int width, final int height) {
    super(embed, width, height);
    this.embed = embed;
    this.dist = dist;
    addAction(KeyEvent.VK_R, new AbstractAction() {

      private static final long serialVersionUID = 1648614278684353766L;

      @Override
      public void actionPerformed(final ActionEvent e) {
        dist.setFrom(null);
      }

    });
    addAction(KeyEvent.VK_V, new AbstractAction() {

      private static final long serialVersionUID = 1929819234561056245L;

      @Override
      public void actionPerformed(final ActionEvent e) {
        reset();
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
