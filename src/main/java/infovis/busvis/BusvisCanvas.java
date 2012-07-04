package infovis.busvis;

import infovis.ctrl.BusVisualization;
import infovis.ctrl.Controller;
import infovis.data.BusStation;
import infovis.data.BusTime;
import infovis.draw.LabelRealizer;
import infovis.draw.LegendRealizer;
import infovis.draw.LineRealizer;
import infovis.draw.StationRealizer;
import infovis.gui.Canvas;
import infovis.gui.Painter;
import infovis.layout.AbstractLayouter;
import infovis.layout.Layouts;

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
public final class BusvisCanvas extends Canvas implements BusVisualization {

  /**
   * SVUID.
   */
  private static final long serialVersionUID = 5517376336494016259L;

  /**
   * The distance measure.
   */
  protected final BusvisWeighter dist;

  /**
   * The drawer.
   */
  private final BusvisDrawer draw;

  /**
   * The embedder.
   */
  protected AbstractLayouter embed;

  /**
   * The embedding technique.
   */
  private Layouts embedder;

  /**
   * Creates a bus canvas.
   * 
   * @param ctrl The controller.
   * @param width The width.
   * @param height The height.
   * @return The bus canvas.
   */
  public static BusvisCanvas createBusCanvas(final Controller ctrl, final int width,
      final int height) {
    final BusvisWeighter dist = new BusvisWeighter(ctrl);
    final BusvisDrawer draw = new BusvisDrawer(dist, StationRealizer.STANDARD,
        LineRealizer.ADVANCED, LabelRealizer.STANDARD, LegendRealizer.STANDARD);
    dist.setMinDist(60.0);
    dist.setFactor(10);
    final Layouts e = Layouts.CIRCULAR;
    final AbstractLayouter embed = Layouts.createFor(e, draw, dist);
    final BusvisCanvas res = new BusvisCanvas(ctrl, e, embed, dist, draw, width, height);
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
  private BusvisCanvas(final Controller ctrl, final Layouts e,
      final AbstractLayouter embed,
      final BusvisWeighter dist, final BusvisDrawer draw,
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
        BusvisCanvas.super.reset();
      }

    });
  }

  @Override
  public void selectBusStation(final BusStation station) {
    dist.setFrom(station);
  }

  @Override
  public void setStartTime(final BusTime time, final boolean ffwMode) {
    dist.setTime(time, ffwMode);
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
  public void setPainter(final AbstractLayouter embed) {
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
    if(p instanceof AbstractLayouter) {
      setPainter((AbstractLayouter) p);
    }
    throw new IllegalArgumentException("p must be an "
        + AbstractLayouter.class.getSimpleName());
  }

  @Override
  public void setEmbedder(final Layouts embedder) {
    if(this.embedder == embedder) return;
    this.embedder = embedder;
    setPainter(Layouts.createFor(embedder, draw, dist));
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

  @Override
  public void fastForwardChange(final boolean fastForwardMode,
      final int fastForwardMinutes) {
    // no-op
  }

}
