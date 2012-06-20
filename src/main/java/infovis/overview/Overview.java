package infovis.overview;

import infovis.ctrl.BusVisualization;
import infovis.ctrl.Controller;
import infovis.data.BusDataBuilder;
import infovis.data.BusStation;
import infovis.data.BusStationManager;
import infovis.data.BusTime;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;

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
   * The mouse listener for this class.
   */
  private final OverviewMouse mouse;

  /**
   * Constructor.
   * 
   * @param ctrl The controller.
   * @param width The width.
   * @param height The height.
   */
  public Overview(final Controller ctrl, final int width, final int height) {
    setURI(new File(ctrl.getResourcePath() + "abstractKN.svg").toURI().toString());
    setPreferredSize(new Dimension(width, height));
    setDisableInteractions(true);
    selectableText = false;
    mouse = new OverviewMouse(this, ctrl);
    addMouseListener(mouse);
    addMouseWheelListener(mouse);
    addMouseMotionListener(mouse);
    ctrl.addBusVisualization(this);
  }

  /**
   * Test.
   * 
   * @param args Ignored.
   */
  public static void main(final String[] args) {
    final JFrame frame = new JFrame("SVG Test");
    final BusStationManager mgr;
    try {
      mgr = BusDataBuilder.load("src/main/resources/");
    } catch(final IOException e) {
      e.printStackTrace();
      return;
    }
    final Overview o = new Overview(new Controller(mgr, frame), 800, 600);
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
  public void paint(final Graphics g) {
    final Graphics2D gfx = (Graphics2D) g;
    final Graphics g2 = gfx.create();
    super.paint(g2);
    g2.dispose();
    if(selectedStation == null) return;
    mouse.transformGraphics(gfx);
    gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    gfx.setColor(Color.RED);
    gfx.setStroke(new BasicStroke(3));
    final double r = OverviewMouse.STATION_RADIUS;
    gfx.draw(new Ellipse2D.Double(selectedStation.getAbstractX() - r,
        selectedStation.getAbstractY() - r, r * 2, r * 2));
  }

  /**
   * The current selected station.
   */
  private BusStation selectedStation;

  @Override
  public void selectBusStation(final BusStation station) {
    selectedStation = station;
    repaint();
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
