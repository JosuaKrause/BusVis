package infovis.overview;

import infovis.ctrl.Controller;
import infovis.data.BusData;
import infovis.data.BusLine;
import infovis.data.BusStation;
import infovis.data.BusStation.Neighbor;
import infovis.data.BusStationManager;
import infovis.gui.Canvas;
import infovis.gui.PainterAdapter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * Visualization of the schematic overview of the Konstanz bus network.
 * 
 * @author Marc Spicker
 */
public class Overview extends PainterAdapter {

  /**
   * Test.
   * 
   * @param args Unused.
   */
  public static void main(final String[] args) {
    BusStationManager mgr;
    try {
      mgr = BusData.load("src/main/resources/");
      mgr.setMaxTimeHours(3);
    } catch(final IOException e) {
      e.printStackTrace();
      return;
    }
    final JFrame frame = new JFrame("Bus test");
    final Controller ctrl = new Controller(mgr, frame);
    final Overview overview = new Overview(ctrl);
    final Canvas c = new OverviewCanvas(overview, 800, 600);
    frame.add(c);
    frame.pack();
    c.setBackground(Color.WHITE);
    c.reset();
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
  }

  /**
   * The controller.
   */
  private final Controller ctrl;

  /**
   * Constructor.
   * 
   * @param ctrl The controller.
   */
  public Overview(final Controller ctrl) {
    this.ctrl = ctrl;
  }

  /**
   * The currently selected bus station.
   */
  private BusStation selected;

  @Override
  public void draw(final Graphics2D g) {
    // draw edges
    for(final BusStation station : ctrl.getStations()) {
      drawEdge(g, station);
    }
    // draw bus stations
    for(final BusStation station : ctrl.getStations()) {
      if(!station.hasAbstractPosition()) {
        continue;
      }
      final double x = station.getAbstractX();
      final double y = station.getAbstractY();
      final Graphics2D g2 = (Graphics2D) g.create();
      g2.setColor(!station.equals(selected) ? Color.WHITE : Color.RED);
      final Shape shape = getStationShape(x, y, station);
      g2.fill(shape);
      g2.setStroke(new BasicStroke(.5f));
      g2.setColor(Color.BLACK);
      g2.draw(shape);
      g2.dispose();
    }
    for(final BusStation station : ctrl.getStations()) {
      if(!station.hasAbstractPosition()) {
        continue;
      }
      final double x = station.getAbstractX();
      final double y = station.getAbstractY();
      drawLabel(g, x, y, station);
    }
  }

  @Override
  public boolean click(final Point2D p) {
    // TODO select bus stations
    return false;
  }

  /**
   * Calculates the shape of a bus station.
   * 
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param station The station.
   * @return The shape of the station.
   */
  public Shape getStationShape(final double x, final double y, final BusStation station) {
    final double r = Math.max(2, station.getMaxDegree() / 2);
    return new Ellipse2D.Double(x - r, y - r, r * 2, r * 2);
  }

  /**
   * Draws edges of a station.
   * 
   * @param g The graphics context.
   * @param station The station.
   */
  public void drawEdge(final Graphics2D g, final BusStation station) {
    if(!station.hasAbstractPosition()) return;
    final double x1 = station.getAbstractX();
    final double y1 = station.getAbstractY();
    for(final Neighbor edge : station.getNeighbors()) {
      final BusStation neighbor = edge.station;
      if(!neighbor.hasAbstractPosition()) {
        continue;
      }
      final double x2 = neighbor.getAbstractX();
      final double y2 = neighbor.getAbstractY();
      drawEdge(g, x1, y1, x2, y2, edge.lines);
    }
  }

  /**
   * Draws an edge.
   * 
   * @param g The graphics context.
   * @param x1 The start x coordinate.
   * @param y1 The start y coordinate.
   * @param x2 The end x coordinate.
   * @param y2 The end y coordinate.
   * @param lines The bus lines connecting the bus stations.
   */
  public void drawEdge(final Graphics2D g, final double x1, final double y1,
      final double x2, final double y2, final BusLine[] lines) {
    int counter = 0;
    for(final BusLine line : lines) {
      g.setStroke(new BasicStroke(lines.length - counter, BasicStroke.CAP_ROUND,
          BasicStroke.JOIN_BEVEL));
      g.setColor(line.getColor());
      g.draw(new Line2D.Double(x1, y1, x2, y2));
      ++counter;
    }
  }

  /**
   * Draws a label.
   * 
   * @param g The graphics context.
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param station The bus station.
   */
  public void drawLabel(final Graphics2D g, final double x, final double y,
      final BusStation station) {
    if(station.getNeighbors().length == 2) return;
    final Graphics2D gfx = (Graphics2D) g.create();
    gfx.setColor(Color.BLACK);
    gfx.translate(x, y);
    gfx.drawString(station.getName(), 0, 0);
    gfx.dispose();
  }

  /**
   * Selects a bus station.
   * 
   * @param station The bus station to select.
   */
  public void selectBusStation(final BusStation station) {
    selected = station;
  }

}
