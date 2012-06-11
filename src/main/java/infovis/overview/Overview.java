package infovis.overview;

import infovis.data.BusData;
import infovis.data.BusStation;
import infovis.gui.Canvas;
import infovis.gui.PainterAdapter;

import java.awt.Graphics2D;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * @author Marc Spicker
 */
public class Overview extends PainterAdapter {

  /**
   * Test.
   * 
   * @param args Ignored.
   */
  public static void main(final String[] args) {
    BusStation.clearStations();
    BusStation.setMaxTimeHours(3);
    try {
      BusData.load("src/main/resources/");
    } catch(final IOException e) {
      e.printStackTrace();
      return;
    }
    final JFrame frame = new JFrame("Bus test");
    final Canvas c = new Canvas(new Overview(), 800, 600);
    frame.add(c);
    frame.pack();
    c.reset();
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
  }

  @Override
  public void draw(final Graphics2D gfx) {

  }

}
