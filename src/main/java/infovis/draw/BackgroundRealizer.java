package infovis.draw;

import static infovis.embed.StationDrawer.*;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

/**
 * Realizes the actual painting of backgrounds.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface BackgroundRealizer {

  /**
   * Draws the background.
   * 
   * @param g The graphics context.
   * @param ref The position of the reference node.
   * @param factor The scaling factor.
   * @param alpha The alpha blend value.
   */
  void drawBackground(Graphics2D g, Point2D ref, double factor, double alpha);

  /**
   * Getter.
   * 
   * @param ref The position of the reference node.
   * @param factor The scaling factor.
   * @return The bounding box of the background.
   */
  Rectangle2D boundingBox(Point2D ref, double factor);

  /**
   * The highest drawn circle interval.
   */
  int MAX_INTERVAL = 12;

  /**
   * No background. However the bounding box is set to one hour in the circle
   * layout.
   */
  BackgroundRealizer NO_BG = new BackgroundRealizer() {

    @Override
    public void drawBackground(final Graphics2D g, final Point2D ref,
        final double factor, final double alpha) {
      // nothing to draw
    }

    @Override
    public Rectangle2D boundingBox(final Point2D ref, final double factor) {
      return getCircle(MAX_INTERVAL, factor, ref).getBounds2D();
    }

  };

  /**
   * Circle background.
   */
  BackgroundRealizer CIRCLES = new BackgroundRealizer() {

    @Override
    public void drawBackground(final Graphics2D g, final Point2D center,
        final double factor, final double alpha) {
      boolean b = true;
      g.setColor(Color.WHITE);
      for(int i = MAX_INTERVAL; i > 0; --i) {
        final Shape circ = getCircle(i, factor, center);
        final Graphics2D g2 = (Graphics2D) g.create();
        if(b) {
          final double d = (MAX_INTERVAL - i + 2.0) / (MAX_INTERVAL + 2);
          final double curAlpha = alpha * d;
          g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
              (float) curAlpha));
          g2.setColor(Color.LIGHT_GRAY);
        }
        b = !b;
        g2.fill(circ);
        g2.dispose();
      }
    }

    @Override
    public Rectangle2D boundingBox(final Point2D center, final double factor) {
      return getCircle(MAX_INTERVAL, factor, center).getBounds2D();
    }

  };

  /**
   * The scaling factor of the cache image.
   */
  double IMG_FACTOR = 5;

  /**
   * The margin to compensate the blur.
   */
  double MARGIN = 10;

  /**
   * Blurred circles using a cached image.
   */
  BackgroundRealizer CIRCLES_BLUR = new BackgroundRealizer() {

    private final Rectangle2D bbox;

    private final BufferedImage img;

    {
      final Rectangle2D b = CIRCLES.boundingBox(new Point2D.Double(), IMG_FACTOR);
      bbox = new Rectangle2D.Double(-MARGIN, -MARGIN, b.getWidth() + MARGIN,
          b.getHeight() + MARGIN);
      final BufferedImage tmp = new BufferedImage((int) bbox.getWidth(),
          (int) bbox.getHeight(), BufferedImage.TYPE_INT_ARGB);
      final Graphics2D g = (Graphics2D) tmp.getGraphics();
      CIRCLES.drawBackground(g,
          new Point2D.Double(bbox.getWidth() * 0.5, bbox.getHeight() * 0.5), IMG_FACTOR,
          1);
      g.dispose();
      // create blur kernel
      final float[] blur = {
          0.00000067f, 0.00002292f, 0.00019117f, 0.00038771f, 0.00019117f, 0.00002292f,
          0.00000067f,
          0.00002292f, 0.00078633f, 0.00655965f, 0.01330373f, 0.00655965f, 0.00078633f,
          0.00002292f,
          0.00019117f, 0.00655965f, 0.05472157f, 0.11098164f, 0.05472157f, 0.00655965f,
          0.00019117f,
          0.00038771f, 0.01330373f, 0.11098164f, 0.22508352f, 0.11098164f, 0.01330373f,
          0.00038771f,
          0.00019117f, 0.00655965f, 0.05472157f, 0.11098164f, 0.05472157f, 0.00655965f,
          0.00019117f,
          0.00002292f, 0.00078633f, 0.00655965f, 0.01330373f, 0.00655965f, 0.00078633f,
          0.00002292f,
          0.00000067f, 0.00002292f, 0.00019117f, 0.00038771f, 0.00019117f, 0.00002292f,
          0.00000067f,
      };
      final ConvolveOp op = new ConvolveOp(new Kernel(7, 7, blur));
      img = op.filter(tmp, null);
    }

    @Override
    public void drawBackground(final Graphics2D g, final Point2D ref,
        final double factor, final double alpha) {
      if(alpha < 1) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha));
      }
      final Rectangle2D b = boundingBox(ref, factor);
      final AffineTransform at =
          AffineTransform.getTranslateInstance(b.getMinX(), b.getMinY());
      at.scale(b.getWidth() / img.getWidth(), b.getHeight() / img.getHeight());
      g.drawImage(img, at, null);
    }

    @Override
    public Rectangle2D boundingBox(final Point2D ref, final double factor) {
      final double f = factor / IMG_FACTOR;
      final double w = img.getWidth() * f;
      final double h = img.getHeight() * f;
      return new Rectangle2D.Double(ref.getX() - w * 0.5, ref.getY() - h * 0.5, w, h);
    }

  };

}
