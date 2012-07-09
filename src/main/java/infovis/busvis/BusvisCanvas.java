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

import javax.swing.SwingUtilities;

/**
 * A canvas showing distances between bus stations.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class BusvisCanvas extends Canvas implements BusVisualization {

  /** The distance measure. */
  protected final BusvisWeighter dist;

  /** The drawer. */
  private final BusvisDrawer draw;

  /** The layouter. */
  protected AbstractLayouter layout;

  /** The layout technique. */
  private Layouts layouter;

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
    final Layouts l = Layouts.CIRCULAR;
    final AbstractLayouter embed = Layouts.createFor(l, draw, dist);
    final BusvisCanvas res = new BusvisCanvas(l, embed, dist, draw, width, height);
    ctrl.addBusVisualization(res);
    res.setBackground(Color.WHITE);
    return res;
  }

  /**
   * Private constructor.
   * 
   * @param layouter The layouter enum.
   * @param layout The corresponding layout.
   * @param dist The distance measure.
   * @param draw The drawer.
   * @param width The width.
   * @param height The height.
   */
  private BusvisCanvas(final Layouts layouter, final AbstractLayouter layout,
      final BusvisWeighter dist, final BusvisDrawer draw,
      final int width, final int height) {
    super(layout, width, height);
    this.layout = layout;
    this.dist = dist;
    this.draw = draw;
    this.layouter = layouter;
    setPaintLock(layout.getAnimationLock());
    layout.addRefreshable(this);
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
   * Sets the currently used layouter.
   * 
   * @param l The layouter.
   */
  public void setPainter(final AbstractLayouter l) {
    if(layout != null) {
      layout.dispose();
    }
    layout = l;
    l.addRefreshable(this);
    super.setPainter(l);
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
  public void setLayout(final Layouts layout) {
    if(layouter == layout) return;
    layouter = layout;
    setPainter(Layouts.createFor(layout, draw, dist));
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
