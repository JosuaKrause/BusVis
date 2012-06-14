package infovis.overview;

import infovis.ctrl.BusVisualization;
import infovis.ctrl.Controller;
import infovis.data.BusStation;
import infovis.data.BusTime;
import infovis.gui.Canvas;

/**
 * The canvas for the overview.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class OverviewCanvas extends Canvas implements BusVisualization {

  /**
   * SVUID.
   */
  private static final long serialVersionUID = -3481359784062284632L;

  /**
   * The overview.
   */
  private final Overview view;

  /**
   * Creates a canvas for the overview.
   * 
   * @param view The overview.
   * @param width The width.
   * @param height The height.
   */
  public OverviewCanvas(final Overview view, final int width, final int height) {
    super(view, width, height);
    this.view = view;
  }

  @Override
  public void focusStation() {
    // TODO focus station
  }

  @Override
  public void selectBusStation(final BusStation station) {
    view.selectBusStation(station);
    refresh();
  }

  @Override
  public void setChangeTime(final int minutes) {
    // no-op
  }

  @Override
  public void setStartTime(final BusTime time) {
    // no-op
  }

  @Override
  public void undefinedChange(final Controller ctrl) {
    // no-op
  }

}
