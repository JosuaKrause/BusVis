package infovis.embed;

import infovis.ctrl.BusVisualization;
import infovis.ctrl.Controller;
import infovis.data.BusStation;
import infovis.data.BusTime;
import infovis.draw.LabelRealizer;
import infovis.draw.LineRealizer;
import infovis.draw.StationRealizer;
import infovis.gui.Canvas;
import infovis.gui.Painter;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

/**
 * A canvas showing distances between bus stations.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class BusCanvas extends Canvas implements BusVisualization {

  /**
   * SVUID.
   */
  private static final long serialVersionUID = 5517376336494016259L;

  /**
   * The distance measure.
   */
  protected final StationDistance dist;

  /**
   * The drawer.
   */
  private final StationDrawer draw;

  /**
   * The embedder.
   */
  protected AbstractEmbedder embed;

  /**
   * The embedding technique.
   */
  private Embedders embedder;

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
    final StationDrawer draw = new StationDrawer(dist,
        StationRealizer.STANDARD, LineRealizer.ADVANCED);
    dist.setMinDist(60.0);
    dist.setFactor(10);
    final Embedders e = Embedders.CIRCULAR;
    final AbstractEmbedder embed = Embedders.createFor(e, draw, dist);
    final BusCanvas res = new BusCanvas(ctrl, e, embed, dist, draw, width, height);
    ctrl.addBusVisualization(res);
    res.setBackground(Color.WHITE);
    return res;
  }

  /**
   * Private constructor.
   * 
   * @param ctrl The controller.
   * @param e The embedder enum.
   * @param embed The corresponding embedder.
   * @param dist The distance measure.
   * @param draw The drawer.
   * @param width The width.
   * @param height The height.
   */
  private BusCanvas(final Controller ctrl, final Embedders e,
      final AbstractEmbedder embed,
      final StationDistance dist, final StationDrawer draw,
      final int width, final int height) {
    super(embed, width, height);
    this.embed = embed;
    this.dist = dist;
    this.draw = draw;
    embedder = e;
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
    embed.addRefreshable(this);
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

  /**
   * Sets the currently used embedder.
   * 
   * @param embed The embedder.
   */
  public void setPainter(final AbstractEmbedder embed) {
    if(this.embed != null) {
      this.embed.dispose();
    }
    this.embed = embed;
    embed.addRefreshable(this);
    super.setPainter(embed);
    dist.changeUndefined();
  }

  @Override
  public void setPainter(final Painter p) {
    if(p instanceof AbstractEmbedder) {
      setPainter((AbstractEmbedder) p);
    }
    throw new IllegalArgumentException("p must be a "
        + AbstractEmbedder.class.getSimpleName());
  }

  @Override
  public void setEmbedder(final Embedders embedder) {
    if(this.embedder == embedder) return;
    this.embedder = embedder;
    setPainter(Embedders.createFor(embedder, draw, dist));
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
