package infovis;

import infovis.data.BusStationManager;
import infovis.gui.MainWindow;
import infovis.util.Stopwatch;

import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.WindowConstants;

/**
 * Starts the presentation application.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class PresentationApp {

  /** No constructor. */
  private PresentationApp() {
    // no constructor
  }

  /**
   * Starts the presentation application.
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
    final MainWindow frame = new MainWindow(m, true, false);
    frame.setTitle("BusVis - Presentation - Loading...");
    frame.setName("BusVis");
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
    // exclusive full screen
    final GraphicsConfiguration gc = frame.getGraphicsConfiguration();
    final GraphicsDevice gd = gc != null ? gc.getDevice()
        : GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    gd.setFullScreenWindow(frame);
    frame.setResizable(false);
    frame.setExtendedState(Frame.MAXIMIZED_BOTH);
    System.out.println("Took " + startup.current());
  }

}
