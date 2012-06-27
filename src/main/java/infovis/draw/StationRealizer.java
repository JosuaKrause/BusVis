package infovis.draw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;

/**
 * Realizes the actual painting of bus stations.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface StationRealizer {

  /**
   * The shape of the depiction of the bus station. This is also the are that
   * can be clicked on.
   * 
   * @param x The x translation.
   * @param y The y translation.
   * @param r The radius of the station.
   * @return The shape of the bus station.
   */
  Shape createStationShape(double x, double y, double r);

  /**
   * Draws the bus station.
   * 
   * @param g The graphics context.
   * @param station The station that is drawn.
   * @param stroke The stroke that can be used.
   * @param referenceNode Whether this node is the reference node.
   */
  void drawStation(Graphics2D g, Shape station, Stroke stroke, boolean referenceNode);

  /**
   * The standard way to show bus stations.
   * 
   * @author Joschi <josua.krause@googlemail.com>
   */
  StationRealizer STANDARD = new StationRealizer() {

    @Override
    public Shape createStationShape(final double x, final double y, final double r) {
      return new Ellipse2D.Double(x - r, y - r, r * 2, r * 2);
    }

    @Override
    public void drawStation(final Graphics2D g, final Shape node, final Stroke stroke,
        final boolean referenceNode) {
      g.setColor(!referenceNode ? Color.WHITE : Color.RED);
      g.fill(node);
      g.setStroke(stroke);
      g.setColor(Color.BLACK);
      g.draw(node);
    }

  };

}
