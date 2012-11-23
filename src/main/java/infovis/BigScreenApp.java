package infovis;

import infovis.data.BusStationManager;
import infovis.gui.MainWindow;
import infovis.util.Stopwatch;

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
    final BusStationManager m = DesktopApp.loadData(args);
    final Stopwatch startup = new Stopwatch();
    System.out.println("Starting up...");
    DesktopApp.setLookAndFeel();
    // initialize window
    final MainWindow frame = new MainWindow(m, false, true);
    frame.setTitle("BusVis - Big-Screen - Loading...");
    frame.setName("BusVis");
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
    System.out.println("Took " + startup.current());
  }

}
