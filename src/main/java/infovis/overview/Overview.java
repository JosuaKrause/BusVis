package infovis.overview;

import infovis.ctrl.BusVisualization;
import infovis.ctrl.Controller;
import infovis.data.BusStation;
import infovis.data.BusTime;
import infovis.embed.Embedders;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

/**
 * Abstract overview over the bus system in Konstanz.
 * 
 * @author Marc Spicker
 */
public final class Overview extends JSVGCanvas implements BusVisualization {

  /**
   * Serial ID.
   */
  private static final long serialVersionUID = -792509063281208L;

  /**
   * The mouse listener for this class.
   */
  private final OverviewMouse mouse;

  /**
   * The bounding box of the abstract map of Konstanz.
   */
  private final Rectangle2D boundingBox;

  /**
   * Constructor.
   * 
   * @param ctrl The controller.
   * @param width The width.
   * @param height The height.
   */
  public Overview(final Controller ctrl, final int width, final int height) {
    // calculate bounding box
    final String parser = XMLResourceDescriptor.getXMLParserClassName();
    final SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
    SVGDocument doc = null;
    try {
      doc = (SVGDocument) f.createDocument(new File(ctrl.getResourcePath()
          + "abstract.svg").toURI().toString());
    } catch(final IOException e) {
      e.printStackTrace();
    }
    final GVTBuilder builder = new GVTBuilder();
    BridgeContext ctx;
    ctx = new BridgeContext(new UserAgentAdapter());
    final GraphicsNode gvtRoot = builder.build(ctx, doc);
    boundingBox = gvtRoot.getBounds();

    setURI(new File(ctrl.getResourcePath() + "abstract.svg").toURI().toString());
    setPreferredSize(new Dimension(width, height));
    setDisableInteractions(true);
    selectableText = false;
    mouse = new OverviewMouse(this, ctrl);
    addMouseListener(mouse);
    addMouseWheelListener(mouse);
    addMouseMotionListener(mouse);
    ctrl.addBusVisualization(this);
  }

  @Override
  public void focusStation() {
    // TODO focus station
  }

  /**
   * Resets the viewport to the given rectangle.
   */
  private void reset() {
    mouse.reset(boundingBox);
  }

  @Override
  public void paint(final Graphics g) {
    final Graphics2D gfx = (Graphics2D) g;
    final Graphics g2 = gfx.create();
    super.paint(g2);
    g2.dispose();
    if(selectedStation == null) return;
    mouse.transformGraphics(gfx);
    gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    gfx.setColor(Color.RED);
    gfx.setStroke(new BasicStroke(3));
    final double r = OverviewMouse.STATION_RADIUS;
    gfx.draw(new Ellipse2D.Double(selectedStation.getAbstractX() - r,
        selectedStation.getAbstractY() - r, r * 2, r * 2));
  }

  /**
   * The current selected station.
   */
  private BusStation selectedStation;

  @Override
  public void selectBusStation(final BusStation station) {
    if(selectedStation != null && station == null) {
      reset();
    }
    selectedStation = station;
    repaint();
  }

  @Override
  public void setEmbedder(final Embedders embed) {
    // no-op
  }

  @Override
  public void setChangeTime(final int minutes) {
    // no-op
  }

  @Override
  public void setStartTime(final BusTime time) {
    // no-op
  }

  @Override
  public void undefinedChange(final Controller ctrl) {
    // no-op
  }

  @Override
  public void overwriteDisplayedTime(final BusTime time, final boolean blink) {
    // no-op
  }

}
