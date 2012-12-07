package infovis;

import infovis.data.BusDataBuilder;
import infovis.data.BusStationManager;
import infovis.gui.MainWindow;
import infovis.util.Resource;
import infovis.util.Stopwatch;

import javax.swing.UIManager;
import javax.swing.WindowConstants;

/**
 * Starts the desktop application.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class DesktopApp {

  /** No constructor. */
  private DesktopApp() {
    // no constructor
  }

  /**
   * Starts the desktop application.
   * 
   * @param args If an argument is provided this path is used as resource path.
   *          Otherwise the default resources are used.
   */
  public static void main(final String[] args) {
    final BusStationManager m = loadData(args);
    final Stopwatch startup = new Stopwatch();
    System.out.println("Starting up...");
    setLookAndFeel();
    // initialize window
    final MainWindow frame = new MainWindow(m, false, false);
    frame.setTitle("BusVis - Desktop - Loading...");
    frame.setName("BusVis");
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
    System.out.println("Took " + startup.current());
  }

  /** Sets the look and feel of the application. */
  public static void setLookAndFeel() {
    try {
      UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
    } catch(final Exception e) {
      // system look and feel already used -- proceed
    }
  }

  /**
   * Prints the usage and terminates the application.
   * 
   * @param <T> Used to reflect method result types.
   * @param msg An optional message.
   * @return Can be used in the return call from the caller method.
   */
  private static <T> T usageAndExit(final String msg) {
    if(msg != null) {
      System.err.println(msg);
    }
    System.err.println("Usage: [[-r] <path> [-d <dump>] [-c <encoding>]] | [-h | --help]");
    System.err.println("<path>: The path to search for schedule data.");
    System.err.println("-r: Signals to search within the jar.");
    System.err.println("-d <dump>: Signals to use the given folder for cached data.");
    System.err.println("-c <encoding>: The character encoding of the input data.");
    System.err.println("-h --help: Prints this help.");
    System.exit(1);
    return null;
  }

  /**
   * Loads the data of the application.
   * 
   * @param args The arguments passed to the application.
   * @return The manager with the data.
   */
  public static BusStationManager loadData(final String[] args) {
    try {
      final Resource r;
      int pos = 0;
      String cur = readNext(args, pos++);
      if("-h".equals(cur) || "--help".equals(cur)) return usageAndExit(null);
      if(cur == null) {
        r = new Resource(Resource.RESOURCES, "nyc/mta_20120701.zip", Resource.UTF8,
            "cache");
      } else {
        final String file;
        final String local;
        if("-r".equals(cur)) {
          file = requireNext(args, pos++, "Missing path argument.");
          local = Resource.RESOURCES;
        } else {
          file = cur;
          local = null;
        }
        final String dump;
        cur = readNext(args, pos++);
        if("-d".equals(cur)) {
          dump = requireNext(args, pos++, "Missing dump path.");
          cur = readNext(args, pos++);
        } else {
          dump = null;
        }
        final String encoding;
        if("-c".equals(cur)) {
          encoding = requireNext(args, pos++, "Missing encoding.");
          cur = readNext(args, pos++);
        } else {
          encoding = null;
        }
        if(cur != null) {
          System.err.println("Supefluous arguments not interpreted: " + cur);
        }
        System.err.println("Loading with: " + local + " " + file
            + " " + encoding + " " + dump);
        r = new Resource(local, file, encoding, dump);
      }
      return BusDataBuilder.load(r);
    } catch(final Exception e) {
      e.printStackTrace();
      return usageAndExit("Exception during startup.");
    }
  }

  /**
   * Requires the element in the array.
   * 
   * @param args The argument array.
   * @param pos The position in the array.
   * @param onErr The message that is shown when there is no element.
   * @return The element.
   */
  private static String requireNext(final String[] args, final int pos, final String onErr) {
    if(args.length <= pos) return usageAndExit(onErr);
    return args[pos];
  }

  /**
   * Returns the element of the array if any.
   * 
   * @param args The argument array.
   * @param pos The position in the array.
   * @return The element or <code>null</code> if no element exists.
   */
  private static String readNext(final String[] args, final int pos) {
    return args.length <= pos ? null : args[pos];
  }

}
