package infovis.overview;

import infovis.data.BusData;
import infovis.data.BusStationManager;
import infovis.gui.Canvas;
import infovis.gui.PainterAdapter;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * Visualization of the schematic overview of the konstanz bus network.
 * 
 * @author Marc Spicker
 */
public class Overview extends PainterAdapter {

  /**
   * Test.
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
    final JFrame frame = new JFrame("Bus test");
    final Canvas c = new Canvas(new Overview(), 800, 600);
    frame.add(c);
    frame.pack();
    c.reset();
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
  }


}
