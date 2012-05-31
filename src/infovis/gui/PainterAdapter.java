package infovis.gui;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

/**
 * Provides meaningful default implementations for a {@link Painter}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class PainterAdapter implements Painter {

  @Override
  public void draw(final Graphics2D gfx) {
    // draw nothing
  }

  @Override
  public void drawHUD(final Graphics2D gfx) {
    // draw nothing
  }

  @Override
  public boolean click(final Point2D p) {
    // the event is not consumed
    return false;
  }

  @Override
  public boolean clickHUD(final Point2D p) {
    // the event is not consumed
    return false;
  }

}
