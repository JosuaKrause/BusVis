package infovis.gui;

import infovis.busvis.BusvisCanvas;
import infovis.ctrl.Controller;
import infovis.data.BusStationManager;
import infovis.overview.Overview;
import infovis.util.Screenshot;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * The main window.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class MainWindow extends JFrame {

  /** All open windows. */
  private final List<JFrame> wnds;

  /** Controller. */
  protected final Controller ctrl;

  /**
   * Creates the main window.
   * 
   * @param m The bus station manager.
   * @param presentation Whether the application is in presentation mode.
   * @param bigScreen Whether the application is in big screen mode.
   */
  public MainWindow(final BusStationManager m, final boolean presentation,
      final boolean bigScreen) {
    ctrl = new Controller(m, this);
    wnds = new LinkedList<JFrame>();
    final Overview over;
    over = ctrl.hasOverview() ? new Overview(ctrl, 400, 400) : null;
    final ControlPanel cp = new ControlPanel(ctrl);
    final BusvisCanvas mainCanvas = BusvisCanvas.createBusCanvas(ctrl, 900, 900);
    add(createUI(over, cp, mainCanvas, bigScreen));
    pack();
    mainCanvas.reset();
    if(!presentation) {
      addAction(KeyEvent.VK_F, new AbstractAction() {

        @Override
        public void actionPerformed(final ActionEvent e) {
          final boolean isMax = getExtendedState() == Frame.MAXIMIZED_BOTH;
          setExtendedState(isMax ? Frame.NORMAL : Frame.MAXIMIZED_BOTH);
        }

      });
    }
    addAction(KeyEvent.VK_P, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent ae) {
        System.out.println("saving screenshots...");
        final File dir = new File("./pics/");
        try {
          Screenshot.saveSVG(dir, "vis", mainCanvas);
        } catch(final IOException e) {
          e.printStackTrace();
        }
        try {
          Screenshot.savePNG(dir, "vis", mainCanvas);
          Screenshot.savePNG(dir, "all", getRootPane());
        } catch(final IOException e) {
          e.printStackTrace();
        }
        System.out.println("done");
      }

    });
    addAction(KeyEvent.VK_Q, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        ctrl.quit(false);
      }

    });
    addAction(KeyEvent.VK_R, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        ctrl.selectStation(null);
        ctrl.focusStation();
      }

    });
    addAction(KeyEvent.VK_V, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        mainCanvas.reset();
      }

    });
    addAction(KeyEvent.VK_L, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        ctrl.toggleLegend();
      }

    });
    addWindowStateListener(new WindowStateListener() {

      @Override
      public void windowStateChanged(final WindowEvent e) {
        SwingUtilities.invokeLater(new Runnable() {

          @Override
          public void run() {
            if(ctrl.getSelectedStation() == null) {
              mainCanvas.reset();
            }
          }

        });
      }

    });
    if(over != null) {
      SwingUtilities.invokeLater(new Runnable() {

        @Override
        public void run() {
          over.loadSVG(ctrl);
        }

      });
    }
    final JRootPane root = getRootPane();
    root.setFocusable(true);
    root.grabFocus();
    mainCanvas.setFocusComponent(root);
    if(over != null) {
      over.setFocusComponent(root);
    }
  }

  /**
   * Creates the UI for this window.
   * 
   * @param over The overview. May be <code>null</code>.
   * @param cp The control panel.
   * @param mainCanvas The main canvas.
   * @param bigScreen Whether the application is run on a big screen.
   * @return The component to add to the root pane.
   */
  private JComponent createUI(final Overview over, final ControlPanel cp,
      final BusvisCanvas mainCanvas, final boolean bigScreen) {
    if(!bigScreen) {
      JComponent left;
      if(over != null) {
        final JSplitPane l = new JSplitPane(JSplitPane.VERTICAL_SPLIT, over, cp);
        l.setDividerLocation(400);
        l.setOneTouchExpandable(true);
        left = l;
      } else {
        left = cp;
      }
      final JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left,
          mainCanvas);
      return pane;
    }
    final JFrame controlFrame = new JFrame("Controls");
    controlFrame.add(cp);
    controlFrame.pack();
    addAndShowWindow(controlFrame);
    if(over != null) {
      final JFrame overFrame = new JFrame("Overview");
      overFrame.add(over);
      overFrame.pack();
      addAndShowWindow(overFrame);
    }
    return mainCanvas;
  }

  /**
   * Adds a window to this window as child and shows it.
   * 
   * @param frame The window to add.
   */
  private void addAndShowWindow(final JFrame frame) {
    frame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    frame.setLocationByPlatform(true);
    frame.setVisible(true);
    wnds.add(frame);
  }

  /**
   * Adds a keyboard action event.
   * 
   * @param key The key id, given by {@link KeyEvent}. (Constants beginning with
   *          <code>VK</code>)
   * @param a The action that is performed.
   */
  public void addAction(final int key, final Action a) {
    final JRootPane root = getRootPane();
    final Object token = new Object();
    final InputMap input = root.getInputMap();
    input.put(KeyStroke.getKeyStroke(key, 0), token);
    final ActionMap action = root.getActionMap();
    action.put(token, a);
  }

  @Override
  public void dispose() {
    ctrl.quit(true);
    while(!wnds.isEmpty()) {
      final JFrame w = wnds.remove(0);
      w.dispose();
    }
    super.dispose();
  }

}
