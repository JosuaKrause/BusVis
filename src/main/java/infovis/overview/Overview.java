package infovis.overview;

import infovis.ctrl.BusVisualization;
import infovis.ctrl.Controller;
import infovis.data.BusStation;
import infovis.data.BusStationManager;
import infovis.data.BusTime;

import java.awt.Dimension;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.apache.batik.swing.JSVGCanvas;

/**
 * Abstract overview over the bus system in Konstanz.
 * 
 * @author Marc Spicker
 */
public class Overview extends JSVGCanvas implements BusVisualization {

  /**
   * Serial ID.
   */
  private static final long serialVersionUID = -792509063281208L;

  /**
   * The controller.
   */
  private final Controller ctrl;

  /**
   * Constructor.
   * 
   * @param ctrl The controller.
   * @param width The width.
   * @param height The height.
   */
  public Overview(final Controller ctrl, final int width, final int height) {
    this.ctrl = ctrl;
    setURI(new File(ctrl.getResourcePath() + "abstractKN.svg").toURI().toString());
    setPreferredSize(new Dimension(width, height));
    this.ctrl.addBusVisualization(this);
  }

  /**
   * Test.
   * 
   * @param args Ignored.
   */
  public static void main(final String[] args) {
    final JFrame frame = new JFrame("SVG Test");
    final Overview o = new Overview(new Controller(new BusStationManager(
        "src/main/resources/"), frame), 800, 600);
    frame.add(o);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
  }

  @Override
  public void focusStation() {
    // TODO Auto-generated method stub

  }

  @Override
  public void selectBusStation(final BusStation station) {
    // TODO Auto-generated method stub

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
