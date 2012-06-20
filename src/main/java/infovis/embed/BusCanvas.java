package infovis.embed;

import infovis.ctrl.BusVisualization;
import infovis.ctrl.Controller;
import infovis.data.BusDataBuilder;
import infovis.data.BusStation;
import infovis.data.BusStationManager;
import infovis.data.BusTime;
import infovis.gui.Canvas;
import infovis.gui.ControlPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
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
public final class BusCanvas extends Canvas implements BusVisualization {

  /**
   * Starts a sample application.
   * 
   * @param args Ignored.
   */
  public static void main(final String[] args) {
    final BusStationManager m;
    try {
      m = BusDataBuilder.load("src/main/resources/");
    } catch(final IOException e) {
      e.printStackTrace();
      return;
    }
    // ini
    final JFrame frame = new JFrame("Bus test");
    final Controller ctrl = new Controller(m, frame);
    final BusCanvas canvas = createBusCanvas(ctrl, 800, 600);
    frame.setLayout(new BorderLayout());
    frame.add(canvas, BorderLayout.CENTER);
    frame.add(new ControlPanel(ctrl), BorderLayout.EAST);
    frame.pack();
    canvas.reset();
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
  protected final StationDistance dist;

  /**
   * Whether to use a pring embedder.
   */
  private static final boolean USE_SPRING_EMBEDDER = false;

  /**
   * Creates a bus canvas.
   * 
   * @param ctrl The controller.
   * @param width The width.
   * @param height The height.
   * @return The bus canvas.
   */
  public static BusCanvas createBusCanvas(final Controller ctrl, final int width,
      final int height) {
    final StationDistance dist = new StationDistance(ctrl);
    dist.setMinDist(60.0);
    dist.setFactor(10);
    final AbstractEmbedder embed = USE_SPRING_EMBEDDER ? new SpringEmbedder(dist, dist)
    : new CircularEmbedder(dist, dist);
    final BusCanvas res = new BusCanvas(ctrl, embed, dist, width, height);
    ctrl.addBusVisualization(res);
    embed.addRefreshable(res);
    res.setBackground(Color.WHITE);
    return res;
  }

  /**
   * Private constructor.
   * 
   * @param ctrl The controller.
   * @param embed The embedder.
   * @param dist The distance measure.
   * @param width The width.
   * @param height The height.
   */
  private BusCanvas(final Controller ctrl, final AbstractEmbedder embed,
      final StationDistance dist,
      final int width, final int height) {
    super(embed, width, height);
    this.embed = embed;
    this.dist = dist;
    addAction(KeyEvent.VK_R, new AbstractAction() {

      private static final long serialVersionUID = 1648614278684353766L;

      @Override
      public void actionPerformed(final ActionEvent e) {
        ctrl.selectStation(null);
        ctrl.focusStation();
      }

    });
    addAction(KeyEvent.VK_V, new AbstractAction() {

      private static final long serialVersionUID = 1929819234561056245L;

      @Override
      public void actionPerformed(final ActionEvent e) {
        reset();
      }

    });
    addAction(KeyEvent.VK_Q, new AbstractAction() {

      private static final long serialVersionUID = -3089254363439068506L;

      @Override
      public void actionPerformed(final ActionEvent e) {
        ctrl.quit(false);
      }

    });
  }

  @Override
  public void reset() {
    SwingUtilities.invokeLater(new Runnable() {

      @SuppressWarnings("synthetic-access")
      @Override
      public void run() {
        BusCanvas.super.reset();
      }

    });
  }

  @Override
  public void selectBusStation(final BusStation station) {
    dist.setFrom(station);
  }

  @Override
  public void setStartTime(final BusTime time) {
    dist.setTime(time);
  }

  @Override
  public void setChangeTime(final int minutes) {
    dist.setChangeTime(minutes);
  }

  @Override
  public void focusStation() {
    reset();
  }

  @Override
  public void undefinedChange(final Controller ctrl) {
    dist.changeUndefined();
  }

  @Override
  public void overwriteDisplayedTime(final BusTime time, final boolean blink) {
    // no-op
  }

}
