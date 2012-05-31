package infovis.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * A little test application for the canvas class.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class Test extends PainterAdapter {

  /**
   * Starts the test application.
   * 
   * @param args No args.
   */
  public static void main(final String[] args) {
    final Test t = new Test();
    final Canvas c = new Canvas(t, 800, 600);
    final JFrame frame = new JFrame("Test");
    frame.add(c);
    frame.pack();
    c.setBackground(Color.WHITE);
    c.reset(new Rectangle2D.Double(-400, -300, 800, 600));
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
  }

  /**
   * Right eye.
   */
  private static final Ellipse2D RIGHT_EYE = new Ellipse2D.Double(20, 10, 10, 10);

  /**
   * Left eye.
   */
  private static final Ellipse2D LEFT_EYE = new Ellipse2D.Double(400, 310, 10, 10);

  /**
   * Line.
   */
  private static final Line2D LINE = new Line2D.Double(0, 30, 30, 30);

  /**
   * Right eye clicked?
   */
  private boolean right;

  /**
   * Left eye clicked?
   */
  private boolean left;

  @Override
  public boolean click(final Point2D p) {
    if(RIGHT_EYE.contains(p)) {
      right = !right;
      return true;
    }
    return false;
  }

  @Override
  public boolean clickHUD(final Point2D p) {
    if(LEFT_EYE.contains(p)) {
      left = !left;
      return true;
    }
    return false;
  }

  @Override
  public void draw(final Graphics2D gfx) {
    gfx.setColor(right ? Color.RED : Color.BLUE);
    gfx.fill(RIGHT_EYE);
    gfx.setColor(Color.BLACK);
    gfx.draw(LINE);
    gfx.draw(RIGHT_EYE);
  }

  @Override
  public void drawHUD(final Graphics2D gfx) {
    gfx.setColor(left ? Color.RED : Color.BLUE);
    gfx.fill(LEFT_EYE);
    gfx.setColor(Color.BLACK);
    gfx.draw(LEFT_EYE);
  }

}
