package infovis.overview;

import infovis.data.BusData;
import infovis.data.BusEdge;
import infovis.data.BusStation;
import infovis.data.BusStationManager;
import infovis.data.BusTime;
import infovis.gui.Canvas;
import infovis.gui.PainterAdapter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * Visualization of the schematic overview of the Konstanz bus network.
 * 
 * @author Marc Spicker
 */
public class Overview extends PainterAdapter {

  /**
   * Data Manager.
   */
  private BusStationManager mgr;

  /**
   * Radius of a bus station on the map.
   */
  private static final int STATION_RADIUS = 6;

  /**
   * Test.
   * 
   * @param args Unused.
   */
  public static void main(final String[] args) {
    final JFrame frame = new JFrame("Bus test");
    final Canvas c = new Canvas(new Overview(), 800, 600);
    frame.add(c);
    frame.pack();
    c.reset();
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
  }

  /**
   * Constructor.
   */
  public Overview() {
    try {
      mgr = BusData.load("src/main/resources/");
      mgr.setMaxTimeHours(3);
    } catch(final IOException e) {
      e.printStackTrace();
      return;
    }
  }

  @Override
  public void draw(final Graphics2D gfx) {
    gfx.setColor(Color.ORANGE);
    gfx.setStroke(new BasicStroke(2.0f));
    for(final BusStation station : mgr.getStations()) {
      if(station.getAbstractX() == Double.MIN_VALUE) {
        continue;
      }
      final Iterable<BusEdge> edges = station.getEdges(new BusTime(0, 0));
      // Draw Bus edges
      for(final BusEdge edge : edges) {
        gfx.setColor(edge.getLine().getColor());
        final BusStation from = edge.getFrom();
        final BusStation to = edge.getTo();
        if(from.getAbstractX() == Double.MIN_VALUE
            || to.getAbstractX() == Double.MIN_VALUE) {
          continue;
        }
        gfx.drawLine((int) from.getAbstractX(), (int) from.getAbstractY(),
            (int) to.getAbstractX(),
            (int) to.getAbstractY());
      }
    }
    // Draw Bus stations
    gfx.setStroke(new BasicStroke());
    for(final BusStation station : mgr.getStations()) {
      if(station.getAbstractX() == Double.MIN_VALUE) {
        continue;
      }
      final int x = (int) station.getAbstractX();
      final int y = (int) station.getAbstractY();
      gfx.setColor(Color.ORANGE);
      gfx.fillOval(x - STATION_RADIUS, y - STATION_RADIUS, STATION_RADIUS * 2,
          STATION_RADIUS * 2);
      gfx.setColor(Color.BLACK);
      gfx.drawOval(x - STATION_RADIUS, y - STATION_RADIUS, STATION_RADIUS
          * 2,
          STATION_RADIUS * 2);
      gfx.drawString(station.getName(), x + 2 * STATION_RADIUS, y + STATION_RADIUS);
    }
  }

}
