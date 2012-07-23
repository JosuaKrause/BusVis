package infovis;

import infovis.data.BusDataBuilder;
import infovis.data.BusStationManager;
import infovis.gui.MainWindow;

import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.IOException;

import javax.swing.UIManager;
import javax.swing.WindowConstants;

/**
 * Starts the main application.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class Main {

  /** No constructor. */
  private Main() {
    // no constructor
  }

  /**
   * Starts the main application.
   * 
   * @param args If an argument is provided this path is used as resource path.
   *          Otherwise the default resources are used.
   */
  public static void main(final String[] args) {
    final BusStationManager m;
    try {
      m = BusDataBuilder.load(args.length > 0 ? args[0] : "src/main/resources/konstanz/");
    } catch(final IOException e) {
      e.printStackTrace();
      return;
    }
    try {
      UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
    } catch(final Exception e) {
      // system look and feel already used -- proceed
    }
    // initialize window
    final MainWindow frame = new MainWindow(m);
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
