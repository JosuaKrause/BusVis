package infovis;

import infovis.data.BusStationManager;
import infovis.gui.MainWindow;

import java.io.IOException;

import javax.swing.WindowConstants;

/**
 * Starts the application for the big screen.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class BigScreenApp {

  /** No constructor. */
  private BigScreenApp() {
    // no constructor
  }

  /**
   * Starts the big screen application.
   * 
   * @param args If an argument is provided this path is used as resource path.
   *          Otherwise the default resources are used.
   */
  public static void main(final String[] args) {
    final BusStationManager m;
    try {
      m = DesktopApp.loadData(args);
    } catch(final IOException e) {
      e.printStackTrace();
      return;
    }
    DesktopApp.setLookAndFeel();
    // initialize window
    final MainWindow frame = new MainWindow(m, false, true);
    frame.setTitle("BusVis - Big-Screen - Loading...");
    frame.setName("BusVis");
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
  }

}
