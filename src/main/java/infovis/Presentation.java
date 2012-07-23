package infovis;

import infovis.data.BusStationManager;
import infovis.gui.MainWindow;

import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.IOException;

import javax.swing.WindowConstants;

/**
 * Starts the presentation application.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class Presentation {

  /** No constructor. */
  private Presentation() {
    // no constructor
  }

  /**
   * Starts the presentation application.
   * 
   * @param args If an argument is provided this path is used as resource path.
   *          Otherwise the default resources are used.
   */
  public static void main(final String[] args) {
    final BusStationManager m;
    try {
      m = Main.loadData(args);
    } catch(final IOException e) {
      e.printStackTrace();
      return;
    }
    Main.setLookAndFeel();
    // initialize window
    final MainWindow frame = new MainWindow(m, true);
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
  }

}
