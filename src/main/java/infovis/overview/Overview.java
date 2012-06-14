package infovis.overview;

import java.io.File;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.apache.batik.swing.JSVGCanvas;

/**
 * Abstract overview over the bus system in Konstanz.
 * 
 * @author Marc Spicker
 */
public class Overview extends JSVGCanvas {

  /**
   * Serial ID.
   */
  private static final long serialVersionUID = -792509063281208L;

  /**
   * Constructor.
   */
  public Overview() {
    setURI(new File("src/main/resources/abstractKN.svg").toURI().toString());
  }

  /**
   * Test.
   * 
   * @param args Ignored.
   */
  public static void main(final String[] args) {
    final JFrame frame = new JFrame("SVG Test");
    final Overview o = new Overview();
    frame.add(o);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
  }

}
