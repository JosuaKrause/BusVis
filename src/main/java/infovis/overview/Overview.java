package infovis.overview;

import infovis.ctrl.BusVisualization;
import infovis.ctrl.Controller;
import infovis.data.BusStation;
import infovis.data.BusTime;
import infovis.draw.StationRealizer;
import infovis.layout.Layouts;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

/**
 * Abstract overview over a bus system.
 * 
 * @author Marc Spicker
 */
public final class Overview extends JSVGCanvas implements BusVisualization {

  /** The sign that shows a selected station on the abstract map. */
  private static final Path2D focusSign;

  static {
    focusSign = new Path2D.Double();
    focusSign.moveTo(0, 0);
    focusSign.lineTo(-15, -40);
    focusSign.curveTo(-5, -30, 5, -30, 15, -40);
    focusSign.closePath();
  }

  /** The mouse listener for this class. */
  protected final OverviewMouse mouse;

  /** The bounding box of the abstract map. */
  private Rectangle2D boundingBox;

  /** Weather the overview is drawn the first time. */
  private boolean firstDraw;

  /** The half-size of the window that is shown when a station is selected. */
  private final int focusSize = 150;

  /** The controller. */
  private final Controller ctrl;

  /**
   * Constructor.
   * 
   * @param ctrl The controller.
   * @param width The width.
   * @param height The height.
   */
  public Overview(final Controller ctrl, final int width, final int height) {
    this.ctrl = ctrl;
    firstDraw = true;

    setPreferredSize(new Dimension(width, height));
    setDisableInteractions(true);
    selectableText = false;

    // mouse listener
    mouse = new OverviewMouse(this, ctrl);
    addMouseListener(mouse);
    addMouseWheelListener(mouse);
    addMouseMotionListener(mouse);

    // resize listener
    addComponentListener(new ComponentAdapter() {

      @Override
      public void componentResized(final ComponentEvent e) {
        final Rectangle2D boundingBox = getSVGBoundingRect();
        if(boundingBox != null) {
          mouse.visibleRectChanged(boundingBox);
        }
      }

    });

    ctrl.addBusVisualization(this);
  }

  /**
   * Setter.
   * 
   * @param focus The component to focus when clicked.
   */
  public void setFocusComponent(final JComponent focus) {
    mouse.setFocusComponent(focus);
  }

  /**
   * Getter.
   * 
   * @return The component to focus when clicked.
   */
  public JComponent getFocusComponent() {
    return mouse.getFocusComponent();
  }

  /**
   * Loads the svg image.
   * 
   * @param ctrl The controller.
   */
  public void loadSVG(final Controller ctrl) {
    // calculate bounding box
    final String parser = XMLResourceDescriptor.getXMLParserClassName();
    final SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
    SVGDocument doc = null;
    URL url = null;
    try {
      url = ctrl.getOverview().getURL();
      doc = (SVGDocument) f.createDocument(url.toString());
    } catch(final IOException e) {
      e.printStackTrace();
    }
    final GVTBuilder builder = new GVTBuilder();
    BridgeContext ctx;
    ctx = new BridgeContext(new UserAgentAdapter());
    final GraphicsNode gvtRoot = builder.build(ctx, doc);
    boundingBox = gvtRoot.getBounds();

    addGVTTreeRendererListener(new GVTTreeRendererAdapter() {

      @Override
      public void gvtRenderingPrepare(final GVTTreeRendererEvent e) {
        reset();
        removeGVTTreeRendererListener(this);
      }

    });

    setURI(url.toString());
  }

  @Override
  public void focusStation() {
    if(selectedStation == null) {
      reset();
    } else {
      final Rectangle2D focus = new Rectangle2D.Double(
          selectedStation.getAbstractX() - focusSize,
          selectedStation.getAbstractY() - focusSize, 2 * focusSize, 2 * focusSize);
      mouse.reset(focus);
    }
  }

  @Override
  public void refresh() {
    repaint();
  }

  /** Resets the viewport to the given rectangle. */
  public void reset() {
    mouse.reset(boundingBox);
  }

  @Override
  public void paint(final Graphics g) {
    if(firstDraw) {
      SwingUtilities.invokeLater(new Runnable() {

        @Override
        public void run() {
          reset();
        }

      });
      firstDraw = false;
    }
    final Graphics g2 = g.create();
    super.paint(g2);
    g2.dispose();
    if(selectedStation == null) return;
    final Graphics2D gfx = (Graphics2D) g;
    gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

    // sign for the selected station
    final AffineTransform st = new AffineTransform();
    st.translate(mouse.getXFromCanvas(selectedStation.getAbstractX()),
        mouse.getYFromCanvas(selectedStation.getAbstractY()));
    final Shape selS = focusSign.createTransformedShape(st);
    gfx.setColor(StationRealizer.PRIM_SEL);
    gfx.fill(selS);
    gfx.setColor(Color.BLACK);
    gfx.draw(selS);

    // sign for the secondary station
    for(final int id : ctrl.secondarySelectedIds()) {
      final BusStation station = ctrl.getForId(id);
      final AffineTransform at = new AffineTransform();
      at.translate(mouse.getXFromCanvas(station.getAbstractX()),
          mouse.getYFromCanvas(station.getAbstractY()));
      final Shape s = focusSign.createTransformedShape(at);
      gfx.setColor(StationRealizer.SEC_SEL);
      gfx.fill(s);
      gfx.setColor(Color.BLACK);
      gfx.draw(s);
    }
  }

  /** The current selected station. */
  private BusStation selectedStation;

  @Override
  public void selectBusStation(final BusStation station) {
    selectedStation = station;
    repaint();
  }

  /**
   * Returns the bounding box of the SVG image.
   * 
   * @return Bounding box of the SVG image.
   */
  public Rectangle2D getSVGBoundingRect() {
    return boundingBox;
  }

  @Override
  public void setLayout(final Layouts embed) {
    // no-op
  }

  @Override
  public void setChangeTime(final int minutes) {
    // no-op
  }

  @Override
  public void setStartTime(final BusTime time, final boolean ffwMode) {
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

  @Override
  public void fastForwardChange(final boolean fastForwardMode,
      final int fastForwardMinutes) {
    // no-op
  }

}
